package com.example.email.mail;

import java.util.Map;

public final class MailSenders {
    private MailSenders() { }

    public static SmtpFirstMailSender fromEnvironment(Map<String, String> environment) {
        String enabled = environment.getOrDefault("SMTP_ENABLED", "true").trim();
        if (!enabled.equalsIgnoreCase("true") && !enabled.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("SMTP_ENABLED must be true or false.");
        }
        boolean smtpEnabled = Boolean.parseBoolean(enabled);
        WelcomeMailSender smtp = smtpEnabled ? SmtpConfig.fromEnvironment(environment)
                .<WelcomeMailSender>map(SmtpMailSender::new).orElseGet(DisabledMailSender::new)
                : new DisabledMailSender();
        WelcomeMailSender api = EmailApiConfig.fromEnvironment(environment)
                .<WelcomeMailSender>map(HttpApiMailSender::new).orElseGet(DisabledMailSender::new);
        return new SmtpFirstMailSender(smtp, api, smtpEnabled);
    }
}
