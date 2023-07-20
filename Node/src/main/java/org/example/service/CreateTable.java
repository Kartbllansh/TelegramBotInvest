package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CreateTable {
    void createTable(String name);
    void addNoteAboutBuy(String tableName, String key, Integer count, LocalDateTime localDateTime, BigDecimal purchase);
    void addNoteAboutSell(String tableName, String key, Integer count);

    String getInfoAboutBag(String tableName);
    Long checkAboutCountSell(Long count, String tableName, String codeStocks);
    Boolean checkAboutCodeStock(String tableName, String codeStock);
}
