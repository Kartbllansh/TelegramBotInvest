package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.service.CreateTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
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
        Long res = checkAboutCountSell(Long.valueOf(count), tableName, key);
        if(res==0) {
            String sql = "DELETE FROM " + tableName + " WHERE code_stocks = ?";
            jdbcTemplate.update(sql, key);
            log.info("Удалена запись из таблицы " + tableName);
        } else if (res>0) {
           //res надо записать в базу данных вместо count_stocks
            String sqlUpdate = "UPDATE " + tableName + " SET count_stonks = ? WHERE code_stocks = ?";
            jdbcTemplate.update(sqlUpdate, res, key);
            log.info("Обновлено значение count_stonks в таблице " + tableName + " для code_stocks = " + key);
        } else {
            log.error("Отрицательное значение в addNoteAboutSell");
        }
        //TODO  метод, который будет позволять рассчитывать какое количество акций осталось на счету у пользователя
        //TODO STring должен вернуть пользователю, что по итогу вышло

    }

    @Override
    public String getInfoAboutBag(String tableName) {
        return null;
    }

    public Long checkAboutCountSell(Long count, String tableName, String codeStocks){
            String sql = "SELECT count_stonks FROM " + tableName + " WHERE code_stocks = ?";
            Long countStonks = jdbcTemplate.queryForObject(sql, Long.class, codeStocks);
            return countStonks - count;
    }

    @Override
    public Boolean checkAboutCodeStock(String tableName, String codeStock) {
        String sql = "SELECT count_stonks FROM " + tableName + " WHERE code_stocks = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, codeStock);

        // Значение codeStock найдено в таблице
        // Значение codeStock не найдено в таблице
        return rowSet.next();
    }
}
