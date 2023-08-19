package org.example.service;

import org.example.entity.AppUser;
import org.example.utils.ButtonForKeyboard;

public interface BuyOrSellService {
    void onActionBuy(AppUser appUser, String cmd, Long chatId);
    void onActionSell(AppUser appUser, String cmd, Long chatId);
    String buyProofYes(AppUser appUser);
    String sellProofYes(AppUser appUser);
    void sendAnswerWithInlineKeyboard(String output, long chatId, ButtonForKeyboard... buttons);
}
