package com.kowa.batch.web;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

/**
 * attachment file name encoding problem :https://stackoverflow.com/questions/31799960/java-mail-mimeutility-encodetext-unsupportedencodingexception-base64
 */

public class JavaMailWithAttachment {
    private MimeMessage message;
    private Session session;
    private Transport transport;

    static final String smtpHost= "smtp.issoh.co.jp";
    static final int port = 587;

    static final String email_from  = "ken.jyo@issoh.co.jp";
    static final String username  = "ken.jyo@issoh.co.jp";
    static final String password = "q#Gh6Ufx";

//    public static void main(String[] args) {
//        JavaMailWithAttachment se = new JavaMailWithAttachment(true);
//        File affix = new File("d:/1809J_1.pdf");
//        se.doSendHtmlEmail("邮件主题", "邮件内容", "shikaiwenchina@gmail.com", affix);//
//    }

    static JavaMailWithAttachment se = new JavaMailWithAttachment(false);

    public static void sendTest(String toUserEmail,String title,String content,File file) {
        se.doSendHtmlEmail(toUserEmail,title, content, file);
    }

    /*
     * 初始化方法
     */
    public JavaMailWithAttachment(boolean debug) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", port);

        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });


        session.setDebug(debug);// 开启后有调试信息
        message = new MimeMessage(session);
    }

    /**
     * 发送邮件
     *
     * @param subject
     *            邮件主题
     * @param sendHtml
     *            邮件内容
     * @param receiveUser
     *            收件人地址
     * @param attachment
     *            附件
     */
    public void doSendHtmlEmail(String receiveUser, String subject, String sendHtml, File attachment) {
        try {
            // 发件人
            InternetAddress from = new InternetAddress(email_from);
            message.setFrom(from);

            // 收件人
            InternetAddress to = new InternetAddress(receiveUser);
            message.setRecipient(Message.RecipientType.TO, to);

            // 邮件主题
            message.setSubject(subject,"UTF-8");

            // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            Multipart multipart = new MimeMultipart();

            // 添加邮件正文
            BodyPart contentPart = new MimeBodyPart();

            contentPart.setContent(sendHtml, "text/html;charset=UTF-8");
            multipart.addBodyPart(contentPart);

            // 添加附件的内容
            if (attachment != null) {
                BodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment);
                attachmentBodyPart.setDataHandler(new DataHandler(source));

                // 网上流传的解决文件名乱码的方法，其实用MimeUtility.encodeWord就可以很方便的搞定
                // 这里很重要，通过下面的Base64编码的转换可以保证你的中文附件标题名在发送时不会变成乱码
                //sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
                //messageBodyPart.setFileName("=?GBK?B?" + enc.encode(attachment.getName().getBytes()) + "?=");

                //MimeUtility.encodeWord可以避免文件名乱码
                attachmentBodyPart.setFileName(MimeUtility.encodeWord(attachment.getName(),"UTF-8","B"));
                multipart.addBodyPart(attachmentBodyPart);
            }

            // 将multipart对象放到message中
            message.setContent(multipart);
            // 保存邮件
            message.saveChanges();

            transport = session.getTransport("smtp");
            // smtp验证，就是你用来发邮件的邮箱用户名密码
            transport.connect(smtpHost, email_from, password);
            // 发送
            transport.sendMessage(message, message.getAllRecipients());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}

