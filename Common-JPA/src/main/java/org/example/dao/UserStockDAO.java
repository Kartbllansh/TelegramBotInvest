package org.example.dao;

import org.example.entity.AppUser;
import org.example.entity.StockQuote;
import org.example.entity.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserStockDAO extends JpaRepository<UserStock, Long> {
    Optional<UserStock> findByAppUserAndStockQuote(AppUser user, StockQuote stock);
    List<UserStock> findByAppUser(AppUser user);
    Optional<UserStock> findByAppUserAndStockQuote_SecId(AppUser user, String code);
}
