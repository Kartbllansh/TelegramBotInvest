package org.example.service.impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.jpa.entity.StockQuote;
import org.example.service.*;
import org.example.utils.ButtonForKeyboard;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.NOT_SELL;
import static org.example.entity.SellUserState.SELL_CHANGE_COUNT;

@Service
public class CallBackMainServiceImpl implements CallBackMainService {
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final BuyOrSellService buyOrSellService;
    private final StockService stockService;
    private final CreateTable createTable;


    public CallBackMainServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, BuyOrSellService buyOrSellService, StockService stockService, CreateTable createTable) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;

        this.buyOrSellService = buyOrSellService;
        this.stockService = stockService;
        this.createTable = createTable;
    }

    @Override
    public void processCallBackQuery(Update update) {
        AppUser appUser = findOrSaveAppUserFromCallBack(update);
    long messageId = update.getCallbackQuery().getMessage().getMessageId();
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String callBackData = update.getCallbackQuery().getData();
    if(!appUser.getSellUserState().equals(NOT_SELL)){
        processSell(appUser, messageId, chatId, callBackData);
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
                producerService.producerAnswerWithCallBack(doEditMessage(messageId,chatId, "Список доступных команд:\n"
                        + "/cancel - отмена выполнения текущей команды;\n"
                        + "/registration - регистрация пользователя;\n"
                        + "/wallet - получить информацию о вашем кошельке"
                        + "/buy - покупка ценной бумаги;\n"
                        + "/sell - продажа ценной бумаги"));
                break;
        }
    }

    private void processSell(AppUser appUser, long messageId, long chatId, String callBackData) {
        switch (appUser.getSellUserState()){
            case SELL_CHANGE_STOCK:
                StockQuote stockQuote = stockService.getInfoAboutTicket(callBackData);
                if (!(stockQuote== null)) {
                    BigDecimal cost = stockQuote.getPrevLegalClosePrice();
                    String symbol = stockQuote.getSecId();
                    String output = "Выбрана акция " + callBackData+" \n Введите также количество акций, которое вы хотите продать. Сейчас у вас "+createTable.countOfTheBag("telegramuser_"+appUser.getTelegramUserId(), symbol);
                    sendAnswerWithInlineKeyboard(output, chatId, messageId, new ButtonForKeyboard("Продать все", "SELL_ALL_COMMAND"));
                    //TODO обработать кнопку SELL_ALL_COMMAND
                    appUser.setSellUserState(SELL_CHANGE_COUNT);
                    appUser.setActiveBuy(symbol + ":" + cost+":"+stockQuote.getShortName());
                    appUserDAO.save(appUser);
                } else {
                    String output = "Чат-бот не знаком с такой ценной бумаги. Убедитесь, что вы хотите продать именно " + callBackData + "\n Если окажется, что вас запрос верен, напишите нам в поддержку. \n Мы обязательно поможем";
                    sendAnswerWithInlineKeyboard(output, chatId, messageId, new ButtonForKeyboard("Список ваших акций", "LIST_OWN_STOCKS"));
                    //TODO work with button LIST_OWN_STOCK
                    //createTable.getInfoAboutBag("telegramuser_" + appUser.getTelegramUserId());
                }

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

    public void sendAnswerWithInlineKeyboard(String output, long chatId, long messageId, ButtonForKeyboard... buttons) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(output);
        editMessageText.setMessageId((int) messageId);
        InlineKeyboardMarkup markupInLineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        for (ButtonForKeyboard button : buttons) {
            InlineKeyboardButton inlineButton = new InlineKeyboardButton();
            inlineButton.setText(button.getText());
            inlineButton.setCallbackData(button.getCallbackData());


            rowInLine.add(inlineButton);

        }
        rowsInLine.add(rowInLine);

        markupInLineKeyboard.setKeyboard(rowsInLine);
        editMessageText.setReplyMarkup(markupInLineKeyboard);
        producerService.producerAnswerWithCallBack(editMessageText);
    }

}
