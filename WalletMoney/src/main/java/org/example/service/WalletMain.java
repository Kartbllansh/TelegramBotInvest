package org.example.service;

import org.example.entity.AppUser;

import java.math.BigDecimal;

public interface WalletMain {
    String toKnowBalance(AppUser appUser);
    String topUpWallet(BigDecimal summa, AppUser appUser);
    //Todo придумать еще идеи к методам
    String topDownWallet(BigDecimal summa, AppUser appUser);
    boolean checkAbilityBuy(BigDecimal summa, AppUser appUser);
}
