package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.example.model.GlobalQuote;
import org.example.model.QuoteResponse;
import org.example.service.StocksInformationService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class StocksServiceImpl implements StocksInformationService {
    private static final String API_KEY = "XO26SREVIMMGO73A";
    private static final String BASE_URL = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=";


    @Override
    public GlobalQuote getInfoAboutStocks(String keyStock) {
        String url = BASE_URL + keyStock + "&apikey=" + API_KEY;

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            // Разбор JSON-ответа и получение стоимости акции
            // Вам нужно вставить соответствующий код для разбора JSON и извлечения нужной информации

            return  parseJson(responseString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    private GlobalQuote parseJson(String jsonStr){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            QuoteResponse quoteResponse = objectMapper.readValue(jsonStr, QuoteResponse.class);

            return GlobalQuote.builder()
                    .symbol(quoteResponse.getGlobalQuote().getSymbol())
                    .price(quoteResponse.getGlobalQuote().getPrice())
                    .open(quoteResponse.getGlobalQuote().getOpen())
                    .high(quoteResponse.getGlobalQuote().getHigh())
                    .low(quoteResponse.getGlobalQuote().getLow())
                    .volume(quoteResponse.getGlobalQuote().getVolume())
                    .change(quoteResponse.getGlobalQuote().getChange())
                    .latestTradingDay(quoteResponse.getGlobalQuote().getLatestTradingDay())
                    .previousClose(quoteResponse.getGlobalQuote().getPreviousClose())
                    .change(quoteResponse.getGlobalQuote().getChange())
                    .changePercent(quoteResponse.getGlobalQuote().getChangePercent())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
