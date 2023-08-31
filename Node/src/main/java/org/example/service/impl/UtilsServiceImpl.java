package org.example.service.impl;

import com.vdurmont.emoji.EmojiParser;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.ProducerService;
import org.example.service.UtilsService;
import org.example.dto.ButtonForKeyboard;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.NOT_SELL;
import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.WalletUserState.NOT_WALLET;
import static org.example.enums.BigMessage.HELP_MESSAGE;

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
        return "Команда отменена"+ EmojiParser.parseToUnicode(":leftwards_arrow_with_hook:");
    }

    @Override
    public void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);

    }


    @Override
    public void sendEditMessageAnswer(String output, Long chatId, long messageId) {
      EditMessageText editMessageText = new EditMessageText();
      editMessageText.setMessageId((int) messageId);
      editMessageText.setChatId(chatId);
      editMessageText.setText(output);
      producerService.producerAnswerWithCallBack(editMessageText);
    }
    // true -
    // false |
    private InlineKeyboardMarkup doButtonInlineKeyboard(boolean area, ButtonForKeyboard... buttons) {
        InlineKeyboardMarkup markupInlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        int buttonsPerRow = area ? 2 : 1; // Определяем количество кнопок в строке

        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (int i = 0; i < buttons.length; i++) {
            ButtonForKeyboard button = buttons[i];
            InlineKeyboardButton inlineButton = new InlineKeyboardButton();
            inlineButton.setText(button.getText());
            inlineButton.setCallbackData(button.getCallbackData());

            currentRow.add(inlineButton);

            if (currentRow.size() >= buttonsPerRow || i == buttons.length - 1) {
                rowsInline.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        markupInlineKeyboard.setKeyboard(rowsInline);
        return markupInlineKeyboard;
    }


    @Override
    public String help() {
        return HELP_MESSAGE;
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
    public void sendMessageAnswerWithInlineKeyboard(String output, long chatId, boolean area, ButtonForKeyboard... buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        sendMessage.setReplyMarkup(doButtonInlineKeyboard(area ,buttons));
        producerService.producerAnswer(sendMessage);
    }

    @Override
    public void sendEditMessageAnswerWithInlineKeyboard(String output, long chatId, long messageId,boolean area, ButtonForKeyboard... buttons) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(output);
        editMessageText.setMessageId((int) messageId);
        editMessageText.setReplyMarkup(doButtonInlineKeyboard(area, buttons));
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
