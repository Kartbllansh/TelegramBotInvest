package org.example.service;

import org.example.jpa.entity.StockQuote;

public interface StockService {
    StockQuote getInfoAboutTicket(String secId);
}
