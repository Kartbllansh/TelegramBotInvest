package org.example.service;

import org.example.entity.AppUser;

public interface BuyOrSellService {
    void onActionBuy(AppUser appUser, String cmd, Long chatId);
    void onActionSell(AppUser appUser, String cmd, Long chatId);
}
