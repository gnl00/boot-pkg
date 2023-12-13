package com.demo.lite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.demo.core", "com.demo.lite"})
@SpringBootApplication
public class LiteMain {
    public static void main(String[] args) {
        SpringApplication.run(LiteMain.class, args);
    }
}