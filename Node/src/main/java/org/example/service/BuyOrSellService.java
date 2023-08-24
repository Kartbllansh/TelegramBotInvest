package org.example.service;

import org.example.entity.AppUser;

public interface BuyOrSellService {
    void onActionBuy(AppUser appUser, String cmd, Long chatId, long messageId);
    void onActionSell(AppUser appUser, String cmd, Long chatId, long messageId);
    String buyProofYes(AppUser appUser);
    String sellProofYes(AppUser appUser);
    void buyChangeStocks(AppUser appUser, String cmd, Long chatId, long messageId);

}
