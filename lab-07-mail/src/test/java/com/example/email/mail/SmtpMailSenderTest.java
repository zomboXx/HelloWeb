package com.example.email.mail;

import com.example.email.model.User;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SmtpMailSenderTest {
    private GreenMail server;

    @BeforeEach
    void startSandboxSmtp() {
        server = new GreenMail(new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
        server.setUser("welcome@example.test", "smtp-user", "test-password");
        server.setUser("student@example.test", "student", "test-password");
        server.start();
    }

    @AfterEach
    void stopSandboxSmtp() {
        server.stop();
    }

    @Test
    void sendsAuthenticatedUtf8WelcomeMessage() throws Exception {
        SmtpConfig config = new SmtpConfig("127.0.0.1", server.getSmtp().getPort(), "none",
                "smtp-user", "test-password", "welcome@example.test");
        new SmtpMailSender(config).sendWelcome(new User("student@example.test", "Đức", "Phát"));

        assertTrue(server.waitForIncomingEmail(3000, 1));
        MimeMessage message = server.getReceivedMessages()[0];
        assertEquals("Welcome to our email list", message.getSubject());
        assertEquals("welcome@example.test", message.getFrom()[0].toString());
        assertEquals("student@example.test", message.getAllRecipients()[0].toString());
        assertTrue(message.isMimeType("text/plain"));
        assertTrue(message.getContent().toString().contains("Dear Đức,"));
        assertTrue(message.getContent().toString().contains("Thanks for joining our email list."));
    }

    @Test
    void supportsLocalSmtpWithoutAuthentication() throws Exception {
        SmtpConfig config = new SmtpConfig("127.0.0.1", server.getSmtp().getPort(), "none",
                "", "", "welcome@example.test");
        new SmtpMailSender(config).sendWelcome(new User("student@example.test", "John", "Smith"));
        assertTrue(server.waitForIncomingEmail(3000, 1));
        assertEquals(1, server.getReceivedMessages().length);
    }

    @Test
    void doesNotDowngradeRequiredTlsToPlainText() {
        SmtpConfig config = new SmtpConfig("127.0.0.1", server.getSmtp().getPort(), "starttls",
                "", "", "welcome@example.test");
        assertThrows(MessagingException.class,
                () -> new SmtpMailSender(config).sendWelcome(new User("student@example.test", "John", "Smith")));
        assertEquals(0, server.getReceivedMessages().length);
    }
}
