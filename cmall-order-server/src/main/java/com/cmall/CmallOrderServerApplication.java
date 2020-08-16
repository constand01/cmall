package com.cmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.cmall.order.mapper")
public class CmallOrderServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallOrderServerApplication.class, args);
    }

}
