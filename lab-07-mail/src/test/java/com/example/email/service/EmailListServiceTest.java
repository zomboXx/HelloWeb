package com.example.email.service;

import com.example.email.data.UserRepository;
import com.example.email.mail.DisabledMailSender;
import com.example.email.mail.WelcomeMailSender;
import com.example.email.mail.MailDelivery;
import com.example.email.mail.MailDeliveryUncertainException;
import com.example.email.model.User;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import static com.example.email.service.RegistrationResult.MailStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class EmailListServiceTest {
    private static EntityManagerFactory factory;
    private UserRepository users;

    @BeforeAll
    static void openTestDatabase() {
        factory = Persistence.createEntityManagerFactory("email-list-mail", Map.of(
                "hibernate.hikari.jdbcUrl", "jdbc:h2:mem:test_mail_" + UUID.randomUUID()
                        + ";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
                "hibernate.show_sql", "false"));
    }

    @AfterAll
    static void closeTestDatabase() {
        if (factory != null) {
            factory.close();
        }
    }

    @BeforeEach
    void clearUsers() {
        try (EntityManager manager = factory.createEntityManager()) {
            manager.getTransaction().begin();
            manager.createQuery("DELETE FROM User").executeUpdate();
            manager.getTransaction().commit();
        }
        users = new UserRepository(factory);
    }

    @Test
    void registrationIsCommittedBeforeSendingMail() {
        CountingMailSender mail = new CountingMailSender(false);
        RegistrationResult result = new EmailListService(users, mail).register(user());
        assertEquals(ACCEPTED_BY_SMTP, result.status());
        assertEquals(1, mail.calls);
        assertTrue(users.findByEmail(user().getEmail()).isPresent());
    }

    @Test
    void duplicateDoesNotSendAnotherMail() {
        CountingMailSender mail = new CountingMailSender(false);
        EmailListService service = new EmailListService(users, mail);
        service.register(user());
        assertEquals(DUPLICATE, service.register(user()).status());
        assertEquals(1, mail.calls);
    }

    @Test
    void smtpFailureKeepsRegistrationSaved() {
        CountingMailSender mail = new CountingMailSender(true);
        RegistrationResult result = new EmailListService(users, mail).register(user());
        assertEquals(FAILED, result.status());
        assertTrue(users.findByEmail(user().getEmail()).isPresent());
    }

    @Test
    void missingSmtpConfigurationIsNotReportedAsSent() {
        RegistrationResult result = new EmailListService(users, new DisabledMailSender()).register(user());
        assertEquals(NOT_CONFIGURED, result.status());
        assertTrue(users.findByEmail(user().getEmail()).isPresent());
    }

    @Test
    void apiAcceptanceIsRecordedAndDuplicateDoesNotResend() {
        CountingMailSender mail = new CountingMailSender(false) {
            @Override
            public MailDelivery sendWelcome(User user) throws MessagingException {
                super.sendWelcome(user); // Also checks the JPA commit happened first.
                return new MailDelivery(MailDelivery.Transport.EMAIL_API, "Brevo",
                        MailDelivery.FallbackReason.SMTP_UNAVAILABLE);
            }
        };
        EmailListService service = new EmailListService(users, mail);
        RegistrationResult result = service.register(user());
        assertEquals(ACCEPTED_BY_API, result.status());
        assertEquals("Brevo", result.delivery().provider());
        assertEquals(MailDelivery.FallbackReason.SMTP_UNAVAILABLE, result.delivery().fallbackReason());
        assertEquals(DUPLICATE, service.register(user()).status());
        assertEquals(1, mail.calls);
    }

    @Test
    void uncertainAcceptanceKeepsRegistrationAndDoesNotClaimSent() {
        WelcomeMailSender mail = new CountingMailSender(false) {
            @Override
            public MailDelivery sendWelcome(User user) throws MessagingException {
                super.sendWelcome(user);
                throw new MailDeliveryUncertainException();
            }
        };
        assertEquals(UNCONFIRMED, new EmailListService(users, mail).register(user()).status());
        assertTrue(users.findByEmail(user().getEmail()).isPresent());
    }

    private User user() {
        return new User("student@example.test", "John", "Smith");
    }

    private class CountingMailSender implements WelcomeMailSender {
        private final boolean fail;
        private int calls;

        CountingMailSender(boolean fail) {
            this.fail = fail;
        }

        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public MailDelivery sendWelcome(User user) throws MessagingException {
            calls++;
            assertTrue(users.findByEmail(user.getEmail()).isPresent());
            if (fail) {
                throw new MessagingException("Simulated SMTP failure");
            }
            return MailDelivery.smtp();
        }
    }
}
