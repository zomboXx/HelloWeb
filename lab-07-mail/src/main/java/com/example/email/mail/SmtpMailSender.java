package com.example.email.mail;

import com.example.email.model.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

public class SmtpMailSender implements WelcomeMailSender {
    private static final Logger LOGGER = Logger.getLogger(SmtpMailSender.class.getName());
    private final SmtpConfig config;
    private final Session session;

    public SmtpMailSender(SmtpConfig config) {
        this.config = config;
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", config.host());
        properties.setProperty("mail.smtp.port", Integer.toString(config.port()));
        properties.setProperty("mail.smtp.auth", Boolean.toString(config.authenticationRequired()));
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(config.security().equals("starttls")));
        properties.setProperty("mail.smtp.starttls.required", Boolean.toString(config.security().equals("starttls")));
        properties.setProperty("mail.smtp.ssl.enable", Boolean.toString(config.security().equals("ssl")));
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        properties.setProperty("mail.smtp.connectiontimeout", "5000");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.writetimeout", "5000");
        // Each application owns its settings. Never enable SMTP debug with credentials.
        session = Session.getInstance(properties);
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public MailDelivery sendWelcome(User user) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.from(), true));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail(), true));
        message.setSubject(WelcomeMessage.SUBJECT, "UTF-8");
        message.setText(WelcomeMessage.text(user), "UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        Transport transport = session.getTransport("smtp");
        try {
            try {
                if (config.authenticationRequired()) {
                    transport.connect(config.host(), config.port(), config.username(), config.password());
                } else {
                    transport.connect(config.host(), config.port(), null, null);
                }
            } catch (MessagingException e) {
                throw new SmtpUnavailableException(e);
            }
            try {
                transport.sendMessage(message, message.getAllRecipients());
            } catch (MessagingException e) {
                // The server might already have accepted DATA before the connection broke.
                throw new MailDeliveryUncertainException();
            }
            return MailDelivery.smtp();
        } finally {
            try {
                transport.close();
            } catch (MessagingException e) {
                // A QUIT/close failure must not change a confirmed send into a retry.
                LOGGER.warning("SMTP connection cleanup failed; no resend was attempted.");
            }
        }
    }
}
