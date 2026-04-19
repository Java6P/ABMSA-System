package com.abmsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AbmsaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbmsaApplication.class, args);
    }
}
