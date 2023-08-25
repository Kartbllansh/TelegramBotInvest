package org.example.service;


import org.example.entity.StockQuote;

import java.util.List;

public interface StockService {
    StockQuote getInfoAboutTicket(String secId);
    List<StockQuote> fuzzysearchCompany(String userText);
    List<StockQuote> searchOnCompany(String searchTerm, int maxResults);
}
