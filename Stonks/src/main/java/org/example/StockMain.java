package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class StockMain {


    public static void main(String[] args) {
        SpringApplication.run(StockMain.class);
                String apiKey = "XO26SREVIMMGO73A";
                List<String> symbols = new ArrayList<>();
                symbols.add("AAPL");
                symbols.add("MSFT");
                symbols.add("GOOGL");
                // Добавьте другие символы компаний по мере необходимости

                int batchSize = 50; // Максимальное количество символов в одном запросе BATCH_STOCK_QUOTES

                for (int i = 0; i < symbols.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, symbols.size());
                    List<String> symbolBatch = symbols.subList(i, endIndex);
                    String symbolStr = String.join(",", symbolBatch);

                    String apiUrl = "https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=" + symbolStr + "&apikey=" + apiKey;

                    try {
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            Scanner scanner = new Scanner(connection.getInputStream());
                            StringBuilder response = new StringBuilder();

                            while (scanner.hasNext()) {
                                response.append(scanner.nextLine());
                            }

                            scanner.close();

                            // Здесь вы можете обработать полученные данные в формате JSON
                            String jsonResponse = response.toString();
                            System.out.println(jsonResponse);
                        } else {
                            System.out.println("Ошибка при отправке запроса. Код ответа: " + responseCode);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

