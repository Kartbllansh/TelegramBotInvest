package org.example.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface ProducerService {
    void producerAnswer(SendMessage sendMessage);
    void producerAnswerWithCallBack(EditMessageText editMessageText);
    void producerDeleteMessageAnswer(DeleteMessage deleteMessage);
}
