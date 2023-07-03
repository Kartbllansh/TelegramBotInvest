package org.example.service.impl;

import org.example.controller.UpdateConroller;
import org.example.service.AnswerConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static broker.kartbllansh.model.RabbitQueue.ANSWER_MESSAGE;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateConroller updateConroller;

    public AnswerConsumerImpl(UpdateConroller updateConroller) {
        this.updateConroller = updateConroller;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        updateConroller.setView(sendMessage);


    }
}
