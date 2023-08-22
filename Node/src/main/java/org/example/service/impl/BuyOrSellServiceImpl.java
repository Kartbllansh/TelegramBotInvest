package org.example.service.impl;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.jpa.entity.StockQuote;
import org.example.service.*;
import org.example.utils.ButtonForKeyboard;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.*;

@Log4j
@Service
public class BuyOrSellServiceImpl implements BuyOrSellService {
    private final AppUserDAO appUserDAO;
    private final CreateTable createTable;
    private final WalletMain walletMain;
    private final StockServiceImpl stockService;
    private final UtilsService utilsService;

    public BuyOrSellServiceImpl( AppUserDAO appUserDAO, CreateTable createTable, WalletMain walletMain, StockServiceImpl stockService, UtilsService utilsService) {
        this.appUserDAO = appUserDAO;
        this.createTable = createTable;
        this.walletMain = walletMain;
        this.stockService = stockService;
        this.utilsService = utilsService;
    }

    @Override
    public void onActionBuy(AppUser appUser, String cmd, Long chatId, long messageId) {
        switch (appUser.getBuyUserState()){
            case CHANGE_COUNT:
               buyChangeCount(appUser, cmd, chatId, messageId);
                break;
            case CHANGE_STONKS:
                buyChangeStocks(appUser, cmd, chatId, messageId);
                break;
            case BUY_PROOF:
                buyProof(appUser, cmd, chatId, messageId);
                break;
        }

    }

    @Override
    public void onActionSell(AppUser appUser, String cmd, Long chatId, long messageId) {
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

    private void buyChangeCount(AppUser appUser, String cmd, Long chatId, long messageId){
        String info = "";
        String oldActiveBuy = appUser.getActiveBuy();
        if (cmd.trim().matches("\\d+")){

            long count = Long.parseLong(cmd);
            String newValue = oldActiveBuy+":"+count;

            //TODO настроить Id  в базе данных

            boolean oppornunityPurchase = walletMain.checkAbilityBuy(utilsService.countSummaPurchase(newValue), appUser);
            if(oppornunityPurchase) {
                info =":receipt:"+"Покупка "+count+" "+utilsService.parseStringFromBD(newValue, 2)+" ("+utilsService.parseStringFromBD(newValue, 0)+") "+":receipt:";
                utilsService.sendMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info+" \n Подтверждение"+":white_large_square:"+ "\n Если вы подтверждаете покупку введите 'Да'("+":white_check_mark:"+"), если отменяете 'Нет'("+":x:"+")"), chatId, new ButtonForKeyboard(EmojiParser.parseToUnicode("Да("+":white_check_mark:"+")"), "YES_BUTTON_BUY"), new ButtonForKeyboard(EmojiParser.parseToUnicode("Нет("+":x:"+")"), "NO_BUTTON_BUY"));
                appUser.setActiveBuy(newValue);

                appUser.setBuyUserState(BUY_PROOF);
                appUserDAO.save(appUser);
            } else {
                BigInteger abilCount = utilsService.countHowMuchStock(newValue,appUser);
                //TODO добавить кнопки, чтобы пользователь мог понять почему ему не хватило средств
               utilsService.sendMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode("Вам не хватает средств на "+count+" акций"+":disappointed:")+ "\n "+walletMain.toKnowBalance(appUser)+", а совершить покупку вы хотите на сумму "+utilsService.countSummaPurchase(newValue)+"\n Вы можете купить "+abilCount+" акций "+utilsService.parseStringFromBD(newValue, 2), chatId, new ButtonForKeyboard("Купить " +abilCount+" акций", abilCount.toString()), new ButtonForKeyboard("Отменить покупку", "CANCEL"));
            }
        } else {
            info = ":receipt:"+"Покупка "+utilsService.parseStringFromBD(oldActiveBuy, 2)+" ("+utilsService.parseStringFromBD(oldActiveBuy, 0)+") "+":receipt:"+"\n \n"+cmd+", Хм, что это за цифра такая"+":thinking:"+"\n Введите корректное число или отмените покупку";
            utilsService.sendAnswer(EmojiParser.parseToUnicode(info), chatId);
        }


    }
//TODO настроить ограничение времени хранения поля activeBuy
    //TODO настроить EditMessage
    private void buyChangeStocks(AppUser appUser, String cmd, Long chatId, long messageId){
        String info = "";
        StockQuote stockQuote = stockService.getInfoAboutTicket(cmd);
        if(!(stockQuote==null)) {
            BigDecimal cost = stockQuote.getPrevLegalClosePrice();
            String symbol = stockQuote.getSecId();
            info = "Стоимость ценной бумаги " + cmd + " равняется " + cost + " это цена на момент " + stockQuote.getDate();

            utilsService.sendAnswer(info+" \n \n Какое количество акций вы хотите приобрести?", chatId);
            //utilsService.sendAnswer("Какое количество акций вы хотите приобрести?", chatId);
            appUser.setBuyUserState(CHANGE_COUNT);
            appUser.setActiveBuy(symbol+":"+cost+":"+stockQuote.getShortName());
            appUserDAO.save(appUser);
        } else {
            utilsService.sendMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode("Чат-бот не знаком с такой ценной бумаги"+":robot:")+"\n  В ближайшее время мы попробуем добавить данную компанию в список доступных. \n Введите другую акцию или отмените покупку", chatId, new ButtonForKeyboard("Отменить", "CANCEL"));
            //TODO добавить лог для записи, какие акции хотели купить
        }
    }

    private void buyProof(AppUser appUser, String cmd, Long chatId, long messageId){

        if(cmd.equalsIgnoreCase("ДА")){
         String info = buyProofYes(appUser);
        utilsService.sendAnswer(EmojiParser.parseToUnicode(info), chatId);
        } else if (cmd.equalsIgnoreCase("НЕТ")) {
            String info = "Сделка отменена"+":x:"+ "\n Если захотите опять что-то купить введите команду /buy";
            utilsService.sendMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info), chatId, new ButtonForKeyboard("Buy", "BUY_COMMAND"));
            appUser.setBuyUserState(NOT_BUY);
            appUserDAO.save(appUser);
        } else {
            String info = "Неверное значение"+":x:"+ "\n Если вы подтверждаете покупку введите 'Да'("+":white_check_mark:"+"), если отменяете 'Нет'("+":x:"+"). \n Если хотите отменить покупку выберите /cancel";
            utilsService.sendMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info), chatId, new ButtonForKeyboard(EmojiParser.parseToUnicode("Да("+":white_check_mark:"+")"), "YES_BUTTON_BUY"), new ButtonForKeyboard(EmojiParser.parseToUnicode("Нет("+":x:"+")"), "NO_BUTTON_BUY"), new ButtonForKeyboard("Отменить", "CANCEL") );
        }

    }
    @Override
    public String buyProofYes(AppUser appUser){
        String activeBuy = appUser.getActiveBuy();
        int count = Integer.parseInt(utilsService.parseStringFromBD(activeBuy, 3));
        BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(utilsService.parseStringFromBD(activeBuy, 1)));
        BigDecimal countFromUser = BigDecimal.valueOf(count);

        createTable.addNoteAboutBuy("telegramuser_"+appUser.getTelegramUserId(), utilsService.parseStringFromBD(activeBuy, 0), count, LocalDateTime.now(), purchace, utilsService.parseStringFromBD(activeBuy, 2));
        String info = "Покупка выполнена успешна"+":white_check_mark:"+ "\n"+ walletMain.topDownWallet(purchace.multiply(countFromUser), appUser);
        String neInfo = ":white_check_mark:"+" Успешная покупка: "+utilsService.parseStringFromBD(activeBuy, 2)+"("+utilsService.parseStringFromBD(activeBuy, 0)+") "+count+" акций "+":white_check_mark:"+"\n \n "+walletMain.topDownWallet(purchace.multiply(countFromUser), appUser);
        //TODO настроить текстовые ответы

        appUser.setBuyUserState(NOT_BUY);
        appUserDAO.save(appUser);
        return neInfo;
    }
//TODO replyKeyboard возможность отменить команды
    private void sellChangeCount(AppUser appUser, String cmd, Long chatId){
        String info = "";
        if (cmd.trim().matches("\\d+")){
            long count = Long.parseLong(cmd);
            String temporaryValue = appUser.getActiveBuy();
            String codeStocks =utilsService.parseStringFromBD(temporaryValue, 0);
            Long someResult = createTable.checkAboutCountSell(count, "telegramuser_"+appUser.getTelegramUserId(), codeStocks);
            if(someResult>=0) {
                info = "Продажа " + count + " акций " + utilsService.parseStringFromBD(temporaryValue, 2)+"("+utilsService.parseStringFromBD(temporaryValue, 0)+")";

                utilsService.sendAnswer(info, chatId);
                utilsService.sendMessageAnswerWithInlineKeyboard("Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId, new ButtonForKeyboard("Да", "YES_BUTTON_SELL"), new ButtonForKeyboard("Нет", "NO_BUTTON_SELL"));
                appUser.setSellUserState(SELL_PROOF);
                appUser.setActiveBuy(temporaryValue + ":" + count);
                appUserDAO.save(appUser);
            } else {
                utilsService.sendAnswer("Нельзя продать такое количество акций, у вас их меньше", chatId);
                //TODO подсказка почему пользователь мог ошибиться
            }
        } else {
            info = "Введено неправильно значение. Бот ожидает число.";
            utilsService.sendAnswer(info, chatId);
        }
    }
    private void sellChangeStock(AppUser appUser, String cmd, Long chatId){
        if(createTable.checkAboutCodeStock("telegramuser_"+appUser.getTelegramUserId(), cmd)) {
            StockQuote stockQuote = stockService.getInfoAboutTicket(cmd);
            if (!(stockQuote== null)) {
                BigDecimal cost = stockQuote.getPrevLegalClosePrice();
                String symbol = stockQuote.getSecId();
                utilsService.sendAnswer("Выбрана акция " + cmd, chatId);
                utilsService.sendAnswer("Введите также количество акций, которое вы хотите продать. Сейчас у вас "+createTable.countOfTheBag("telegramuser_"+appUser.getTelegramUserId(), symbol), chatId);
                appUser.setSellUserState(SELL_CHANGE_COUNT);
                appUser.setActiveBuy(symbol + ":" + cost+":"+stockQuote.getShortName());
                appUserDAO.save(appUser);
            } else {
                utilsService.sendAnswer("Чат-бот не знаком с такой ценной бумаги. Убедитесь, что вы хотите продать именно "+cmd+"\n Если окажется, что вас запрос верен, напишите нам в поддержку. \n Мы обязательно поможем", chatId);
                createTable.getInfoAboutBag("telegramuser_"+appUser.getTelegramUserId());
            }
        } else {
            utilsService.sendAnswer("Такой акции нет в вашем инвестиционном портфеле \n В следующем сообщении будут приведены акции, находящиеся в вашем портфеле", chatId);
            createTable.getInfoAboutBag("telegramuser_"+appUser.getTelegramUserId());
        }

    }

    private void sellProof(AppUser appUser, String cmd, Long chatId){
        String info = "";
        if(cmd.equalsIgnoreCase("ДА")){
           info =  sellProofYes(appUser);
        } else if (cmd.equalsIgnoreCase("НЕТ")) {
            info = "Сделка отменена. Если захотите опять что-то продать введите команду /buy";
            appUser.setSellUserState(NOT_SELL);
            appUserDAO.save(appUser);
        } else {
            info = "Введите Да или Нет. Или же команду /cancel";
        }
        utilsService.sendAnswer(info, chatId);
    }
    public String sellProofYes(AppUser appUser){
        String activeSell = appUser.getActiveBuy();
        int count = Integer.parseInt(utilsService.parseStringFromBD(activeSell, 3));
        BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(utilsService.parseStringFromBD(activeSell, 1)));
        BigDecimal countFromUser = BigDecimal.valueOf(count);

        createTable.addNoteAboutSell("telegramuser_"+appUser.getTelegramUserId(), utilsService.parseStringFromBD(activeSell, 0), count);
        String info = walletMain.topUpWallet(countFromUser.multiply(purchace), appUser);
        appUser.setSellUserState(NOT_SELL);
        appUserDAO.save(appUser);
        return info;
    }


}
