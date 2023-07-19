package org.example.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
@Log4j
@Component

//@NoArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;
@Autowired
    public TelegramBot(@Value("${bot.token}") String botToken){
        super(botToken);
        log.info("Constructor-token work");
    }
    @Autowired
    @Override
    public String getBotUsername() {
    log.info("check username");
        return botName;
    }

    private UpdateConroller updateConroller;
    @Autowired
    public void setUpdateConroller(UpdateConroller updateConroller) {
        this.updateConroller = updateConroller;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (updateConroller != null) {
            updateConroller.registerBot(this);
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
    updateConroller.processUpdate(update);
    }




    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
