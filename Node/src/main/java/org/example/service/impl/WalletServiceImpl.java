package org.example.service.impl;

import com.vdurmont.emoji.EmojiParser;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.UtilsService;
import org.example.service.WalletMain;
import org.example.service.WalletService;
import org.example.dto.ButtonForKeyboard;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.example.entity.WalletUserState.NOT_WALLET;
import static org.example.entity.WalletUserState.WALLET_TOP_UP_CHANGE_COUNT;
import static org.example.enums.CommandService.WALLET_LOOK_BALANCE;
import static org.example.enums.CommandService.WALLET_TOP_UP_CMD;

@Service
public class WalletServiceImpl implements WalletService {
    private final WalletMain walletMain;
    private final AppUserDAO appUserDAO;
    private final UtilsService utilsService;

    public WalletServiceImpl(WalletMain walletMain, AppUserDAO appUserDAO, UtilsService utilsService) {
        this.walletMain = walletMain;
        this.appUserDAO = appUserDAO;
        this.utilsService = utilsService;
    }

    @Override
    public void onActiveWallet(AppUser appUser, String text, Long chatId, long messageId) {
        if(appUser.getWalletUserState().equals(WALLET_TOP_UP_CHANGE_COUNT)){
            topUpChangeCount(appUser, text, chatId, messageId);
            return;
        }
        var serviceCommand = CommandService.fromValue(text);
        if(WALLET_LOOK_BALANCE.equals(serviceCommand)){
           String infoAboutBalance = walletMain.toKnowBalance(appUser);
           //utilsService.sendAnswer(infoAboutBalance, chatId);
           utilsService.sendEditMessageAnswer(infoAboutBalance, chatId, messageId);
        } else if (WALLET_TOP_UP_CMD.equals(serviceCommand)) {
            utilsService.sendMessageAnswerWithInlineKeyboard("Введите сумму, на которую хотите увеличить свой счет", chatId, true, new ButtonForKeyboard("Отмена", "CANCEL"));
            appUser.setWalletUserState(WALLET_TOP_UP_CHANGE_COUNT);
            appUserDAO.save(appUser);
        } else{
            utilsService.sendMessageAnswerWithInlineKeyboard("Активирована команда /wallet"+ EmojiParser.parseToUnicode("")+"\n Выберите действия из предложенных снизу в кнопках", chatId, false, new ButtonForKeyboard("Пополнить"+EmojiParser.parseToUnicode(":top: "), "TOP_UP_COMMAND"), new ButtonForKeyboard("Посмотреть"+EmojiParser.parseToUnicode(":eyes:"), "LOOK_BALANCE_COMMAND"),new ButtonForKeyboard("Отмена"+EmojiParser.parseToUnicode(":leftwards_arrow_with_hook:"), "CANCEL") );
        }
    }
    private void topUpChangeCount(AppUser appUser, String text, Long chatId, long messageId){
    if (isNumber(text)){
        BigDecimal bigDecimal = new BigDecimal(text);
        String someInfo = walletMain.topUpWallet(bigDecimal, appUser, true);
        utilsService.sendAnswer(someInfo, chatId);
        appUser.setWalletUserState(NOT_WALLET);
        appUserDAO.save(appUser);
    } else {
        utilsService.sendMessageAnswerWithInlineKeyboard("Пожалуйста введите корректное число или нажмите /cancel, чтобы выйти", chatId, true, new ButtonForKeyboard("Отмена", "CANCEL") );
    }
    }

    private boolean isNumber(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
