package org.example.service;

import org.example.entity.AppUser;
import org.example.utils.ButtonForKeyboard;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface UtilsService {
    String cancelProcess(AppUser appUser);
    void sendAnswer(String output, Long chatId);
    void sendEditMessageAnswer(String output, Long chatId, long messageId);
    String help();
    String parseStringFromBD(String s, int i);
    void sendMessageAnswerWithInlineKeyboard(String output, long chatId, boolean area, ButtonForKeyboard... buttons);
    void sendEditMessageAnswerWithInlineKeyboard(String output, long chatId, long messageId,boolean area, ButtonForKeyboard... buttons);
    BigDecimal countSummaPurchase(String info);
    BigInteger countHowMuchStock(String info, AppUser appUser);

    void sendDeleteMessageAnswer(long chatId, long messageId);

}
