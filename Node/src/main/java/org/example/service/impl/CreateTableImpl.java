package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.service.CreateTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Log4j
public class CreateTableImpl implements CreateTable {
    private final JdbcTemplate jdbcTemplate;

    public CreateTableImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createTable(String tableName) {
        String sql = "CREATE TABLE IF NOT EXISTS "+tableName+ " ( id_deal SERIAL PRIMARY KEY, code_stocks VARCHAR(255), count_stonks INTEGER, time_buy TIMESTAMP, purchase_stonks BIGINT );";
        jdbcTemplate.execute(sql);
        log.info("Создана индивидувльная таблица с именем "+tableName);
    }

    @Override
    public void addNoteAboutBuy(String tableName, String key, Integer count, LocalDateTime localDateTime, Float purchase) {
        String sql = "INSERT INTO " + tableName + " (code_stocks, count_stonks, time_buy, purchase_stonks) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, key, count, localDateTime, purchase);
        log.info("Добавлена запись в таблицу " + tableName);
    }

    @Override
    public void addNoteAboutSell(String tableName, String key, Integer count) {
        String sql = "DELETE FROM " + tableName + " WHERE code_stocks = ?";
        jdbcTemplate.update(sql, key);
        log.info("Удалена запись из таблицы " + tableName);

    }
}
