package com.bubble.giju;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GijuApplication {

    public static void main(String[] args) {
        SpringApplication.run(GijuApplication.class, args);
    }

}
