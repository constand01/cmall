package com.cmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.cmall.user.mapper")
public class CmallUserServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(CmallUserServerApplication.class, args);
    }

}
