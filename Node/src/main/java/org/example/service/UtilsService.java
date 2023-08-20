package org.example.service;

import org.example.entity.AppUser;
import org.example.utils.ButtonForKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.math.BigDecimal;

public interface UtilsService {
    String cancelProcess(AppUser appUser);
    void sendAnswer(String output, Long chatId);

    String help();
    String parseStringFromBD(String s, int i);
    void sendMessageAnswerWithInlineKeyboard(String output, long chatId, ButtonForKeyboard... buttons);
    void sendEditMessageAnswerWithInlineKeyboard(String output, long chatId, long messageId, ButtonForKeyboard... buttons);
    BigDecimal countSummaPurchase(String info);
}
