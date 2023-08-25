package org.example.service.impl;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.dao.StockQuoteRepository;
import org.example.entity.AppUser;
import org.example.entity.StockQuote;
import org.example.service.*;
import org.example.utils.ButtonForKeyboard;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.*;

@Log4j
@Service
public class BuyOrSellServiceImpl implements BuyOrSellService {
    private final AppUserDAO appUserDAO;
    private final WalletMain walletMain;
    private final StockServiceImpl stockService;
    private final UtilsService utilsService;
    private final AppUserStockService appUserStockService;

    public BuyOrSellServiceImpl(AppUserDAO appUserDAO, WalletMain walletMain, StockServiceImpl stockService, UtilsService utilsService, AppUserStockService appUserStockService) {
        this.appUserDAO = appUserDAO;
        this.walletMain = walletMain;
        this.stockService = stockService;
        this.utilsService = utilsService;
        this.appUserStockService = appUserStockService;
    }

    @Override
    public void onActionBuy(AppUser appUser, String cmd, Long chatId, long messageId) {
        switch (appUser.getBuyUserState()){
            case CHANGE_COUNT:
               buyChangeCount(appUser, cmd, chatId, messageId);
                break;
            case CHANGE_STOCK:
                buyChangeStocks(appUser, cmd, chatId, messageId);
                break;
            case BUY_PROOF:
                buyProof(appUser, cmd, chatId, messageId);
                break;
            case LOOK_ID_BUY:
                lookIdBuy(appUser, cmd, chatId, messageId);
                break;

        }

    }

    private void lookIdBuy(AppUser appUser, String cmd, Long chatId, long messageId) {
        List<StockQuote> list = stockService.searchOnCompany(cmd, 11);
        StringBuilder stringBuilder = new StringBuilder("Результат поиска: \n \n");
        List<ButtonForKeyboard> buttonForKeyboards = new ArrayList<>();
        if (!(list == null || list.isEmpty())) {

        for (StockQuote stockQuote : list) {
            stringBuilder.append("Компания ").append(stockQuote.getShortName()).append(" - ").append(stockQuote.getSecId()).append("\n");
            buttonForKeyboards.add(new ButtonForKeyboard("Купить " + stockQuote.getSecId(), stockQuote.getSecId()));

        }
        } else {
            stringBuilder.append("Ничего не найдено! \n");
        }
        buttonForKeyboards.add(new ButtonForKeyboard("Выбрать акцию самому", "BUY_ANOTHER_STOCK"));
        buttonForKeyboards.add(new ButtonForKeyboard("Отменить", "CANCEL"));

        utilsService.sendEditMessageAnswerWithInlineKeyboard(stringBuilder.toString(), chatId, Long.parseLong(appUser.getActiveBuy()), false, buttonForKeyboards.toArray(new ButtonForKeyboard[0]));
        utilsService.sendDeleteMessageAnswer(chatId, messageId);
    }

    @Override
    public void onActionSell(AppUser appUser, String cmd, Long chatId, long messageId) {
        switch (appUser.getSellUserState()){
            case SELL_CHANGE_COUNT:
                sellChangeCount(appUser, cmd, chatId, messageId);
                break;
            case SELL_CHANGE_STOCK:
               sellChangeStock(appUser, cmd, chatId, messageId);
                break;
            case SELL_PROOF:
                sellProof(appUser, cmd, chatId, messageId);
                break;
        }


    }

    private void buyChangeCount(AppUser appUser, String cmd, Long chatId, long messageId){
        String info = "";
        String oldActiveBuy = appUser.getActiveBuy();
        if (cmd.trim().matches("\\d+")){

            long count = Long.parseLong(cmd);
            String newValue = oldActiveBuy+":"+count;


            boolean oppornunityPurchase = walletMain.checkAbilityBuy(utilsService.countSummaPurchase(newValue), appUser);
            if(oppornunityPurchase) {
                info =":receipt:"+"Покупка "+count+" "+utilsService.parseStringFromBD(newValue, 2)+" ("+utilsService.parseStringFromBD(newValue, 0)+") "+":receipt:";
                utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info+" \n Подтверждение"+":white_large_square:"+ "\n Если вы подтверждаете покупку введите 'Да'("+":white_check_mark:"+"), если отменяете 'Нет'("+":x:"+")"), chatId, Long.parseLong(utilsService.parseStringFromBD(oldActiveBuy, 3 )), true, new ButtonForKeyboard(EmojiParser.parseToUnicode("Да("+":white_check_mark:"+")"), "YES_BUTTON_BUY"), new ButtonForKeyboard(EmojiParser.parseToUnicode("Нет("+":x:"+")"), "NO_BUTTON_BUY"));
                utilsService.sendDeleteMessageAnswer(chatId, messageId);
                appUser.setActiveBuy(newValue);

                appUser.setBuyUserState(BUY_PROOF);
                appUserDAO.save(appUser);
            } else {
                BigInteger abilCount = utilsService.countHowMuchStock(newValue,appUser);
               utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode("Вам не хватает средств на "+count+" акций"+":disappointed:")+ "\n "+walletMain.toKnowBalance(appUser)+", а совершить покупку вы хотите на сумму "+utilsService.countSummaPurchase(newValue)+"\n Вы можете купить "+abilCount+" акций "+utilsService.parseStringFromBD(newValue, 2), chatId, Long.parseLong(utilsService.parseStringFromBD(newValue, 3)), false, new ButtonForKeyboard("Купить " +abilCount+" акций", abilCount.toString()), new ButtonForKeyboard("Отменить покупку", "CANCEL"));
               utilsService.sendDeleteMessageAnswer(chatId, messageId);
            }
        } else {
            info = ":receipt:"+"Покупка "+utilsService.parseStringFromBD(oldActiveBuy, 2)+" ("+utilsService.parseStringFromBD(oldActiveBuy, 0)+") "+":receipt:"+"\n \n"+cmd+", Хм, что это за цифра такая"+":thinking:"+"\n Введите корректное число или отмените покупку";
            utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info), chatId, Long.parseLong(utilsService.parseStringFromBD(oldActiveBuy, 3)), false, new ButtonForKeyboard("Отменить", "CANCEL"));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        }


    }

    public void buyChangeStocks(AppUser appUser, String cmd, Long chatId, long messageId){
        String info = "";
        StockQuote stockQuote = stockService.getInfoAboutTicket(cmd);
        String messageIdFromDis = appUser.getActiveBuy();
        if(!(stockQuote==null)) {
            BigDecimal cost = stockQuote.getPrevLegalClosePrice();
            String symbol = stockQuote.getSecId();
            info = "Стоимость ценной бумаги " + cmd + " равняется " + cost + " это цена на момент " + stockQuote.getDate();
            String newActiveBuy = symbol+":"+cost+":"+stockQuote.getShortName()+":"+messageIdFromDis;
            utilsService.sendEditMessageAnswer(info+" \n \n Какое количество акций вы хотите приобрести? \n Максимум вы можете приобрести "+utilsService.countHowMuchStock(newActiveBuy, appUser), chatId, Long.parseLong(messageIdFromDis));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
            //utilsService.sendAnswer("Какое количество акций вы хотите приобрести?", chatId);
            appUser.setBuyUserState(CHANGE_COUNT);

            appUser.setActiveBuy(newActiveBuy);
            appUserDAO.save(appUser);
        } else {
            utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode("Чат-бот не знаком с такой ценной бумагой, как "+cmd+"  :robot_face:")+"\n  В ближайшее время мы попробуем добавить данную компанию в список доступных. \n Введите другую акцию или отмените покупку", chatId, Long.parseLong(messageIdFromDis), false, new ButtonForKeyboard("Отменить", "CANCEL"));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        }
    }

    private void buyProof(AppUser appUser, String cmd, Long chatId, long messageId){
        if(cmd.equalsIgnoreCase("ДА")){
         String info = buyProofYes(appUser);
        utilsService.sendEditMessageAnswer(EmojiParser.parseToUnicode(info), chatId, Long.parseLong(utilsService.parseStringFromBD(appUser.getActiveBuy(), 3)));
        utilsService.sendDeleteMessageAnswer(chatId, messageId);
        } else if (cmd.equalsIgnoreCase("НЕТ")) {
            String info = "Сделка отменена"+":x:"+ "\n Если захотите опять что-то купить введите команду /buy";
            utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info), chatId, Long.parseLong(utilsService.parseStringFromBD(appUser.getActiveBuy(), 3)), false, new ButtonForKeyboard("Buy", "BUY_COMMAND"));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
            appUser.setBuyUserState(NOT_BUY);
            appUserDAO.save(appUser);
        } else {
            String info = "Неверное значение"+":x:"+ "\n Если вы подтверждаете покупку введите 'Да'("+":white_check_mark:"+"), если отменяете 'Нет'("+":x:"+"). \n Если хотите отменить покупку выберите /cancel";
            utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info), chatId, Long.parseLong(utilsService.parseStringFromBD(appUser.getActiveBuy(), 3)), true, new ButtonForKeyboard(EmojiParser.parseToUnicode("Да("+":white_check_mark:"+")"), "YES_BUTTON_BUY"), new ButtonForKeyboard(EmojiParser.parseToUnicode("Нет("+":x:"+")"), "NO_BUTTON_BUY"), new ButtonForKeyboard("Отменить", "CANCEL") );
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        }

    }
    @Override
    public String buyProofYes(AppUser appUser){
        String activeBuy = appUser.getActiveBuy();
        int count = Integer.parseInt(utilsService.parseStringFromBD(activeBuy, 4));
        BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(utilsService.parseStringFromBD(activeBuy, 1)));
        BigDecimal countFromUser = BigDecimal.valueOf(count);
        appUserStockService.saveOrUpdateUserStock(appUser, utilsService.parseStringFromBD(activeBuy, 0), count, LocalDateTime.now(), purchace);
        //createTable.addNoteAboutBuy("telegramuser_"+appUser.getTelegramUserId(), utilsService.parseStringFromBD(activeBuy, 0), count, LocalDateTime.now(), purchace, utilsService.parseStringFromBD(activeBuy, 2));
        //String info = "Покупка выполнена успешна"+":white_check_mark:"+ "\n"+ walletMain.topDownWallet(purchace.multiply(countFromUser), appUser);
        String neInfo = ":white_check_mark:"+" Успешная покупка: "+utilsService.parseStringFromBD(activeBuy, 2)+"("+utilsService.parseStringFromBD(activeBuy, 0)+") "+count+" акций "+":white_check_mark:"+"\n \n "+walletMain.topDownWallet(purchace.multiply(countFromUser), appUser);

        appUser.setBuyUserState(NOT_BUY);
        appUserDAO.save(appUser);
        return neInfo;
    }
    private void sellChangeCount(AppUser appUser, String cmd, Long chatId, long messageId){
        String temporaryValue = appUser.getActiveBuy();
        String info = "";
        if (cmd.trim().matches("\\d+")){
            long count = Long.parseLong(cmd);

            String codeStocks =utilsService.parseStringFromBD(temporaryValue, 0);
            /*Long someResult = createTable.checkAboutCountSell(count, "telegramuser_"+appUser.getTelegramUserId(), codeStocks);*/
            int someResult = appUserStockService.checkAboutCountSell(appUser, (int) count, codeStocks);
            if(someResult>=0) {
                info = "Продажа " + count + " акций " + utilsService.parseStringFromBD(temporaryValue, 2)+"("+utilsService.parseStringFromBD(temporaryValue, 0)+")";
                utilsService.sendEditMessageAnswerWithInlineKeyboard(info+"Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId, Long.parseLong(utilsService.parseStringFromBD(temporaryValue, 3)), true, new ButtonForKeyboard("Да", "YES_BUTTON_SELL"), new ButtonForKeyboard("Нет", "NO_BUTTON_SELL"));
                utilsService.sendDeleteMessageAnswer(chatId, messageId);
                appUser.setSellUserState(SELL_PROOF);
                appUser.setActiveBuy(temporaryValue + ":" + count);
                appUserDAO.save(appUser);
            } else {
                utilsService.sendEditMessageAnswerWithInlineKeyboard("Нельзя продать"+count+" акций "+utilsService.parseStringFromBD(temporaryValue, 2)+", у вас их всего"+appUserStockService.countOfTheBag(appUser, codeStocks), chatId, Long.parseLong(utilsService.parseStringFromBD(temporaryValue, 3)), true, new ButtonForKeyboard("Продать все", "SELL_ALL_COMMAND"));
                utilsService.sendDeleteMessageAnswer(chatId, messageId);
            }
        } else {
            info = "Введено неправильно значение. Бот ожидает число.";
            utilsService.sendEditMessageAnswer(info, chatId, Long.parseLong(utilsService.parseStringFromBD(temporaryValue, 3)));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        }
    }
    private void sellChangeStock(AppUser appUser, String cmd, Long chatId, long messageId){
        String oldActiveBuy = appUser.getActiveBuy();
        if(appUserStockService.hasUserBoughtStock(appUser, cmd)) {
            StockQuote stockQuote = stockService.getInfoAboutTicket(cmd);
            if (!(stockQuote== null)) {
                BigDecimal cost = stockQuote.getPrevLegalClosePrice();
                String symbol = stockQuote.getSecId();
                utilsService.sendEditMessageAnswerWithInlineKeyboard("Выбрана акция " + cmd+"\n Введите также количество акций, которое вы хотите продать. \n Сейчас у вас "+appUserStockService.countOfTheBag(appUser, symbol), chatId, Long.parseLong(oldActiveBuy), true, new ButtonForKeyboard("Продать все", "SELL_ALL_COMMAND"));
                utilsService.sendDeleteMessageAnswer(chatId, messageId);
                appUser.setSellUserState(SELL_CHANGE_COUNT);
                appUser.setActiveBuy(symbol + ":" + cost+":"+stockQuote.getShortName()+":"+oldActiveBuy);
                appUserDAO.save(appUser);
            } else {
                utilsService.sendEditMessageAnswerWithInlineKeyboard("Чат-бот не знаком с такой ценной бумаги. \n Убедитесь, что вы хотите продать именно "+cmd+"\n И введите правильный ключ акции \n Если окажется, что вас запрос верен, напишите нам в поддержку. \n Мы обязательно поможем", chatId, Long.parseLong(oldActiveBuy), true, new ButtonForKeyboard("Список ваших акций", "LIST_OWN_STOCKS"));
                //createTable.getInfoAboutBag("telegramuser_"+appUser.getTelegramUserId());
                utilsService.sendDeleteMessageAnswer(chatId, messageId);
            }
        } else {
            utilsService.sendEditMessageAnswer("Такой акции нет в вашем инвестиционном портфеле \n Ваш портфель: \n "+appUserStockService.getInfoAboutBag(appUser), chatId, Long.parseLong(oldActiveBuy));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        }

    }

    private void sellProof(AppUser appUser, String cmd, Long chatId, long messageId){
        String messadeIdFrom = utilsService.parseStringFromBD(appUser.getActiveBuy(), 3);
        String info = "";
        if(cmd.equalsIgnoreCase("ДА")){
           info =  sellProofYes(appUser);
            utilsService.sendEditMessageAnswer(info, chatId, Long.parseLong(messadeIdFrom));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        } else if (cmd.equalsIgnoreCase("НЕТ")) {
            info = "Сделка отменена. Если захотите опять что-то продать введите команду /sell";
            utilsService.sendEditMessageAnswerWithInlineKeyboard(info, chatId, Long.parseLong(messadeIdFrom), true, new ButtonForKeyboard("Sell", "SELL_COMMAND"));
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
            appUser.setSellUserState(NOT_SELL);
            appUserDAO.save(appUser);
        } else {
            info = "Неверное значение"+":x:"+ "\n Если вы подтверждаете продажу введите 'Да'("+":white_check_mark:"+"), если отменяете 'Нет'("+":x:"+"). \n Если хотите отменить продажу выберите /cancel";
            utilsService.sendEditMessageAnswerWithInlineKeyboard(EmojiParser.parseToUnicode(info), chatId, Long.parseLong(utilsService.parseStringFromBD(appUser.getActiveBuy(), 3)), true, new ButtonForKeyboard(EmojiParser.parseToUnicode("Да("+":white_check_mark:"+")"), "YES_BUTTON_SELL"), new ButtonForKeyboard(EmojiParser.parseToUnicode("Нет("+":x:"+")"), "NO_BUTTON_SELL"), new ButtonForKeyboard("Отменить", "CANCEL") );
            utilsService.sendDeleteMessageAnswer(chatId, messageId);
        }

        utilsService.sendDeleteMessageAnswer(chatId, messageId);
    }
    public String sellProofYes(AppUser appUser){
        String activeSell = appUser.getActiveBuy();
        int count = Integer.parseInt(utilsService.parseStringFromBD(activeSell, 4));
        BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(utilsService.parseStringFromBD(activeSell, 1)));
        BigDecimal countFromUser = BigDecimal.valueOf(count);
        //TODO обработать исключения метода снизу
        appUserStockService.sellUserStock(appUser, utilsService.parseStringFromBD(activeSell, 0), count);
        //String info = walletMain.topUpWallet(countFromUser.multiply(purchace), appUser);
        String neInfo = ":white_check_mark:"+" Успешная покупка: "+utilsService.parseStringFromBD(activeSell, 2)+"("+utilsService.parseStringFromBD(activeSell, 0)+") "+count+" акций "+":white_check_mark:"+"\n \n "+walletMain.topUpWallet(purchace.multiply(countFromUser), appUser);
        appUser.setSellUserState(NOT_SELL);
        appUserDAO.save(appUser);
        return neInfo;
    }


}
