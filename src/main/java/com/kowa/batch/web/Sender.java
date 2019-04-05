package com.kowa.batch.web;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Sender {


//    static final String smtpHost= "smtp.126.com";
//    static final String username = "xx@126.com";
//    static final String password = "xxxxxxx";
//    static final String email_from = "xx";
//    static final String email_to = "cccc.gmail.com";


//    static final String smtpHost= "smtp.yeah.net";
    static final String smtpHost= "smtp.issoh.co.jp";
    static final int port = 587;

    static final String email_from  = "ken.jyo@issoh.co.jp";
    static final String username  = "ken.jyo@issoh.co.jp";


    static final String password = "q#Gh6Ufx";
//    static final String password = "q1w2e3";

//    static final String email_to = "cccc.gmail.com";

    public static void main(String[] args) {


        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email_from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("googlevsbing@126.com"));
            message.setSubject(" Subject");
            message.setText("Dear Mail Crawler,"
                    + "\n\n No spam to my email, please!");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }





    public static void sendWithTssl() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost );
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email_from));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("shikaiwenchina@gmail.com"));

            message.setSubject("Subject");
            message.setText("Dear Mail Crawler,"
                    + "\n\n No spam to my email, please!");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    public static void sendWithSSL() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username,password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email_from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(""));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler," +
                    "\n\n No spam to my email, please!");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
