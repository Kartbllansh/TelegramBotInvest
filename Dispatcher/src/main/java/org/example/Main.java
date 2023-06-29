package org.example;

import lombok.extern.log4j.Log4j;
import org.example.controller.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Log4j
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}