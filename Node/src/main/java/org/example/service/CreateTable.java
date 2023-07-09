package org.example.service;

import java.time.LocalDateTime;

public interface CreateTable {
    void createTable(String name);
    void addNoteAboutBuy(String key, Integer count, LocalDateTime localDateTime, Float purchase);
    void addNoteAboutSell(String key);
}
