package com.kowa.batch;

import com.kowa.batch.web.JavaMailWithAttachment;
import com.kowa.batch.web.PDFExtractor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * send mail code
 * https://www.cnblogs.com/yejg1212/archive/2013/06/01/3112702.html
 * https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 * http://blog.apitore.com/2016/12/05/pdfbox-pdf-to-text/
 */
@SpringBootApplication
public class WebApplication implements CommandLineRunner {


    public static String getYearDate(){
//        Date date = new Date();
//        int year = date.getYear() + 1900;
//        int month = date.getMonth() + 1;
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMM");

        //先月
        String monthStr = String.format("%02d", new Date().getMonth());
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
        String yearString = sdf.format(new Date());

        return yearString + monthStr;
    }





    public static void main(String[] args) throws Exception {

//        SpringApplication.run(WebApplication.class, args);

//        URLClassLoader urlClassLoader = (URLClassLoader)Test.class.getClassLoader();
//        URL[] urls = urlClassLoader.getURLs();
//        for (URL u : urls) {
//            System.out.println(u.getPath());
//        }

//        File fme = new File("application.properties");
//        System.out.println(fme.exists());


        System.out.println("Working Directory = " +System.getProperty("user.dir"));
        System.out.println(Paths.get(".").toAbsolutePath().normalize().toString());
        System.out.println(WebApplication.class.getClassLoader());

        ClassPathResource classPathResource = new ClassPathResource("application.properties");

        System.out.println(IOUtils.readLines(classPathResource.getInputStream(), "UTF-8"));
        if(true){
            return;
        }


        FileInputStream fis = new FileInputStream("D:\\shikaiwen\\fenghe\\projects\\kowas\\trunk\\issoh-mail-sender\\target\\config.properties");
        InputStream userMappingStream = new FileInputStream("D:\\shikaiwen\\fenghe\\projects\\kowas\\trunk\\issoh-mail-sender\\target\\usermapping.txt");

//        InputStream userMappingStream = new FileInputStream("usermapping.txt");
//        FileInputStream fis = new FileInputStream("config.properties");


        Properties pp = new Properties();
        pp.load(new InputStreamReader(fis, Charset.forName("UTF-8")));

        String emailTitle = pp.getProperty("emailTitle");
        String emailContent = getEmailContent();
        String zipFileName = pp.getProperty("zipFileName");

        String allFolderPath = pp.getProperty("allFolderPath");

        String pdfFolder = allFolderPath + File.separator + getYearDate() + "給与分";
        System.out.println((allFolderPath + File.separator + zipFileName));
        System.out.println(pdfFolder);

        PDFExtractor.unZipIt(allFolderPath + File.separator + zipFileName, pdfFolder);

        System.out.println(" extract and rename file to folder " + pdfFolder + " completed ..");


        List<String> list = Arrays.asList(IOUtils.toString(userMappingStream, "UTF-8").split("\n"));

        List<String[]> hasFileUserList = new ArrayList<>();
        for (String mystr : list) {
            String username = mystr.trim().split(",")[0].trim();
            String email = mystr.trim().split(",")[1].trim();
            File f = new File(pdfFolder, PDFExtractor.getOutputFileName(username));
            if(!f.exists()){
//                throw new RuntimeException("file not exists :" + username + ".pdf");
                System.out.println("file not exists :" + f.getAbsolutePath());
            }else{

                String[] strs = new String[2];
                strs[0] = username;
                strs[1] = email;
                hasFileUserList.add(strs);
            }
        }

        System.out.println("file check over , existing user file count :" + hasFileUserList.size() );
        System.out.println("enter Y if you want to send email now , press other key to quit ");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        if("Y".equalsIgnoreCase(line)){

            String userEmail = "kaibun.shi@issoh.co.jp";
            File f = new File(pdfFolder, PDFExtractor.getOutputFileName("施凱文"));
            System.out.println(userEmail + ":" + emailTitle + ":" + emailContent);
            JavaMailWithAttachment.sendTest(userEmail,emailTitle,emailContent,f);


//             for (String [] strs : hasFileUserList) {
//                 System.out.print("username:"+ strs[0]);
//                 System.out.println("email:"+ strs[1]);
//                 File f = new File(pdfFolder, PDFExtractor.getOutputFileName(strs[0]));
//                JavaMailWithAttachment.sendTest(strs[1],emailTitle,emailContent,f);
//            }

            System.out.println("send over...");
        }else{
            System.out.println("good bye ");
        }

    }

    public static String getEmailContent(){

        Date date = new Date();
        // 先月
        String previousMonthStr = date.getMonth() + "";
        String currentMonthStr = (date.getMonth() + 1) + "";
        String nextDate = (date.getDate() + 1) + "";
        Map<String, String> rootMap = new HashMap<>();
        rootMap.put("previousMonthStr", previousMonthStr);
        rootMap.put("currentMonthStr", currentMonthStr);
        rootMap.put("nextDate", nextDate);


        String contentTemp = "お疲れ様です。\n" +
                "人事の徐です。\n" +
                " \n" +
                " \n" +
                "${previousMonthStr}月の給与明細を送付いたします。ご確認ください。\n" +
                " \n" +
                "問題が無ければ、${currentMonthStr}月15日より振込させて頂きます。\n" +
                "なお、計算が間違っている場合は徐のチャット迄連絡ください（${currentMonthStr}月${nextDate}日12：\n" +
                "00迄）。\n" +
                " \n" +
                "以上、宜しくお願い致します。";


        for (String key : rootMap.keySet()) {
            contentTemp = contentTemp.replace("${" + key + "}", rootMap.get(key));
        }


        return contentTemp;

    }


    @Override
    public void run(String... args) throws Exception {
//        List<BatchTrigger> allData = batchTriggerMapper.selectAllData();
//        System.out.println(allData);
//        batchTriggerMapper.updateTriggerStatusByName("trigger商品マスター取込(在庫)",Constants.STATUS_ENABLE);

    }
}

