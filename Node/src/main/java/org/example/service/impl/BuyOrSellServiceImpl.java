package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.BuyOrSellService;
import org.example.service.CreateTable;
import org.example.service.ProducerService;
import org.example.service.StocksInformationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.SELL_CHANGE_COUNT;
import static org.example.entity.SellUserState.SELL_PROOF;

@Log4j
@Service
public class BuyOrSellServiceImpl implements BuyOrSellService {
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final StocksInformationService stockInformationService;
    private final CreateTable createTable;

    public BuyOrSellServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, StocksInformationService stocksInformationService, CreateTable createTable) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.stockInformationService = stocksInformationService;
        this.createTable = createTable;
    }

    @Override
    public void onActionBuy(AppUser appUser, String cmd, Long chatId) {
        String info = "";
        switch (appUser.getBuyUserState()){

            case CHANGE_COUNT:
                if (cmd.trim().matches("\\d+")){
                    long count = Long.parseLong(cmd);
                    info ="Успешно куплено "+count+" акций";
                    String temporaryValue = appUser.getActiveBuy();
                    String newValue = temporaryValue+":"+count;
                    appUser.setActiveBuy(newValue);
                } else {
                    info = "Введено неправильно значение. Бот ожидает число.";
                }
                sendAnswer(info, chatId);
                sendAnswer("Подтверждение! Если вы подтверждаете покупку введите Да, если отменяете Нет", chatId);
                appUser.setBuyUserState(BUY_PROOF);
                appUserDAO.save(appUser);
                break;
            case CHANGE_STONKS:
                //TODO добавить эффект докупки, чтобы не было строчек с двумя одинаковыми ключами
                //TODO стоимость акции в базу данных записывается в виде целого числа
                if(!(stockInformationService.getInfoAboutStocks(cmd)==null)) {
                    String cost = stockInformationService.getInfoAboutStocks(cmd).getPrice();
                    String symbol = stockInformationService.getInfoAboutStocks(cmd).getSymbol();
                    info = "Цена ценной бумаги " + cmd + " равняется " + cost + " это цена на момент " + stockInformationService.getInfoAboutStocks(cmd).getLatestTradingDay();
                    sendAnswer(info, chatId);
                    sendAnswer("Какое количество акций вы хотите приобрести?", chatId);

                    appUser.setBuyUserState(CHANGE_COUNT);
                    appUser.setActiveBuy(symbol+":"+cost);
                    appUserDAO.save(appUser);
                } else {
                    sendAnswer("Ценной бумаги с таким символом нет", chatId);
                }
                break;
            case BUY_PROOF:
                if(cmd.equalsIgnoreCase("ДА")){
                    info = "ЕЕЕ. Успешная сделка!";
                    String activeBuy = appUser.getActiveBuy();
                    createTable.addNoteAboutBuy("telegramuser_"+appUser.getTelegramUserId(), parseStringFromBD(activeBuy, 0), Integer.valueOf(parseStringFromBD(activeBuy, 2)), LocalDateTime.now(), Float.valueOf(parseStringFromBD(activeBuy, 1)));
                    appUser.setBuyUserState(NOT_BUY);
                    appUserDAO.save(appUser);
                } else if (cmd.equalsIgnoreCase("НЕТ")) {
                    info = "Сделка отменена. Если захотите опять что-то купить введите команду /buy";
                    appUser.setBuyUserState(NOT_BUY);
                    appUserDAO.save(appUser);
                } else {
                    info = "Введите Да или Нет. Или же команду /cancel";
                }
                sendAnswer(info, chatId);
                break;
        }

    }

    @Override
    public void onActionSell(AppUser appUser, String cmd, Long chatId) {
        String info ="";
        switch (appUser.getSellUserState()){
            case SELL_CHANGE_COUNT:
                if (cmd.trim().matches("\\d+")){
                    long count = Long.parseLong(cmd);
                    String temporaryValue = appUser.getActiveBuy();
                    String codeStocks =parseStringFromBD(temporaryValue, 0);
                    Long someResult = createTable.checkAboutCountSell(count, "telegramuser_"+appUser.getTelegramUserId(), codeStocks);
                    if(someResult>=0) {
                        info = "Успешно продано " + count + " акций " + parseStringFromBD(temporaryValue, 0);
                        sendAnswer(info, chatId);
                        sendAnswer("Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId);
                        appUser.setSellUserState(SELL_PROOF);
                        String newValue = temporaryValue + ":" + count;
                        appUser.setActiveBuy(newValue);
                        appUserDAO.save(appUser);
                    } else {
                     sendAnswer("Нельзя продать такое количество акций, у вас их меньше", chatId);
                     //TODO подсказка почему пользователь мог ошибиться
                    }
                } else {
                    info = "Введено неправильно значение. Бот ожидает число.";
                    sendAnswer(info, chatId);
                }

                break;
            case SELL_CHANGE_STOCK:
                if(createTable.checkAboutCodeStock("telegramuser_"+appUser.getTelegramUserId(), cmd)) {
                    if (!(stockInformationService.getInfoAboutStocks(cmd) == null)) {
                        String cost = stockInformationService.getInfoAboutStocks(cmd).getPrice();
                        String symbol = stockInformationService.getInfoAboutStocks(cmd).getSymbol();
                        //TODO проверка на то, какие акции есть у пользователя(ПРОВЕРКА)
                        sendAnswer("Выбрана акция " + cmd, chatId);
                        sendAnswer("Введите также количество акций, которое вы хотите продать. Сейчас у вас 3", chatId);
                        appUser.setSellUserState(SELL_CHANGE_COUNT);
                        appUser.setActiveBuy(symbol + ":" + cost);
                        appUserDAO.save(appUser);
                    } else {
                        sendAnswer("Данный код акции не видит Vantage", chatId);
                    }
                } else {
                    sendAnswer("В твоем портфеле нет такой акции", chatId);
                }

                break;
            case SELL_PROOF:
                if(cmd.equalsIgnoreCase("ДА")){
                    info = "ЕЕЕ. Успешная сделка!";
                    String activeSell = appUser.getActiveBuy();
                    createTable.addNoteAboutSell("telegramuser_"+appUser.getTelegramUserId(), parseStringFromBD(activeSell, 0), Integer.valueOf(parseStringFromBD(activeSell, 3)));
                    appUser.setBuyUserState(NOT_BUY);
                    appUserDAO.save(appUser);
                } else if (cmd.equalsIgnoreCase("НЕТ")) {
                    info = "Сделка отменена. Если захотите опять что-то купить введите команду /buy";
                    appUser.setBuyUserState(NOT_BUY);
                    appUserDAO.save(appUser);
                } else {
                    info = "Введите Да или Нет. Или же команду /cancel";
                }
                sendAnswer(info, chatId);
                break;
        }


    }

    private String parseStringFromBD(String s, int i){
        String[] parts = s.split(":");
        if(i==0){
            return parts[0];
        } else if (i==1) {
            return parts[1];

        } else {
            return parts[2];
        }
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }
}
