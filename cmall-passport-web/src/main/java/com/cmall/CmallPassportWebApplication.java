package com.cmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CmallPassportWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallPassportWebApplication.class, args);
    }

}
