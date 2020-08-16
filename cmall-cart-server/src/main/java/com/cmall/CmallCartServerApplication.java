package com.cmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.cmall.cart.mapper")
public class CmallCartServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmallCartServerApplication.class, args);
    }

}
