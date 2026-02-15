package com.cool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoolCommunityApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CoolCommunityApplication.class, args);
    }
}
