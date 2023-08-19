package org.example.service.impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;

import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.NOT_SELL;
import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.WalletUserState.NOT_WALLET;

@Service
public class CallBackMainServiceImpl implements CallBackMainService {
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final BuyOrSellService buyOrSellService;


    public CallBackMainServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, BuyOrSellService buyOrSellService) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;

        this.buyOrSellService = buyOrSellService;
    }

    @Override
    public void processCallBackQuery(Update update) {
        AppUser appUser = findOrSaveAppUserFromCallBack(update);
    long messageId = update.getCallbackQuery().getMessage().getMessageId();
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String callBackData = update.getCallbackQuery().getData();
        switch (callBackData) {
            case "YES_BUTTON_BUY":
                processYesButtonBuy(appUser, messageId, chatId);
                break;
            case "NO_BUTTON_BUY":
                processNoButtonBuy(appUser, messageId, chatId);
                break;
            case "YES_BUTTON_SELL":
                processYesButtonSell(appUser, messageId, chatId);
                break;
            case "NO_BUTTON_SELL":
                processNoButtonSell(appUser, messageId, chatId);
                break;
        }
    }

    private void processNoButtonSell(AppUser appUser, long messageId, long chatId) {
        String output = "Сделка отменена. Если захотите опять что-то продать введите команду /buy";
        producerService.producerAnswerWithCallBack(doEditMessage(messageId, chatId, output));
        appUser.setSellUserState(NOT_SELL);
        appUserDAO.save(appUser);
    }

    private void processYesButtonSell(AppUser appUser, long messageId, long chatId) {
        producerService.producerAnswerWithCallBack(doEditMessage(messageId, chatId, buyOrSellService.sellProofYes(appUser)));
    }

    private void processNoButtonBuy(AppUser appUser, long messageId, long chatId) {
        String output = "Сделка отменена. Если захотите опять что-то купить введите команду /buy";
    producerService.producerAnswerWithCallBack(doEditMessage(messageId, chatId, output ));
        appUser.setBuyUserState(NOT_BUY);
        appUserDAO.save(appUser);
    }

    private void processYesButtonBuy(AppUser appUser, long messageId, long chatId) {
     producerService.producerAnswerWithCallBack(doEditMessage(messageId, chatId, buyOrSellService.buyProofYes(appUser)));
    }

    private EditMessageText doEditMessage(long messageId, long chatId, String output){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(output);
        editMessageText.setMessageId((int) messageId);
        return editMessageText;
    }
    private AppUser findOrSaveAppUserFromCallBack(Update update){

        var telegramUser = update.getCallbackQuery().getFrom();

        var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        return optional.get();
    }

}
