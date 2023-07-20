package org.example.dao;

import org.example.entity.Stocks;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StocksRowMapper implements RowMapper<Stocks> {
    @Override
    public Stocks mapRow(ResultSet rs, int rowNum) throws SQLException {
        Stocks stocks = new Stocks();
        stocks.setId(rs.getLong("id_deal"));
        stocks.setCodeStock(rs.getString("code_stocks"));
        stocks.setCountStock(rs.getInt("count_stonks"));
        stocks.setLocalDateTime(rs.getTimestamp("time_buy").toLocalDateTime());
        stocks.setPrice(rs.getBigDecimal("purchase_stonks"));
        return stocks;
    }
}
