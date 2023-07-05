package org.example.service.impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.CreateTable;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.example.entity.UserState.BASIC_STATE;

@Service
public class MainServiceImpl implements MainService {
    //private final TelegramUserDAO telegramUserDAO;
    private final CreateTable createTable;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;

    public MainServiceImpl( CreateTable createTable, ProducerService producerService, AppUserDAO appUserDAO) {
        this.createTable = createTable;
        //this.telegramUserDAO = telegramUserDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Hello from NODE TExt");
        producerService.producerAnswer(sendMessage);

        var textMessage = update.getMessage();
        var telegramUser = textMessage.getFrom();
        var appUser = findOrSaveAppUser(telegramUser);

    }

    @Override
    public void processDocMessage(Update update) {

    }

    @Override
    public void processPhotoMessage(Update update) {

    }

    private AppUser findOrSaveAppUser(User telegramUser){
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if(persistentAppUser == null){
            createTable.createTable("telegramUser_"+telegramUser.getId().toString());
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .lastName(telegramUser.getLastName())
                    .firstName(telegramUser.getFirstName())
                    //TODO доделать (изменить значение по умолчанию)
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }
    private void doNewUniqTable(Long telegramId){

    }
}
