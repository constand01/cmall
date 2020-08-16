package com.cmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.cmall.manage.server.mapper")
public class CmallManageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallManageServerApplication.class, args);
    }

}
