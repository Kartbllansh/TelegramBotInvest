package org.example.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface AnswerConsumer {
    void consume(SendMessage sendMessage);
    void consumeWithCallBack(EditMessageText editMessageText);
    void consumeDeleteMessage(DeleteMessage deleteMessage);
}
