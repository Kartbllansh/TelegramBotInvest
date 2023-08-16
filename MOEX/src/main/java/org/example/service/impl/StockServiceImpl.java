package org.example.service.impl;

import org.example.jpa.dao.StockQuoteRepository;
import org.example.jpa.entity.StockQuote;
import org.example.service.StockService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {
    private final StockQuoteRepository stockQuoteRepository;

    public StockServiceImpl(StockQuoteRepository stockQuoteRepository) {
        this.stockQuoteRepository = stockQuoteRepository;
    }

    @Override
    public StockQuote getInfoAboutTicket(String secId) {
        Optional<StockQuote> s = stockQuoteRepository.findBySecId(secId);
        return s.orElse(null);
    }
}
