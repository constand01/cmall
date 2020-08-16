package com.cmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CmallUserWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallUserWebApplication.class, args);
    }

}
