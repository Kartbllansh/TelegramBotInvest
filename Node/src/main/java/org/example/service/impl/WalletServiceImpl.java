package org.example.service.impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.ProducerService;
import org.example.service.WalletMain;
import org.example.service.WalletService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.math.BigDecimal;

import static org.example.entity.WalletUserState.WALLET_TOP_UP_CHANGE_COUNT;
import static org.example.enums.CommandService.WALLET_LOOK_BALANCE;
import static org.example.enums.CommandService.WALLET_TOP_UP_CMD;

@Service
public class WalletServiceImpl implements WalletService {
    private final WalletMain walletMain;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;

    public WalletServiceImpl(WalletMain walletMain, ProducerService producerService, AppUserDAO appUserDAO) {
        this.walletMain = walletMain;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void onActiveWallet(AppUser appUser, String text, Long chatId) {
        if(appUser.getWalletUserState().equals(WALLET_TOP_UP_CHANGE_COUNT)){
            topUpChangeCount(appUser, text, chatId);
        }
        var serviceCommand = CommandService.fromValue(text);
        if(WALLET_LOOK_BALANCE.equals(serviceCommand)){
           String infoAboutBalance = walletMain.toKnowBalance(appUser);
           sendAnswer(infoAboutBalance, chatId);
        } else if (WALLET_TOP_UP_CMD.equals(serviceCommand)) {
            sendAnswer("Введите сумму, на которую хотите увеличить свой счет", chatId);
            appUser.setWalletUserState(WALLET_TOP_UP_CHANGE_COUNT);
            appUserDAO.save(appUser);
        }
    }
    private void topUpChangeCount(AppUser appUser, String text, Long chatId){
    if (isNumber(text)){
        BigDecimal bigDecimal = new BigDecimal(text);
        String someInfo = walletMain.topUpWallet(bigDecimal, appUser);
        sendAnswer(someInfo, chatId);
    } else {
        sendAnswer("Пожалуйста введите корректное число или нажмите /cancel, чтобы выйти", chatId);
    }
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }
    public boolean isNumber(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
