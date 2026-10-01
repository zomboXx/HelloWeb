package com.example.email.mail;

import com.example.email.model.User;
import jakarta.mail.MessagingException;

import static com.example.email.mail.MailDelivery.FallbackReason.*;

/** SMTP first, HTTPS only when SMTP is unavailable before the send begins. */
public class SmtpFirstMailSender implements WelcomeMailSender {
    private final WelcomeMailSender smtp;
    private final WelcomeMailSender api;
    private final boolean smtpEnabled;

    public SmtpFirstMailSender(WelcomeMailSender smtp, WelcomeMailSender api, boolean smtpEnabled) {
        this.smtp = smtp;
        this.api = api;
        this.smtpEnabled = smtpEnabled;
    }

    @Override
    public boolean isConfigured() {
        return (smtpEnabled && smtp.isConfigured()) || api.isConfigured();
    }

    @Override
    public MailDelivery sendWelcome(User user) throws MessagingException {
        if (smtpEnabled && smtp.isConfigured()) {
            try {
                return smtp.sendWelcome(user);
            } catch (SmtpUnavailableException e) {
                if (!api.isConfigured()) {
                    throw e;
                }
                return api.sendWelcome(user).withFallback(SMTP_UNAVAILABLE);
            }
        }
        if (!api.isConfigured()) {
            throw new MessagingException("No email transport is configured.");
        }
        return api.sendWelcome(user).withFallback(smtpEnabled ? SMTP_NOT_CONFIGURED : SMTP_DISABLED);
    }

    public String policyNotice() {
        if (!isConfigured()) {
            return "Email sending is not configured. Registration works, but no welcome email will be sent.";
        }
        if (!smtpEnabled) {
            return "SMTP is preferred, but disabled on this host. Welcome emails use an HTTPS email API instead.";
        }
        if (!smtp.isConfigured()) {
            return "SMTP is preferred, but not configured. Welcome emails use an HTTPS email API instead.";
        }
        if (api.isConfigured()) {
            return "SMTP is preferred. If its connection cannot be established, an HTTPS email API is used instead.";
        }
        return "Welcome emails use SMTP. No HTTPS email API fallback is configured.";
    }
}
