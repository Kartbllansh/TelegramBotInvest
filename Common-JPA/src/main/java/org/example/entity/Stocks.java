package org.example.entity;

import lombok.*;


import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stocks {

    private long id;
    @Column(name = "code_stocks")
    private String codeStock;
    @Column(name = "count_stonks")
    private int countStock;
    @Column(name = "time_buy")
    private LocalDateTime localDateTime;
    @Column(name = "purchase_stonks")
    private BigDecimal price;
}
