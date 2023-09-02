package org.example.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.dao.StockQuoteRepository;
import org.example.entity.StockQuote;
import org.example.service.MainBaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class MainBaseServiceImpl implements MainBaseService {
//@Value("${url.moex}")
    private String apiUrl ="https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities.json?iss.meta=off&iss.only=securities&securities.columns=SECID,SHORTNAME,PREVLEGALCLOSEPRICE,PREVDATE";

    private final StockQuoteRepository stockQuoteRepository;
    private final RestTemplate restTemplate;

    public MainBaseServiceImpl(StockQuoteRepository stockQuoteRepository, RestTemplate restTemplate) {
        this.stockQuoteRepository = stockQuoteRepository;
        this.restTemplate = restTemplate;
    }
    @Scheduled(fixedRate = 6000000) // Запуск каждые 10 минут (600000 миллисекунд)
    @Override
    public void fetchStockQuote() {
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
        System.out.println(response.getBody());
        parseFileJsonAndUpdate(response);
    }
    private void parseFileJsonAndUpdate(ResponseEntity<String> response){
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(response.getBody(), JsonObject.class);
        JsonArray dataNode = root.getAsJsonObject("securities").getAsJsonArray("data");

        for (JsonElement securityElement : dataNode) {
            JsonArray securityArray = securityElement.getAsJsonArray();

            String secId = securityArray.get(0).getAsString();
            String shortName = securityArray.get(1).getAsString();
            BigDecimal prevLegalClosePrice = securityArray.get(2).getAsBigDecimal();
            String date = securityArray.get(3).getAsString();
            var optional = stockQuoteRepository.findBySecId(secId);
            if(optional.isEmpty()){
                StockQuote stockQuote = new StockQuote();
                stockQuote.setSecId(secId);
                stockQuote.setShortName(shortName);
                stockQuote.setPrevLegalClosePrice(prevLegalClosePrice);
                stockQuote.setDate(date);
                stockQuote.setTimeNow(LocalDateTime.now().toString());

                stockQuoteRepository.save(stockQuote);
            } else {
                optional.get().setShortName(shortName);
                optional.get().setSecId(secId);
                optional.get().setDate(date);
                optional.get().setPrevLegalClosePrice(prevLegalClosePrice);
                optional.get().setTimeNow(LocalDateTime.now().toString());
                stockQuoteRepository.save(optional.get());
            }


        }
    }


}
