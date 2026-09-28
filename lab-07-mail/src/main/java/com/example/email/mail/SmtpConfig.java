package com.example.email.mail;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record SmtpConfig(String host, int port, String security,
                         String username, String password, String from) {
    public SmtpConfig {
        if (host == null || host.isBlank() || host.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("SMTP_HOST must be a hostname.");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("SMTP_PORT must be between 1 and 65535.");
        }
        if (!java.util.Set.of("starttls", "ssl", "none").contains(security)) {
            throw new IllegalArgumentException("SMTP_SECURITY must be starttls, ssl, or none.");
        }
        if (security.equals("none") && !java.util.Set.of("localhost", "127.0.0.1", "::1").contains(host)) {
            throw new IllegalArgumentException("Unencrypted SMTP is only allowed on loopback for local testing.");
        }
        if (username == null || password == null || username.isBlank() != password.isEmpty()) {
            throw new IllegalArgumentException("Set both SMTP_USERNAME and SMTP_PASSWORD, or neither.");
        }
        try {
            if (from == null || from.indexOf('\r') >= 0 || from.indexOf('\n') >= 0) {
                throw new AddressException("Invalid sender");
            }
            new InternetAddress(from, true).validate();
        } catch (AddressException e) {
            throw new IllegalArgumentException("SMTP_FROM must be a valid email address.", e);
        }
    }

    public boolean authenticationRequired() {
        return !username.isBlank();
    }

    @Override
    public String toString() {
        return "SmtpConfig[host=" + host + ", port=" + port + ", security=" + security
                + ", credentials=REDACTED]";
    }

    public static Optional<SmtpConfig> fromEnvironment(Map<String, String> environment) {
        String host = value(environment, "SMTP_HOST");
        String from = value(environment, "SMTP_FROM");
        if (host.isEmpty() && from.isEmpty()) {
            return Optional.empty();
        }
        String security = environment.getOrDefault("SMTP_SECURITY", "starttls").trim().toLowerCase(Locale.ROOT);
        String defaultPort = security.equals("ssl") ? "465" : "587";
        int port;
        try {
            port = Integer.parseInt(environment.getOrDefault("SMTP_PORT", defaultPort).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("SMTP_PORT must be a number.", e);
        }
        return Optional.of(new SmtpConfig(host, port, security, value(environment, "SMTP_USERNAME"),
                environment.getOrDefault("SMTP_PASSWORD", ""), from));
    }

    private static String value(Map<String, String> environment, String name) {
        return environment.getOrDefault(name, "").trim();
    }
}
