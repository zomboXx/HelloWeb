package com.example.email.mail;

import com.example.email.model.User;
import jakarta.mail.MessagingException;

public interface WelcomeMailSender {
    boolean isConfigured();

    MailDelivery sendWelcome(User user) throws MessagingException;
}
