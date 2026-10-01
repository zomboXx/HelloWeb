package com.example.email.mail;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailApiConfigTest {
    @Test
    void noApiSettingsMeansNotConfigured() {
        assertTrue(EmailApiConfig.fromEnvironment(Map.of()).isEmpty());
    }

    @Test
    void parsesProviderAndKeepsSecretOutOfToString() {
        EmailApiConfig config = EmailApiConfig.fromEnvironment(Map.of(
                "EMAIL_API_PROVIDER", "Brevo", "EMAIL_API_KEY", "secret-key",
                "EMAIL_API_FROM", "sender@example.test")).orElseThrow();
        assertEquals(EmailApiConfig.Provider.BREVO, config.provider());
        assertFalse(config.toString().contains("secret-key"));
        assertTrue(config.toString().contains("REDACTED"));
        assertEquals("https", config.provider().endpoint().getScheme());
    }

    @Test
    void rejectsPartialAndUnsupportedConfigurationWithoutLeakingValues() {
        assertThrows(IllegalArgumentException.class,
                () -> EmailApiConfig.fromEnvironment(Map.of("EMAIL_API_PROVIDER", "resend")));
        var error = assertThrows(IllegalArgumentException.class,
                () -> EmailApiConfig.fromEnvironment(Map.of("EMAIL_API_PROVIDER", "secret-invalid-value")));
        assertFalse(error.getMessage().contains("secret-invalid-value"));
    }

    @Test
    void rejectsUnsafeHeaderAndSenderValues() {
        assertThrows(IllegalArgumentException.class, () -> new EmailApiConfig(
                EmailApiConfig.Provider.RESEND, "key\r\nother: value", "sender@example.test"));
        assertThrows(IllegalArgumentException.class, () -> new EmailApiConfig(
                EmailApiConfig.Provider.RESEND, "key", "sender@example.test\r\nBcc: other@example.test"));
        assertThrows(IllegalArgumentException.class, () -> new EmailApiConfig(
                EmailApiConfig.Provider.BREVO, "key", "Name <sender@example.test>"));
    }

    @Test
    void factoryRequiresExplicitBooleanAndCanDisableSmtpWithoutParsingItsSettings() {
        assertThrows(IllegalArgumentException.class,
                () -> MailSenders.fromEnvironment(Map.of("SMTP_ENABLED", "maybe")));
        var sender = MailSenders.fromEnvironment(Map.of("SMTP_ENABLED", "false", "SMTP_HOST", "invalid host"));
        assertFalse(sender.isConfigured());
        assertTrue(sender.policyNotice().contains("not configured"));
    }

    @Test
    void factorySupportsApiOnlyWithHonestNotice() {
        var sender = MailSenders.fromEnvironment(Map.of("SMTP_ENABLED", "false",
                "EMAIL_API_PROVIDER", "resend", "EMAIL_API_KEY", "test-secret",
                "EMAIL_API_FROM", "sender@example.test"));
        assertTrue(sender.isConfigured());
        assertTrue(sender.policyNotice().contains("disabled on this host"));
        assertFalse(sender.policyNotice().contains("test-secret"));
    }
}
