package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.service.ProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static broker.kartbllansh.model.RabbitQueue.ANSWER_MESSAGE;

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
}
