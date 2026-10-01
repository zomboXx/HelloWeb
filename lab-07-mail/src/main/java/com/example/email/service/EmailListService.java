package com.example.email.service;

import com.example.email.data.UserRepository;
import com.example.email.mail.WelcomeMailSender;
import com.example.email.mail.MailDelivery;
import com.example.email.mail.MailDeliveryUncertainException;
import com.example.email.model.User;
import jakarta.mail.MessagingException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.email.service.RegistrationResult.MailStatus.*;

public class EmailListService {
    private static final Logger LOGGER = Logger.getLogger(EmailListService.class.getName());
    private final UserRepository users;
    private final WelcomeMailSender mail;

    public EmailListService(UserRepository users, WelcomeMailSender mail) {
        this.users = users;
        this.mail = mail;
    }

    public RegistrationResult register(User user) {
        if (!users.insertIfAbsent(user)) {
            return new RegistrationResult(user, DUPLICATE);
        }
        // Commit first: neither SMTP nor HTTPS sending is part of a JPA transaction.
        if (!mail.isConfigured()) {
            return new RegistrationResult(user, NOT_CONFIGURED);
        }
        try {
            MailDelivery delivery = mail.sendWelcome(user);
            return new RegistrationResult(user,
                    delivery.transport() == MailDelivery.Transport.SMTP ? ACCEPTED_BY_SMTP : ACCEPTED_BY_API,
                    delivery);
        } catch (MessagingException e) {
            // Do not log credentials, the recipient, or the complete message body.
            LOGGER.log(Level.WARNING, "Welcome email failed ({0}); registration remains saved.",
                    e.getClass().getSimpleName());
            return new RegistrationResult(user, e instanceof MailDeliveryUncertainException ? UNCONFIRMED : FAILED);
        }
    }
}
