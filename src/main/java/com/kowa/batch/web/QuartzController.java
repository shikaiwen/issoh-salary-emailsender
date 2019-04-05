package com.kowa.batch.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class QuartzController {

    @Autowired
    ApplicationContext applicationContext;



    @GetMapping("/quartz/joblist")
    @ResponseBody
    public Object getJobList() throws Exception{
        return null;
    }












}
