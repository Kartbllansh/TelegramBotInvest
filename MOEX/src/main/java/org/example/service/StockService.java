package org.example.service;

import org.example.jpa.entity.StockQuote;

import java.util.List;

public interface StockService {
    StockQuote getInfoAboutTicket(String secId);
    List<StockQuote> fuzzysearchCompany(String userText);
}
