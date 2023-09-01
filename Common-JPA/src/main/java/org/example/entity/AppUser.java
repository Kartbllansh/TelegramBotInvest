package org.example.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appUser")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramUserId;
    //TODO убрать chatID так как в нем тоже самое значение как и в telegramUserID
    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserState state;
    private Boolean isActiveMail;
    private Boolean isActiveConsent;
    @Enumerated(EnumType.STRING)
    private BuyUserState buyUserState;
    @Enumerated(EnumType.STRING)
    private SellUserState sellUserState;
    private String activeBuy;
    private BigDecimal walletMoney;
    private BigDecimal topUpAmount;
    @Enumerated(EnumType.STRING)
    private WalletUserState walletUserState;

}
