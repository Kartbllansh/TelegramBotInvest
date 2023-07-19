package org.example.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramUserId;
    //TODO убрать chatID так как в нем тоже самое значение как и в telegramUserID
    private Long chatId;
    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserState state;
    private Boolean isActive;
    @Enumerated(EnumType.STRING)
    private BuyUserState buyUserState;
    @Enumerated(EnumType.STRING)
    private SellUserState sellUserState;
    private String activeBuy;

}
