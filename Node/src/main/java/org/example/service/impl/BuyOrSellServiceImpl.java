package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.jpa.entity.StockQuote;
import org.example.service.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.*;

@Log4j
@Service
public class BuyOrSellServiceImpl implements BuyOrSellService {
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final CreateTable createTable;
    private final WalletMain walletMain;
    private final StockServiceImpl stockService;

    public BuyOrSellServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, CreateTable createTable, WalletMain walletMain, StockServiceImpl stockService) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.createTable = createTable;
        this.walletMain = walletMain;
        this.stockService = stockService;
    }

    @Override
    public void onActionBuy(AppUser appUser, String cmd, Long chatId) {
        switch (appUser.getBuyUserState()){
            case CHANGE_COUNT:
               buyChangeCount(appUser, cmd, chatId);
                break;
            case CHANGE_STONKS:
                buyChangeStocks(appUser, cmd, chatId);
                break;
            case BUY_PROOF:
                buyProof(appUser, cmd, chatId);
                break;
        }

    }

    @Override
    public void onActionSell(AppUser appUser, String cmd, Long chatId) {
        switch (appUser.getSellUserState()){
            case SELL_CHANGE_COUNT:
                sellChangeCount(appUser, cmd, chatId);
                break;
            case SELL_CHANGE_STOCK:
               sellChangeStock(appUser, cmd, chatId);
                break;
            case SELL_PROOF:
                sellProof(appUser, cmd, chatId);
                break;
        }


    }

    private void buyChangeCount(AppUser appUser, String cmd, Long chatId){
        String info = "";
        if (cmd.trim().matches("\\d+")){

            long count = Long.parseLong(cmd);
            String newValue = appUser.getActiveBuy()+":"+count;
            info ="Покупка "+count+" "+parseStringFromBD(newValue, 2)+" ("+parseStringFromBD(newValue, 0)+") ";
            //TODO настроить Id  в базе данных
            appUser.setActiveBuy(newValue);

        } else {
            info = "Введено неправильно значение. Бот ожидает число.";
        }
        sendAnswer(info, chatId);
        sendAnswer("Подтверждение! Если вы подтверждаете покупку введите Да, если отменяете Нет", chatId);
        appUser.setBuyUserState(BUY_PROOF);
        appUserDAO.save(appUser);
    }

    private void buyChangeStocks(AppUser appUser, String cmd, Long chatId){
        String info = "";
        StockQuote stockQuote = stockService.getInfoAboutTicket(cmd);
        //TODO стоимость акции в базу данных записывается в виде целого числа
        if(!(stockQuote==null)) {
            BigDecimal cost = stockQuote.getPrevLegalClosePrice();
            String symbol = stockQuote.getSecId();
            info = "Цена ценной бумаги " + cmd + " равняется " + cost + " это цена на момент " + stockQuote.getDate();

            sendAnswer(info, chatId);
            sendAnswer("Какое количество акций вы хотите приобрести?", chatId);
            appUser.setBuyUserState(CHANGE_COUNT);
            appUser.setActiveBuy(symbol+":"+cost+":"+stockQuote.getShortName());
            appUserDAO.save(appUser);
        } else {
            sendAnswer("Чат-бот не знаком с такой ценной бумаги. В ближайшее время мы попробуем добавить данную компанию в список доступных.", chatId);
            //TODO добавить лог для записи, какие акции хотели купить
        }
    }

    private void buyProof(AppUser appUser, String cmd, Long chatId){
        String info = "";
        if(cmd.equalsIgnoreCase("ДА")){

            String activeBuy = appUser.getActiveBuy();
            int count = Integer.parseInt(parseStringFromBD(activeBuy, 3));
            BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(parseStringFromBD(activeBuy, 1)));
            BigDecimal countFromUser = BigDecimal.valueOf(count);

            createTable.addNoteAboutBuy("telegramuser_"+appUser.getTelegramUserId(), parseStringFromBD(activeBuy, 0), count, LocalDateTime.now(), purchace, parseStringFromBD(activeBuy, 2));
            info = walletMain.topDownWallet(purchace.multiply(countFromUser), appUser);
            //TODO настроить текстовые ответы

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
    }

    private void sellChangeCount(AppUser appUser, String cmd, Long chatId){
        //TODO на данный момент программа не проверяет на моменте выбора количества акций возможность покупки
        String info = "";
        if (cmd.trim().matches("\\d+")){
            long count = Long.parseLong(cmd);
            String temporaryValue = appUser.getActiveBuy();
            String codeStocks =parseStringFromBD(temporaryValue, 0);
            Long someResult = createTable.checkAboutCountSell(count, "telegramuser_"+appUser.getTelegramUserId(), codeStocks);
            if(someResult>=0) {
                info = "Продажа " + count + " акций " + parseStringFromBD(temporaryValue, 2)+"("+parseStringFromBD(temporaryValue, 0)+")";

                sendAnswer(info, chatId);
                sendAnswer("Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId);
                appUser.setSellUserState(SELL_PROOF);
                appUser.setActiveBuy(temporaryValue + ":" + count);
                appUserDAO.save(appUser);
            } else {
                sendAnswer("Нельзя продать такое количество акций, у вас их меньше", chatId);
                //TODO подсказка почему пользователь мог ошибиться
            }
        } else {
            info = "Введено неправильно значение. Бот ожидает число.";
            sendAnswer(info, chatId);
        }
    }
    private void sellChangeStock(AppUser appUser, String cmd, Long chatId){
        if(createTable.checkAboutCodeStock("telegramuser_"+appUser.getTelegramUserId(), cmd)) {
            StockQuote stockQuote = stockService.getInfoAboutTicket(cmd);
            if (!(stockQuote== null)) {
                BigDecimal cost = stockQuote.getPrevLegalClosePrice();
                String symbol = stockQuote.getSecId();
                sendAnswer("Выбрана акция " + cmd, chatId);
                sendAnswer("Введите также количество акций, которое вы хотите продать. Сейчас у вас "+createTable.countOfTheBag("telegramuser_"+appUser.getTelegramUserId(), symbol), chatId);
                appUser.setSellUserState(SELL_CHANGE_COUNT);
                appUser.setActiveBuy(symbol + ":" + cost+":"+stockQuote.getShortName());
                appUserDAO.save(appUser);
            } else {
                sendAnswer("Чат-бот не знаком с такой ценной бумаги. Убедитесь, что вы хотите продать именно "+cmd+"\n Если окажется, что вас запрос верен, напишите нам в поддержку. \n Мы обязательно поможем", chatId);
                createTable.getInfoAboutBag("telegramuser_"+appUser.getTelegramUserId());
            }
        } else {
            sendAnswer("Такой акции нет в вашем инвестиционном портфеле \n В следующем сообщении будут приведены акции, находящиеся в вашем портфеле", chatId);
            createTable.getInfoAboutBag("telegramuser_"+appUser.getTelegramUserId());
        }

    }

    private void sellProof(AppUser appUser, String cmd, Long chatId){
        String info = "";
        if(cmd.equalsIgnoreCase("ДА")){
            String activeSell = appUser.getActiveBuy();
            int count = Integer.parseInt(parseStringFromBD(activeSell, 3));
            BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(parseStringFromBD(activeSell, 1)));
            BigDecimal countFromUser = BigDecimal.valueOf(count);

            createTable.addNoteAboutSell("telegramuser_"+appUser.getTelegramUserId(), parseStringFromBD(activeSell, 0), count);
            info = walletMain.topUpWallet(countFromUser.multiply(purchace), appUser);
            appUser.setSellUserState(NOT_SELL);
            appUserDAO.save(appUser);
        } else if (cmd.equalsIgnoreCase("НЕТ")) {
            info = "Сделка отменена. Если захотите опять что-то купить введите команду /buy";
            appUser.setSellUserState(NOT_SELL);
            appUserDAO.save(appUser);
        } else {
            info = "Введите Да или Нет. Или же команду /cancel";
        }
        sendAnswer(info, chatId);
    }

    private String parseStringFromBD(String s, int i){
        String[] parts = s.split(":");
        if(i==0){
            return parts[0]; //символ
        } else if (i==1) {
            return parts[1]; //стоимость

        } else if (i==2){
            return parts[2]; //shortname
        } else {
            return parts[3]; //количество
        }
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }
    private void sendAnswerWithInlineKeyboardYesOrNo(String output, long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        InlineKeyboardMarkup markupInLineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData("YES_BUTTON");

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("NO_BUTTON");

        rowInLine.add(yesButton);
        rowInLine.add(noButton);
        rowsInLine.add(rowInLine);
        markupInLineKeyboard.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(markupInLineKeyboard);


    }
}
