package org.example.service;

import org.example.entity.AppUser;

import java.math.BigDecimal;

public interface WalletMain {
    String toKnowBalance(AppUser appUser);
    void topUpWallet(BigDecimal summa, AppUser appUser);
    //Todo придумать еще идеи к методам
}
