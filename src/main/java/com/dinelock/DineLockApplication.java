package com.dinelock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DineLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DineLockApplication.class, args);
    }

}
