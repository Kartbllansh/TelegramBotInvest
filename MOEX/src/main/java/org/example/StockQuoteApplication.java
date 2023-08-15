package org.example;

import lombok.extern.log4j.Log4j;
import org.example.service.impl.MainBaseServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@Log4j
public class StockQuoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockQuoteApplication.class);
    }
}