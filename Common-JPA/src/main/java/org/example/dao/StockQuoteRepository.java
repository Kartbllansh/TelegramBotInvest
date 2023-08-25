package org.example.dao;
import org.example.entity.StockQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockQuoteRepository extends JpaRepository<StockQuote, Long> {
    Optional<StockQuote> findBySecId(String secId);
}
