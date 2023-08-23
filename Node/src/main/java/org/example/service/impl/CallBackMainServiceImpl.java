package org.example.service.impl;

import com.vdurmont.emoji.EmojiParser;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.jpa.entity.StockQuote;
import org.example.service.*;
import org.example.utils.ButtonForKeyboard;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.SellUserState.*;
import static org.example.entity.UserState.WAIT_TO_AGREE_CONSENT;
import static org.example.entity.WalletUserState.NOT_WALLET;
import static org.example.entity.WalletUserState.WALLET_TOP_UP_CHANGE_COUNT;
import static org.example.enums.BigMessage.CONSENT_MESSAGE;
import static org.example.enums.BigMessage.LEARNING_MESSAGE;

@Service
public class CallBackMainServiceImpl implements CallBackMainService {
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final BuyOrSellService buyOrSellService;
    private final StockService stockService;
    private final CreateTable createTable;
    private final UtilsService utilsService;
    private final WalletMain walletMain;


    public CallBackMainServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, BuyOrSellService buyOrSellService, StockService stockService, CreateTable createTable, UtilsService utilsService, WalletMain walletMain) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;

        this.buyOrSellService = buyOrSellService;
        this.stockService = stockService;
        this.createTable = createTable;
        this.utilsService = utilsService;
        this.walletMain = walletMain;
    }

    @Override
    public void processCallBackQuery(Update update) {
        AppUser appUser = findOrSaveAppUserFromCallBack(update);
    long messageId = update.getCallbackQuery().getMessage().getMessageId();
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String callBackData = update.getCallbackQuery().getData();

    if(callBackData.equals("CANCEL")){
    String cancel = utilsService.cancelProcess(appUser);
    utilsService.sendEditMessageAnswer(cancel, chatId, messageId);
    }

        if(!appUser.getSellUserState().equals(NOT_SELL)) {
            processSell(appUser, messageId, chatId, callBackData);
        } else if(!appUser.getBuyUserState().equals(NOT_BUY)){
            processBuy(appUser, messageId, chatId, callBackData);
        } else if(!appUser.getWalletUserState().equals(NOT_WALLET)){
            processWallet(appUser, messageId, chatId, callBackData);
        } else if(appUser.getState().equals(WAIT_TO_AGREE_CONSENT)){
            processAgreeWithConsent(appUser, messageId, chatId, callBackData);
        }

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
            case "HELP_COMMAND":
                producerService.producerAnswerWithCallBack(doEditMessage(messageId,chatId, utilsService.help()));
                break;
            case "BUY_COMMAND":
                appUser.setBuyUserState(CHANGE_STOCK);
                appUserDAO.save(appUser);
                utilsService.sendEditMessageAnswer(appUser.getUserName()+", вы активировали команду /buy! \n"
                        +"Введите код акции, которую хотите купить", chatId, messageId);
                break;
            case "SELL_COMMAND":
                appUser.setSellUserState(SELL_CHANGE_STOCK);
                appUserDAO.save(appUser);
                utilsService.sendAnswer(createTable.getInfoAboutBag("telegramUser_"+appUser.getTelegramUserId()), appUser.getTelegramUserId());
                List<String> list = createTable.getAllKeysInBag("telegramUser_"+appUser.getTelegramUserId());
                List<ButtonForKeyboard> buttonsList = new ArrayList<>();
                String output = appUser.getUserName()+", вы активировали команду /sell! \n"
                        +"Введите ключ акции, которую хотите продать";

                for (String buttonText : list) {
                    buttonsList.add(new ButtonForKeyboard(buttonText, buttonText));
                }

                utilsService.sendMessageAnswerWithInlineKeyboard(output, chatId, buttonsList.toArray(new ButtonForKeyboard[0]));
                break;
            case "CONSENT_STATE":
                utilsService.sendEditMessageAnswerWithInlineKeyboard(CONSENT_MESSAGE, chatId, messageId, new ButtonForKeyboard("Соглашаюсь", "YES_BUTTON_CONSENT"), new ButtonForKeyboard("Отказываюсь", "NO_BUTTON_CONSENT"));
                appUser.setState(WAIT_TO_AGREE_CONSENT);
                appUserDAO.save(appUser);
                break;
            case "LEARNING_STATE":
                utilsService.sendMessageAnswerWithInlineKeyboard(LEARNING_MESSAGE, chatId, new ButtonForKeyboard("Не придумал", "BOUT"));
                break;
        }
    }

    private void processAgreeWithConsent(AppUser appUser, long messageId, long chatId, String callBackData) {
        switch (callBackData){
            case "YES_BUTTON_CONSENT":
                appUser.setIsActiveConsent(true);
                utilsService.sendEditMessageAnswer("Вы согласились с условиями", chatId, messageId);
                break;
            case "NO_BUTTON_CONSENT":
                appUser.setIsActiveConsent(false);
                utilsService.sendEditMessageAnswer("Вы отказались с условиями", chatId, messageId);
                break;
        }
    }

    private void processWallet(AppUser appUser, long messageId, long chatId, String callBackData) {
        switch (appUser.getWalletUserState()){
            case WALLET_CHANGE_CMD:
                if(callBackData.equals("TOP_UP_COMMAND")){
                    utilsService.sendEditMessageAnswer("Введите сумму, на которую хотите увеличить свой счет", chatId, messageId);
                    appUser.setWalletUserState(WALLET_TOP_UP_CHANGE_COUNT);
                    appUserDAO.save(appUser);
                } else {
                    String infoAboutBalance = walletMain.toKnowBalance(appUser);
                    //utilsService.sendAnswer(infoAboutBalance, chatId);
                    utilsService.sendEditMessageAnswer(infoAboutBalance, chatId, messageId);
                }
                break;
        }
    }

    private void processBuy(AppUser appUser, long messageId, long chatId, String callBackData) {
        switch (appUser.getBuyUserState()){
            case CHANGE_COUNT:
                String newValue = appUser.getActiveBuy()+":"+callBackData;
                String info ="Покупка "+callBackData+" "+utilsService.parseStringFromBD(newValue, 2)+" ("+utilsService.parseStringFromBD(newValue, 0)+") ";
                utilsService.sendEditMessageAnswerWithInlineKeyboard(info+"\n Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId,messageId, new ButtonForKeyboard("Да", "YES_BUTTON_SELL"), new ButtonForKeyboard("Нет", "NO_BUTTON_SELL"));
                appUser.setActiveBuy(newValue);
                appUser.setBuyUserState(BUY_PROOF);
                appUserDAO.save(appUser);
                break;
            case CHANGE_STOCK:
                if(callBackData.equals("RECOGNIZE_TICKET")){
                    List<StockQuote> list = stockService.fuzzysearchCompany("сбербанк");
                    StringBuilder stringBuilder = new StringBuilder();
                    for(StockQuote stockQuote : list){
                      stringBuilder.append(stockQuote.getShortName()+" - "+stockQuote.getSecId());

                    }
                    utilsService.sendAnswer(stringBuilder.toString(), chatId);
                }
                break;
        }
    }

    private void processSell(AppUser appUser, long messageId, long chatId, String callBackData) {
        switch (appUser.getSellUserState()){
            case SELL_CHANGE_STOCK:
                if (callBackData.equals("LIST_OWN_STOCK")){
                    String output = createTable.getInfoAboutBag("telegramUser_"+appUser.getTelegramUserId());
                    utilsService.sendEditMessageAnswerWithInlineKeyboard(output, chatId, messageId, new ButtonForKeyboard("Отмена продажи", "CANCEL"));
                }


                StockQuote stockQuote = stockService.getInfoAboutTicket(callBackData);
                if (!(stockQuote== null)) {
                    BigDecimal cost = stockQuote.getPrevLegalClosePrice();
                    String symbol = stockQuote.getSecId();
                    String output = "Выбрана акция " + callBackData+" \n Введите также количество акций, которое вы хотите продать. Сейчас у вас "+createTable.countOfTheBag("telegramuser_"+appUser.getTelegramUserId(), symbol);
                    utilsService.sendEditMessageAnswerWithInlineKeyboard(output, chatId, messageId, new ButtonForKeyboard("Продать все", "SELL_ALL_COMMAND"));

                    appUser.setSellUserState(SELL_CHANGE_COUNT);
                    appUser.setActiveBuy(symbol + ":" + cost+":"+stockQuote.getShortName());
                    appUserDAO.save(appUser);
                } else {
                    String output = "Чат-бот не знаком с такой ценной бумаги. Убедитесь, что вы хотите продать именно " + callBackData + "\n Если окажется, что вас запрос верен, напишите нам в поддержку. \n Мы обязательно поможем";
                    utilsService.sendEditMessageAnswerWithInlineKeyboard(output, chatId, messageId, new ButtonForKeyboard("Список ваших акций", "LIST_OWN_STOCKS"));
                    //createTable.getInfoAboutBag("telegramuser_" + appUser.getTelegramUserId());
                }

                break;
            case SELL_CHANGE_COUNT:
                if (callBackData.equals("SELL_ALL_COMMAND")){
                    String temporaryValue = appUser.getActiveBuy();
                    String symbol = utilsService.parseStringFromBD(temporaryValue, 0);
                    long count = createTable.countOfTheBag("telegramuser_"+appUser.getTelegramUserId(), symbol);
                    appUser.setSellUserState(SELL_PROOF);
                    appUser.setActiveBuy(temporaryValue + ":" + count);
                    appUserDAO.save(appUser);
                    utilsService.sendEditMessageAnswerWithInlineKeyboard("Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId,messageId, new ButtonForKeyboard("Да", "YES_BUTTON_SELL"), new ButtonForKeyboard("Нет", "NO_BUTTON_SELL"));
                }
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
     producerService.producerAnswerWithCallBack(doEditMessage(messageId, chatId, EmojiParser.parseToUnicode(buyOrSellService.buyProofYes(appUser))));
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
