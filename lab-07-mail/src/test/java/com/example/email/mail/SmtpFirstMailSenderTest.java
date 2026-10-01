package com.example.email.mail;

import com.example.email.model.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;

import static com.example.email.mail.MailDelivery.FallbackReason.*;
import static org.junit.jupiter.api.Assertions.*;

class SmtpFirstMailSenderTest {
    private final User user = new User("student@example.test", "John", "Smith");

    @Test
    void smtpSuccessNeverCallsApi() throws Exception {
        StubSender smtp = new StubSender(MailDelivery.smtp(), null);
        StubSender api = api(null);
        assertEquals(MailDelivery.Transport.SMTP,
                new SmtpFirstMailSender(smtp, api, true).sendWelcome(user).transport());
        assertEquals(1, smtp.calls);
        assertEquals(0, api.calls);
    }

    @Test
    void connectionFailureFallsBackExactlyOnce() throws Exception {
        StubSender smtp = new StubSender(null, new SmtpUnavailableException(new MessagingException("connect")));
        StubSender api = api(null);
        MailDelivery result = new SmtpFirstMailSender(smtp, api, true).sendWelcome(user);
        assertEquals(SMTP_UNAVAILABLE, result.fallbackReason());
        assertEquals(MailDelivery.Transport.EMAIL_API, result.transport());
        assertEquals(1, smtp.calls);
        assertEquals(1, api.calls);
    }

    @Test
    void uncertainOrOtherSendFailuresNeverFallBack() {
        for (MessagingException failure : new MessagingException[] {
                new MailDeliveryUncertainException(), new MessagingException("after send") }) {
            StubSender api = api(null);
            assertThrows(MessagingException.class, () -> new SmtpFirstMailSender(
                    new StubSender(null, failure), api, true).sendWelcome(user));
            assertEquals(0, api.calls);
        }
    }

    @Test
    void explicitlyDisabledSmtpIsNotAttempted() throws Exception {
        StubSender smtp = new StubSender(MailDelivery.smtp(), null);
        StubSender api = api(null);
        var sender = new SmtpFirstMailSender(smtp, api, false);
        assertEquals(SMTP_DISABLED, sender.sendWelcome(user).fallbackReason());
        assertEquals(0, smtp.calls);
        assertEquals(1, api.calls);
        assertTrue(sender.policyNotice().contains("disabled"));
    }

    @Test
    void missingSmtpConfigurationUsesApiWithoutClaimingSmtpFailure() throws Exception {
        var sender = new SmtpFirstMailSender(new DisabledMailSender(), api(null), true);
        assertEquals(SMTP_NOT_CONFIGURED, sender.sendWelcome(user).fallbackReason());
        assertTrue(sender.policyNotice().contains("not configured"));
    }

    @Test
    void neitherTransportConfiguredDoesNotClaimSending() {
        var sender = new SmtpFirstMailSender(new DisabledMailSender(), new DisabledMailSender(), true);
        assertFalse(sender.isConfigured());
        assertThrows(MessagingException.class, () -> sender.sendWelcome(user));
    }

    @Test
    void failureWithoutFallbackOrFailureOfApiIsNotHidden() {
        var smtpFailure = new SmtpUnavailableException(new MessagingException("connect"));
        assertThrows(SmtpUnavailableException.class, () -> new SmtpFirstMailSender(
                new StubSender(null, smtpFailure), new DisabledMailSender(), true).sendWelcome(user));
        StubSender api = api(new MessagingException("API rejected"));
        assertThrows(MessagingException.class, () -> new SmtpFirstMailSender(
                new StubSender(null, smtpFailure), api, true).sendWelcome(user));
        assertEquals(1, api.calls);
    }

    private StubSender api(MessagingException failure) {
        return new StubSender(new MailDelivery(MailDelivery.Transport.EMAIL_API, "Brevo", NONE), failure);
    }

    private static class StubSender implements WelcomeMailSender {
        private final MailDelivery delivery;
        private final MessagingException failure;
        private int calls;

        StubSender(MailDelivery delivery, MessagingException failure) {
            this.delivery = delivery;
            this.failure = failure;
        }

        @Override
        public boolean isConfigured() { return true; }

        @Override
        public MailDelivery sendWelcome(User user) throws MessagingException {
            calls++;
            if (failure != null) { throw failure; }
            return delivery;
        }
    }
}
