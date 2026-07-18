package com.starlinkiraq.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * نقطة انطلاق تطبيق Spring Boot الخاص بمتجر Starlink العراق.
 */
@SpringBootApplication
@EnableAsync
public class StoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class, args);
    }
}
