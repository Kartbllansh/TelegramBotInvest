package org.example.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
//@NoArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.token}")
    private   String botToken;
    @Value("${bot.name}")
    private String botName;



    @Override
    public void onUpdateReceived(Update update) {
    var originalMessage = update.getMessage();
        System.out.println(originalMessage.getText());
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
    @Override
    public String getBotToken(){
        return botToken;
    }
}
