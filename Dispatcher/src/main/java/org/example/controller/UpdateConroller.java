package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.UpdateProducer;
import org.example.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import static broker.kartbllansh.model.RabbitQueue.*;
import static org.example.entity.BuyUserState.NOT_BUY;
import static org.example.entity.SellUserState.NOT_SELL;

@Component
@Log4j
public class UpdateConroller {

    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;
    private TelegramBot telegramBot;
    private final AppUserDAO appUserDAO;



    public UpdateConroller(MessageUtils messageUtils, UpdateProducer updateProducer, AppUserDAO appUserDAO) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;

        this.appUserDAO = appUserDAO;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }

        if (update.hasMessage()) {
            distributeMessagesByType(update);
        } else if (update.hasCallbackQuery()) {
            processCallBackQuery(update);
        } else {
            log.error("Unsupported message type is received: " + update);
        }
    }

    private void processCallBackQuery(Update update) {
        updateProducer.produce(CALLBACK_QUERY_UPDATE, update);
    }

    private void distributeMessagesByType(Update update) {
        var message = update.getMessage();
        if (message.hasText()) {

            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
      var sendMessage = messageUtils.generateSendMessageWithText(update, "Неподдерживаемый тип сообщений");
      setView(sendMessage);

    }

    public void setView(SendMessage sendMessage) {
        //TODO попробовать перенести запись Id в Node
        Integer messageId =  telegramBot.sendAnswerMessage(sendMessage);
        processSetMessageId(sendMessage, messageId);
    }

    private void processSetMessageId(SendMessage sendMessage, Integer messageId) {
        var optional = appUserDAO.findByTelegramUserId(Long.valueOf(sendMessage.getChatId()));
        if(optional.isEmpty()){
            log.error("ProccesSetMessageID user is null");
        }else {
            if(!optional.get().getBuyUserState().equals(NOT_BUY) || !optional.get().getSellUserState().equals(NOT_SELL)){
                optional.get().setActiveBuy(messageId.toString());
                appUserDAO.save(optional.get());
            }

        }

    }

    public void setViewWithCallBack(EditMessageText editMessageText){
        try {
            telegramBot.execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public void setViewDeleteMessage(DeleteMessage deleteMessage){
        try {
            telegramBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен! Обрабатывается...");
        setView(sendMessage);
    }
    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        //setFileIsReceivedView(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        //setFileIsReceivedView(update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }
}
