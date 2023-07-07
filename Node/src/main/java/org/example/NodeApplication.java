package org.example;

import lombok.extern.log4j.Log4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j
public class NodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class);
        log.info("NodeApplication running...");
    }
}