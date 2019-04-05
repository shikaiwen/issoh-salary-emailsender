package com.kowa.batch.web;

import java.net.URL;
import java.net.URLClassLoader;

public class Test {

    public static void main(String[] args) {
        String strClassPath = System.getProperty("java.class.path");
        URLClassLoader urlClassLoader = (URLClassLoader)Test.class.getClassLoader();
        URL[] urls = urlClassLoader.getURLs();

        URL rere = urlClassLoader.getResource("application.properties");
        System.out.println(rere.getPath());


//        for (URL u : urls) {
//            System.out.println(u.getPath());
//        }

//        URLClassLoader
//        System.out.println("Classpath is " + StringUtils.join(strClassPath.split(";"),"\n"));
    }
}
