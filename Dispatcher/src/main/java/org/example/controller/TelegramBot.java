package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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
@PostConstruct
    private void doMenuWithCommands(){
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "welcome message from the bot"));
        listOfCommand.add(new BotCommand("/registration", "mail confirmation"));
        listOfCommand.add(new BotCommand("/help", "full command help"));
        listOfCommand.add(new BotCommand("/buy", "purchase of shares"));
        listOfCommand.add(new BotCommand("/sell", "sale of shares"));
        listOfCommand.add(new BotCommand("/wallet", "commands with your wallet"));
        listOfCommand.add(new BotCommand("/cancel", "cancel active commands"));
    try {
        this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
    } catch (TelegramApiException e) {
        log.error("error with menu with commands");
    }

}


    @Override
    public void onUpdateReceived(Update update) {
    updateConroller.processUpdate(update);
    }




    public Integer sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                return execute(message).getMessageId();
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
        return null;
    }
}
