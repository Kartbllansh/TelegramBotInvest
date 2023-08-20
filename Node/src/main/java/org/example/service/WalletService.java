package org.example.service;

import org.example.entity.AppUser;

public interface WalletService {
    void onActiveWallet(AppUser appUser, String text, Long chatId, long messageId);
}
