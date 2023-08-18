package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.service.CallBackMainService;
import org.example.service.ConsumerService;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static broker.kartbllansh.model.RabbitQueue.*;

@Log4j
@Service
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;
    private final ProducerService producerService;
    private final CallBackMainService callBackMainService;

    public ConsumerServiceImpl(MainService mainService, ProducerService producerService, CallBackMainService callBackMainService) {
        this.mainService = mainService;
        this.producerService = producerService;
        this.callBackMainService = callBackMainService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        mainService.processTextMessage(update);


    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdates(Update update) {
        log.info("Node ; DOC MESSAGE is receive");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("На данный момент бот не умеет работать с документами. \n Напишите в поддержку, если у вас есть идеи для функционала. \n Спасибо! ");
        producerService.producerAnswer(sendMessage);

    }

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdates(Update update) {
        log.info("Node ; PHOTO MESSAGE is receive");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("На данный момент бот не умеет работать с фотографиями. \n Напишите в поддержку, если у вас есть идеи для функционала. \n Спасибо! ");
        producerService.producerAnswer(sendMessage);
    }
    @Override
    @RabbitListener(queues = CALLBACK_QUERY_UPDATE)
    public void consumeCallBackUpdates(Update update){
        log.info("Node ; CallBack is received");
        callBackMainService.processCallBackQuery(update);
    }
}
