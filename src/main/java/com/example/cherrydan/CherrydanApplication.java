package com.example.cherrydan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CherrydanApplication {

    public static void main(String[] args) {
        SpringApplication.run(CherrydanApplication.class, args);
    }

}
