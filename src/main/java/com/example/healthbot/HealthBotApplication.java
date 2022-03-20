package com.example.healthbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class HealthBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthBotApplication.class, args);
    }

}
