package webserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Service
public class MailService {

    private Properties prop;
    private Session session;
    @Value("${domain}")
    private String domain;

    public MailService(@Value("${mail.smtp.host}") String host,
                       @Value("${mail.smtp.port}") int port,
                       @Value("${mail.smtp.ssl.trust}") String ssl,
                       @Value("${mail.credentials.username}") String username,
                       @Value("${mail.credentials.password}") String password) {

        prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        prop.put("mail.smtp.ssl.trust", ssl);

        session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

    }

    public boolean sendMessage(String senderName, String recipient, String subject, String content) {

        try {
            InternetAddress fromAddress = new InternetAddress(senderName + "@" + domain);
            Message message = new MimeMessage(session);
            message.setFrom(fromAddress);
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(content, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return true;

    }
}
