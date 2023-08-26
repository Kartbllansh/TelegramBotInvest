package org.example.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "userStock")
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appuser_id")
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private StockQuote stockQuote;

    @Column(name = "count_stocks")
    private int countStock;
    @Column(name = "time_buy")
    private String noticeBuyOrSell;
    @Column(name = "purchase_stonks")
    private BigDecimal price;


}
