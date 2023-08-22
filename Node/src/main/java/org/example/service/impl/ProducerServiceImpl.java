package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.service.ProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import static broker.kartbllansh.model.RabbitQueue.*;

@Service
@Log4j
public class ProducerServiceImpl  implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    public ProducerServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void producerAnswer(SendMessage sendMessage) {
      rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage );
    }

    @Override
    public void producerAnswerWithCallBack(EditMessageText editMessageText) {
        rabbitTemplate.convertAndSend(ANSWER_CALLBACK_ANSWER, editMessageText);
    }

    @Override
    public void producerDeleteMessageAnswer(DeleteMessage deleteMessage) {
        rabbitTemplate.convertAndSend(DELETE_MESSAGE_ANSWER, deleteMessage);
    }
}
