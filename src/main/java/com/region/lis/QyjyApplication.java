package com.region.lis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.region.lis.mapper")
public class QyjyApplication {

    public static void main(String[] args) {
        SpringApplication.run(QyjyApplication.class, args);
    }

}
