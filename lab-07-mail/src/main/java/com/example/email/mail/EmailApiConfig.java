package com.example.email.mail;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record EmailApiConfig(Provider provider, String apiKey, String from) {
    public enum Provider {
        BREVO("Brevo", "https://api.brevo.com/v3/smtp/email"),
        RESEND("Resend", "https://api.resend.com/emails");

        private final String displayName;
        private final URI endpoint;

        Provider(String displayName, String endpoint) {
            this.displayName = displayName;
            this.endpoint = URI.create(endpoint);
        }

        public String displayName() { return displayName; }
        public URI endpoint() { return endpoint; }
    }

    public EmailApiConfig {
        if (provider == null) {
            throw new IllegalArgumentException("EMAIL_API_PROVIDER must be brevo or resend.");
        }
        if (apiKey == null || apiKey.isBlank()
                || apiKey.chars().anyMatch(c -> c < 33 || c > 126)) {
            throw new IllegalArgumentException("EMAIL_API_KEY must be a non-empty API key without whitespace.");
        }
        try {
            if (from == null || from.indexOf('\r') >= 0 || from.indexOf('\n') >= 0) {
                throw new AddressException("Invalid sender");
            }
            InternetAddress address = new InternetAddress(from, true);
            address.validate();
            if (address.getPersonal() != null || !address.getAddress().equals(from) || !from.contains("@")) {
                throw new AddressException("Expected a single email address");
            }
        } catch (AddressException e) {
            throw new IllegalArgumentException("EMAIL_API_FROM must be a single valid email address.");
        }
    }

    public static Optional<EmailApiConfig> fromEnvironment(Map<String, String> environment) {
        String provider = environment.getOrDefault("EMAIL_API_PROVIDER", "").trim();
        String key = environment.getOrDefault("EMAIL_API_KEY", "").trim();
        String from = environment.getOrDefault("EMAIL_API_FROM", "").trim();
        if (provider.isEmpty() && key.isEmpty() && from.isEmpty()) {
            return Optional.empty();
        }
        Provider parsed;
        try {
            parsed = Provider.valueOf(provider.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("EMAIL_API_PROVIDER must be brevo or resend.");
        }
        return Optional.of(new EmailApiConfig(parsed, key, from));
    }

    @Override
    public String toString() {
        return "EmailApiConfig[provider=" + provider + ", credentials=REDACTED]";
    }
}
