import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
 
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
 
/**
 * 使用POP3协议解析邮件帮助类
 *https://blog.csdn.net/chen15369337607/article/details/102853697
 * @author bzs on 2018/6/28.
 */
@Slf4j
public class ParsingEmailUtil {
 
    public static void main(String[] args) throws Exception{
        resceive("**********@163.com", "*****");
    }
     /**
     * @param args
     */
    // public static void main(String[] args) {
    //     MailUtil mailUtil = new MailUtil();
    //     mailUtil.readMail();
    // }
    /**
     * 获取邮箱信息
     *
     * @param emailAdress 需要解析的邮箱地址
     * @param password    邮箱的授权密码
     * @throws Exception
     */
    public static void resceive(String emailAdress, String password) throws Exception {
 
        String port = "110";   // 端口号
        String servicePath = "pop.163.com";   // 服务器地址
 
 
        // 准备连接服务器的会话信息
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "pop3");       // 使用pop3协议
        props.setProperty("mail.pop3.port", port);           // 端口
        props.setProperty("mail.pop3.host", servicePath);       // pop3服务器
 
        // 创建Session实例对象
        Session session = Session.getInstance(props);
        Store store = session.getStore("pop3");
        store.connect(emailAdress, password); //163邮箱程序登录属于第三方登录所以这里的密码是163给的授权密码而并非普通的登录密码
 
 
        // 获得收件箱
        Folder folder = store.getFolder("INBOX");
        /* Folder.READ_ONLY：只读权限
         * Folder.READ_WRITE：可读可写（可以修改邮件的状态）
         */
        folder.open(Folder.READ_WRITE); //打开收件箱
 
//        // 由于POP3协议无法获知邮件的状态,所以getUnreadMessageCount得到的是收件箱的邮件总数
//        System.out.println("未读邮件数: " + folder.getUnreadMessageCount());
//
//        // 由于POP3协议无法获知邮件的状态,所以下面得到的结果始终都是为0
//        System.out.println("删除邮件数: " + folder.getDeletedMessageCount());
//        System.out.println("新邮件: " + folder.getNewMessageCount());
 
        // 获得收件箱中的邮件总数
        log.warn("邮件总数: {}", folder.getMessageCount());
 
        // 得到收件箱中的所有邮件,并解析
        Message[] messages = folder.getMessages();
        //解析邮件
        parseMessage(messages);
 
        //得到收件箱中的所有邮件并且删除邮件
//        deleteMessage(messages);
 
        //释放资源
        folder.close(true);
        store.close();
    }
 
    /**
     * 解析邮件
     *
     * @param messages 要解析的邮件列表
     */
    public static void parseMessage(Message... messages) throws MessagingException, IOException {
        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");
 
        // 解析所有邮件
        for (int i = 0, count = messages.length; i < count; i++) {
            MimeMessage msg = (MimeMessage) messages[i];
            log.info("------------------解析第" + msg.getMessageNumber() + "封邮件-------------------- ");
            log.warn("主题: {}" , getSubject(msg));
            log.warn("发件人: {}" , getFrom(msg));
            log.warn("收件人：{}" , getReceiveAddress(msg, null));
            log.warn("发送时间：{}" , getSentDate(msg, null));
            log.warn("是否已读：{}" , isSeen(msg));
            log.warn("邮件优先级：{}" , getPriority(msg));
            log.warn("是否需要回执：{}" , isReplySign(msg));
            log.warn("邮件大小：{}" , msg.getSize() * 1024 + "kb");
            boolean isContainerAttachment = isContainAttachment(msg);
            log.warn("是否包含附件：{}" ,isContainerAttachment);
            if (isContainerAttachment) {
                saveAttachment(msg, "d:\\log\\" + msg.getSubject() + "_" + i + "_"); //保存附件
            }
            StringBuffer content = new StringBuffer(30);
            //解析邮件正文
            getMailTextContent(msg, content);
            log.warn("邮件正文：{}" , content);
            log.info("------------------第" + msg.getMessageNumber() + "封邮件解析结束-------------------- ");
            System.out.println();
        }
    }
 
 
    /**
     * 删除邮件
     *
     * @param messages 要删除邮件列表
     */
    public static void deleteMessage(Message... messages) throws MessagingException, IOException {
        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");
 
        // 解析所有邮件
        for (int i = 0, count = messages.length; i < count; i++) {
 
            /**
             *   邮件删除
             */
            Message message = messages[i];
            String subject = message.getSubject();
            // set the DELETE flag to true
            message.setFlag(Flags.Flag.DELETED, true);
            System.out.println("Marked DELETE for message: " + subject);
 
 
        }
    }
 
    /**
     * 获得邮件主题
     *
     * @param msg 邮件内容
     * @return 解码后的邮件主题
     */
    public static String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {
        return MimeUtility.decodeText(msg.getSubject());
    }
 
    /**
     * 获得邮件发件人
     *
     * @param msg 邮件内容
     * @return 姓名 <Email地址>
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static String getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        String from = "";
        Address[] froms = msg.getFrom();
        if (froms.length < 1)
            throw new MessagingException("没有发件人!");
 
        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();
        if (person != null) {
            person = MimeUtility.decodeText(person) + " ";
        } else {
            person = "";
        }
        from = person + "<" + address.getAddress() + ">";
 
        return from;
    }
 
    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
     * <p>Message.RecipientType.TO  收件人</p>
     * <p>Message.RecipientType.CC  抄送</p>
     * <p>Message.RecipientType.BCC 密送</p>
     *
     * @param msg  邮件内容
     * @param type 收件人类型
     * @return 收件人1 <邮件地址1>, 收件人2 <邮件地址2>, ...
     * @throws MessagingException
     */
    public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
        StringBuffer receiveAddress = new StringBuffer();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }
 
        if (addresss == null || addresss.length < 1)
            throw new MessagingException("没有收件人!");
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress) address;
            receiveAddress.append(internetAddress.toUnicodeString()).append(",");
        }
 
        receiveAddress.deleteCharAt(receiveAddress.length() - 1); //删除最后一个逗号
 
        return receiveAddress.toString();
    }
 
    /**
     * 获得邮件发送时间
     *
     * @param msg 邮件内容
     * @return yyyy年mm月dd日 星期X HH:mm
     * @throws MessagingException
     */
    public static String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null)
            return "";
 
        if (pattern == null || "".equals(pattern))
            pattern = "yyyy年MM月dd日 E HH:mm ";
 
        return new SimpleDateFormat(pattern).format(receivedDate);
    }
 
    /**
     * 判断邮件中是否包含附件
     *
     * @param part 邮件内容
     * @return 邮件中存在附件返回true，不存在返回false
     * @throws MessagingException
     * @throws IOException
     */
    public static boolean isContainAttachment(Part part) throws MessagingException, IOException {
        boolean flag = false;
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*")) {
                    flag = isContainAttachment(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("application") != -1) {
                        flag = true;
                    }
 
                    if (contentType.indexOf("name") != -1) {
                        flag = true;
                    }
                }
 
                if (flag) break;
            }
        } else if (part.isMimeType("message/rfc822")) {
            flag = isContainAttachment((Part) part.getContent());
        }
        return flag;
    }
 
    /**
     * 判断邮件是否已读
     *
     * @param msg 邮件内容
     * @return 如果邮件已读返回true, 否则返回false
     * @throws MessagingException
     */
    public static boolean isSeen(MimeMessage msg) throws MessagingException {
        return msg.getFlags().contains(Flags.Flag.SEEN);
    }
 
    /**
     * 判断邮件是否需要阅读回执
     *
     * @param msg 邮件内容
     * @return 需要回执返回true, 否则返回false
     * @throws MessagingException
     */
    public static boolean isReplySign(MimeMessage msg) throws MessagingException {
        boolean replySign = false;
        String[] headers = msg.getHeader("Disposition-Notification-To");
        if (headers != null)
            replySign = true;
        return replySign;
    }
 
    /**
     * 获得邮件的优先级
     *
     * @param msg 邮件内容
     * @return 1(High):紧急  3:普通(Normal)  5:低(Low)
     * @throws MessagingException
     */
    public static String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "普通";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.indexOf("1") != -1 || headerPriority.indexOf("High") != -1)
                priority = "紧急";
            else if (headerPriority.indexOf("5") != -1 || headerPriority.indexOf("Low") != -1)
                priority = "低";
            else
                priority = "普通";
        }
        return priority;
    }
 
    /**
     * 获得邮件文本内容
     *
     * @param part    邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws MessagingException
     * @throws IOException
     */
    public static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }
 
    /**
     * 保存附件
     *
     * @param part    邮件中多个组合体中的其中一个组合体
     * @param destDir 附件保存目录
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveAttachment(Part part, String destDir) throws MessagingException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();    //复杂体邮件
            //复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                //获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                //某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    InputStream is = bodyPart.getInputStream();
                    saveFile(is, destDir, decodeText(bodyPart.getFileName()));
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveAttachment(bodyPart, destDir);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
                        saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()));
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachment((Part) part.getContent(), destDir);
        }
    }
 
    /**
     * 读取输入流中的数据保存至指定目录
     *
     * @param is       输入流
     * @param fileName 文件名
     * @param destDir  文件存储目录
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void saveFile(InputStream is, String destDir, String fileName) throws FileNotFoundException, IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(new File(destDir + fileName)));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }
 
    /**
     * 文本解码
     *
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本
     * @return 解码后的文本
     * @throws UnsupportedEncodingException
     */
    public static String decodeText(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }
 

    public void readMail(){
        try {
            // 1. 设置连接信息, 生成一个 Session
            Properties props = new Properties();
            // 用空的 Properties也行这里
            props.setProperty("mail.smtp.host", "smtp.qq.com");
            props.setProperty("mail.smtp.auth", "true");
            // 解决邮件下载慢的问题，因为我邮件下载速度很快，所以没有验证这段代码
            //props.setProperty("mail.imap.partialfetch", "false");
            //props.setProperty("mail.imaps.partialfetch", "false");
            
            Session session = Session.getDefaultInstance(props);
            // 2. 获取 Store 并连接到服务器	
            // 设置连接相关信息，file 可以直接指定邮箱的某个文件夹
            // 邮件在邮件服务器上是在每个用户名下，设置不同的文件，收件箱默认为“INBOX”
            // URLName urlname = new URLName("pop3","pop.qq.com",110,"INBOX","youremail@foxmail.com","你的授权码");
            URLName urlname = new URLName("pop3","pop.qq.com",110,null,"youremil@foxmail.com","授权码");
            Store store = session.getStore(urlname);
            store.connect();
            // 默认父目录
            Folder folder = store.getDefaultFolder();
            if (folder == null) {
                System.out.println("服务器不可用");
                return;
            }
            String defaultFolderName = folder.getName();
            System.out.println("默认信箱名:" + defaultFolderName);
            // 默认目录列表
            Folder[] folders = folder.list();
            System.out.println("默认目录下的子目录数: " + folders.length);
            System.out.println("-----根目录下的包含的文件名-----");
           
            for(int i = 0; i < folders.length; i++) {
               System.out.println("【目录下的文件名】：【" + folders[i].getFullName() + "】");
            }
            System.out.println("---------------");
           
            // 获取收件箱
            Folder popFolder = folder.getFolder("INBOX");
            
            // 可读邮件,可以删邮件的模式打开目录
            popFolder.open(Folder.HOLDS_FOLDERS);
            // 4. 列出来收件箱 下所有邮件
            Message[] messages = popFolder.getMessages();
            // 取出来邮件数
            int msgCount = popFolder.getMessageCount();
            System.out.println("共有邮件: " + msgCount + "封");
            // FetchProfile fProfile = new FetchProfile();// 选择邮件的下载模式,
            // 根据网速选择不同的模式
            // fProfile.add(FetchProfile.Item.ENVELOPE);
            // folder.fetch(messages, fProfile);// 选择性的下载邮件
            // 5. 循环处理每个邮件并实现邮件转为新闻的功能
            for (int i = 0; i < msgCount; i++) {
                // 单个邮件
                System.out.println("第" + i +"封邮件开始");
                mailReceiver(messages[i]);
                System.out.println("第" + i +"封邮件结束");
                //邮件读取用来校验
                messages[i].writeTo(new FileOutputStream("D:/pop3MailReceiver"+ i +".eml"));
            }
            // 7. 关闭 Folder 会真正删除邮件, false 不删除
            popFolder.close(false);
            // 8. 关闭 store, 断开网络连接
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析邮件
     */
    private void mailReceiver(Message msg)throws Exception{
        System.out.println("【邮件ID】:【" + msg.getMessageNumber() + "】");
        System.out.println("【邮件主题】:【" + MimeUtility.decodeText(msg.getSubject()) + "】");
        System.out.println("【邮件发送时间】:【" + this.getSentDate(msg) + "】");
        // 发件人信息
        Address[] froms = msg.getFrom();
        if(froms != null) {
            InternetAddress addr = (InternetAddress)froms[0];
            System.out.println("【发件人地址】:【" + addr.getAddress() + "】");
            System.out.println("【发件人显示名】:【" + addr.getPersonal() + "】");
        }
       // System.out.println("【收件人】:【" + msg.getRecipients(Message.RecipientType.TO) + "】");
        System.out.println("【是否需要回复】:【" + this.needReplyState(msg) + "】");
        System.out.println("【是否需要回复】:【" + this.isNew(msg) + "】");
        // getContent() 是获取包裹内容, Part相当于外包装
        Object o = msg.getContent();
        if(o instanceof Multipart) {
            Multipart multipart = (Multipart) o ;
            reMultipart(multipart);
        } else if (o instanceof Part){
            Part part = (Part) o;
            rePart(part);
        } else {
            System.out.println("【邮件内容类型】:【" + msg.getContentType() + "】");
            System.out.println("【内容】:【" + msg.getContent() + "】");
        }
    }

    /**
     * @param part 解析内容
     * @throws Exception
     */
    private void rePart(Part part) throws MessagingException, IOException {
        if (part.getDisposition() != null) {

            String strFileNmae = MimeUtility.decodeText(part.getFileName());
            //MimeUtility.decodeText解决附件名乱码问题
            System.out.println("【发现附件】:【 " +  MimeUtility.decodeText(part.getFileName()) + "】");
            System.out.println("【内容类型】:【 " + MimeUtility.decodeText(part.getContentType()) + "】");
            System.out.println("【附件内容】:【" + part.getContent() + "】");
            InputStream in = part.getInputStream();
            // 打开附件的输入流
            // 读取附件字节并存储到文件中
            java.io.FileOutputStream out = new FileOutputStream(strFileNmae);
            int data;
            while((data = in.read()) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } /*else {
            if(part.getContentType().startsWith("text/plain")) {
                System.out.println("文本内容：" + part.getContent());
            } else {
                //System.out.println("HTML内容：" + part.getContent());
            }
        }*/
    }

    /**
     * @param multipart // 接卸包裹（含所有邮件内容(包裹+正文+附件)）
     * @throws Exception
     */
    private void reMultipart(Multipart multipart) throws Exception {
        //System.out.println("邮件共有" + multipart.getCount() + "部分组成");
        // 依次处理各个部分
        for (int j = 0, n = multipart.getCount(); j < n; j++) {
            //System.out.println("处理第" + j + "部分");
            Part part = multipart.getBodyPart(j);//解包, 取出 MultiPart的各个部分, 每部分可能是邮件内容,
            // 也可能是另一个小包裹(MultipPart)
            // 判断此包裹内容是不是一个小包裹, 一般这一部分是 正文 Content-Type: multipart/alternative
            if (part.getContent() instanceof Multipart) {
                Multipart p = (Multipart) part.getContent();// 转成小包裹
                //递归迭代
                reMultipart(p);
            } else {
                rePart(part);
            }
        }
    }

    /**
     *获取邮件日期 
     */
    private String getSentDate(Message message){
        String emailSendDate = "邮件发送的时间出错";
        try {
            Date date = message.getSentDate();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            emailSendDate = format.format(date);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return  emailSendDate;
    }
    
    public String needReplyState(Message message) throws MessagingException {
        String replyFlag = "此邮件不需要回复";
        String [] needReply = message.getHeader("Disposition-Notification-To");
        if (null != needReply){
            replyFlag = "邮件需要回复";
        }
        return replyFlag;
    }
    
    public String isNew(Message message) throws MessagingException {
        Flags flags = message.getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i<flag.length;i++){
            if (Flags.Flag.SEEN == flag[i]){
                return "邮件未读";
            }
        }
        return  "邮件已读";
    }
   




 
}
