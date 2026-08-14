package com.payflow.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PayflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayflowApplication.class, args);
    }
}