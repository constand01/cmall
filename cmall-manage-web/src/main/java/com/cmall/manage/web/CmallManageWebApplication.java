package com.cmall.manage.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CmallManageWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallManageWebApplication.class, args);
    }

}
