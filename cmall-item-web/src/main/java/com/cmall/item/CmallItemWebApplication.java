package com.cmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CmallItemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallItemWebApplication.class, args);
    }

}
