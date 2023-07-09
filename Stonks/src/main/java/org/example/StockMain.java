package org.example;

import org.example.service.StocksInformationService;
import org.example.service.impl.StocksServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StockMain {


    public static void main(String[] args) {
        SpringApplication.run(StockMain.class);
        StocksServiceImpl service = new StocksServiceImpl();
        System.out.println(service.getInfoAboutStocks("MSST").getPrice());

    }
}
