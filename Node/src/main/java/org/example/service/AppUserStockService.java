package org.example.service;

import org.example.entity.AppUser;
import org.example.entity.StockQuote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AppUserStockService {

    boolean hasUserBoughtStock(AppUser user, String codeStock);
    void saveOrUpdateUserStock(AppUser user, String code, int countStock, LocalDateTime purchaseTime, BigDecimal price);

    void sellUserStock(AppUser user, String c, int countStockToSell);
    String getInfoAboutBag(AppUser user);
    List<String> getAllKeysInBag(AppUser appUser);
    Long countOfTheBag(AppUser user, String code);
    Integer checkAboutCountSell(AppUser user, Integer count, String codeStocks);
    String reportAboutInvestBag(AppUser appUser);
}
