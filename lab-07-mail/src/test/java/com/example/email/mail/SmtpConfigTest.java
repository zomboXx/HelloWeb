package com.example.email.mail;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SmtpConfigTest {
    @Test
    void noConfigurationDisablesMail() {
        assertTrue(SmtpConfig.fromEnvironment(Map.of()).isEmpty());
    }

    @Test
    void defaultsToRequiredStartTls() {
        SmtpConfig config = SmtpConfig.fromEnvironment(Map.of(
                "SMTP_HOST", "smtp.example.test", "SMTP_FROM", "sender@example.test")).orElseThrow();
        assertEquals("starttls", config.security());
        assertEquals(587, config.port());
        assertFalse(config.authenticationRequired());
    }

    @Test
    void sslDefaultsTo465() {
        SmtpConfig config = SmtpConfig.fromEnvironment(Map.of(
                "SMTP_HOST", "smtp.example.test", "SMTP_FROM", "sender@example.test",
                "SMTP_SECURITY", "ssl")).orElseThrow();
        assertEquals(465, config.port());
    }

    @Test
    void rejectsIncompleteAndInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> SmtpConfig.fromEnvironment(Map.of("SMTP_HOST", "smtp.example.test")));
        assertThrows(IllegalArgumentException.class,
                () -> new SmtpConfig("smtp.example.test", 70000, "starttls", "", "", "sender@example.test"));
        assertThrows(IllegalArgumentException.class,
                () -> new SmtpConfig("smtp.example.test", 587, "starttls", "user", "", "sender@example.test"));
        assertThrows(IllegalArgumentException.class,
                () -> new SmtpConfig("smtp.example.test", 587, "starttls", "", "secret", "sender@example.test"));
        assertThrows(IllegalArgumentException.class,
                () -> new SmtpConfig("localhost", 1025, "none", "", "", "bad\r\nBcc: a@example.test"));
    }

    @Test
    void onlyLoopbackCanUsePlainSmtp() {
        assertThrows(IllegalArgumentException.class,
                () -> new SmtpConfig("smtp.example.test", 25, "none", "", "", "sender@example.test"));
        assertDoesNotThrow(() -> new SmtpConfig("127.0.0.1", 1025, "none", "", "", "sender@example.test"));
    }

    @Test
    void configurationNeverPrintsThePassword() {
        SmtpConfig config = new SmtpConfig("localhost", 1025, "none", "user", "dummy-secret", "sender@example.test");
        assertFalse(config.toString().contains("dummy-secret"));
    }
}
