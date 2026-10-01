package com.example.email.mail;

import com.example.email.model.User;

public class DisabledMailSender implements WelcomeMailSender {
    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public MailDelivery sendWelcome(User user) {
        throw new IllegalStateException("Email sending is not configured.");
    }
}
