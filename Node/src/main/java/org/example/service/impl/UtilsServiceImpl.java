package org.example.service.impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.ProducerService;
import org.example.service.UtilsService;
import org.example.service.WalletMain;
import org.example.utils.ButtonForKeyboard;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.NOT_SELL;
import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.WalletUserState.NOT_WALLET;
@Service
public class UtilsServiceImpl implements UtilsService {
    private final AppUserDAO appUserDAO;
    private final ProducerService producerService;

    public UtilsServiceImpl(AppUserDAO appUserDAO, ProducerService producerService) {
        this.appUserDAO = appUserDAO;
        this.producerService = producerService;
    }

    @Override
    public String cancelProcess(AppUser appUser) {
        if(!appUser.getBuyUserState().equals(NOT_BUY)){
            appUser.setBuyUserState(NOT_BUY);
        }
        if (!appUser.getSellUserState().equals(NOT_SELL)){
            appUser.setSellUserState(NOT_SELL);
        }
        if(!appUser.getWalletUserState().equals(NOT_WALLET)){
            appUser.setWalletUserState(NOT_WALLET);
        }
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена!";
    }

    @Override
    public void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);

    }

    @Override
    public Integer sendAnswerWithId(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
        return sendMessage.getReplyToMessageId();
    }

    @Override
    public void sendEditMessageAnswer(String output, Long chatId, long messageId) {
      EditMessageText editMessageText = new EditMessageText();
      editMessageText.setMessageId((int) messageId);
      editMessageText.setChatId(chatId);
      editMessageText.setText(output);
      producerService.producerAnswerWithCallBack(editMessageText);
    }

    private InlineKeyboardMarkup doButtonInlineKeyboard(ButtonForKeyboard... buttons) {
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
        return markupInLineKeyboard;
    }

    @Override
    public String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя;\n"
                + "/wallet - получить информацию о вашем кошельке"
                + "/buy - покупка ценной бумаги;\n"
                + "/sell - продажа ценной бумаги";
    }

    @Override
    public String parseStringFromBD(String s, int i) {
        String[] parts = s.split(":");
        if(i==0){
            return parts[0]; //символ
        } else if (i==1) {
            return parts[1]; //стоимость

        } else if (i==2){
            return parts[2]; //shortname
        } else if(i==3) {
            return parts[3]; //messageId
        } else {
            return parts[4]; //количество
        }
    }

    @Override
    public void sendMessageAnswerWithInlineKeyboard(String output, long chatId, ButtonForKeyboard... buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        sendMessage.setReplyMarkup(doButtonInlineKeyboard(buttons));
        producerService.producerAnswer(sendMessage);
    }

    @Override
    public void sendEditMessageAnswerWithInlineKeyboard(String output, long chatId, long messageId, ButtonForKeyboard... buttons) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(output);
        editMessageText.setMessageId((int) messageId);
        editMessageText.setReplyMarkup(doButtonInlineKeyboard(buttons));
        producerService.producerAnswerWithCallBack(editMessageText);
    }

    @Override
    public BigDecimal countSummaPurchase(String activeBuy) {
        int count = Integer.parseInt(parseStringFromBD(activeBuy, 4));
        BigDecimal purchace = BigDecimal.valueOf(Double.parseDouble(parseStringFromBD(activeBuy, 1)));
        BigDecimal countFromUser = BigDecimal.valueOf(count);
        return purchace.multiply(countFromUser);
    }

    @Override
    public BigInteger countHowMuchStock(String activeBuy, AppUser appUser) {
        BigDecimal balance = appUser.getWalletMoney();
        BigDecimal purchase = BigDecimal.valueOf(Double.parseDouble(parseStringFromBD(activeBuy, 1)));
        BigDecimal result = balance.divideToIntegralValue(purchase);

        return result.toBigInteger();

    }

    @Override
    public void sendDeleteMessageAnswer(long chatId, long messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId((int) messageId);
        producerService.producerDeleteMessageAnswer(deleteMessage);
    }
}
