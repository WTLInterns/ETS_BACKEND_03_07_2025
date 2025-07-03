package com.example.demo.Service;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public boolean sendEmail(String message, String subject, String to) {
        boolean f = false;
        String from = "contactwtltourism@gmail.com"; 
        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", 465); 
        properties.put("mail.smtp.ssl.enable", true);
        properties.put("mail.smtp.auth", true);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("contactwtltourism@gmail.com", "wgxt suje ufqw ahje"); // Replace with
                                                                                                     
            }

        });

        session.setDebug(true);

        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setFrom(from);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(message, "text/html"); 

            Transport.send(mimeMessage);
            f = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }
}