package com.esun.financialsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EsunFinancialSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsunFinancialSystemApplication.class, args);
    }
}
