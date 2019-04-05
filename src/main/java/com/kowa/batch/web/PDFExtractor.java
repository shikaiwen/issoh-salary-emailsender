package com.kowa.batch.web;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PDFExtractor {


    public static void main(String[] args) {
        unZipIt("D:\\shikaiwen\\tmp\\salar.zip","C:\\Users\\issoh\\Documents\\WeChat Files\\kevin396401\\FileStorage\\File\\2019-04\\給料out");
    }

    public static void unZipIt(String zipFile,String descDir){

        new File(descDir).mkdirs();
//        File zz = new File(zipFile);

//        String OUTPUT_FOLDER = zz.getPath().replace(".zip","");
//        String OUTPUT_FOLDER = "D:\\shikaiwen\\tmp\\ma";
        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
//            File folder = new File(OUTPUT_FOLDER);
//            if(!folder.exists()){
//                folder.mkdir();
//            }

            //get the zip file content

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile),Charset.forName("UTF-8"));

            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

//                String fileName = ze.getName();
//                File newFile = new File(OUTPUT_FOLDER + File.separator + fileName);
//                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
//                new File(newFile.getParent()).mkdirs();
                if (ze.isDirectory()) {
//                    newFile.mkdir();
                }else{
//                    FileOutputStream fos = new FileOutputStream(newFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    baos.close();
                    byte [] fileBytes = baos.toByteArray();
                    String username =  doParseAndRename(fileBytes);

                    FileOutputStream pdfOutputStream = new FileOutputStream(descDir + File.separator + getOutputFileName(username));
                    IOUtils.write(fileBytes,pdfOutputStream);

                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }

    static String yearString ;
    static {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
        yearString = sdf.format(new Date());
    }

    public static String getOutputFileName(String username){
        //先月
        String monthStr = String.format("%02d", new Date().getMonth());
        String fileName = "給与明細書_"+yearString+monthStr+"_"+username+".pdf";
        return fileName;
    }

    public void renameFile(String dir){
//        File folder = new File(dir);
//        File [] files = folder.listFiles();
//        for (File f : files) {
//            doParseAndRename(f);
//        }
    }

    public static String doParseAndRename(byte [] fileBytes){

        StringWriter output = null;
        PDDocument document = null;
        try{

            boolean sort = false;
            boolean separateBeads = true;
            String password = "";
            int startPage = 1;
            int endPage = 2147483647;

//            String pdfFile = "C:\\Users\\issoh\\Desktop\\給与明細書_201904_施凱文.pdf";

            document = PDDocument.load(fileBytes);
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!(ap.canExtractContent())) {
                throw new IOException("You do not have permission to extract text");
            }

            output = new StringWriter();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(sort);
            stripper.setShouldSeparateByBeads(separateBeads);
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            stripper.writeText(document, output);

            String content = output.toString();
            String [] parts = StringUtils.split(content,"\n");
            if (parts == null || parts.length<5) {
                throw new RuntimeException(" file error ....");
            }
            String idStr = parts[1];
            String username = parts[2];

            System.out.println(idStr.split("：")[1].trim());

            String name = username.split("：")[1].replaceAll("\\s", "").replace("様", "");
            System.out.println(name);

            return name;
        }catch( Exception e ){
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(document);
        }

    }

}


