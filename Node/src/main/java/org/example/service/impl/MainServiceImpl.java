package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.AppUserService;
import org.example.service.CreateTable;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static org.example.enums.CommandService.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final CreateTable createTable;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;

    public MainServiceImpl(CreateTable createTable, ProducerService producerService, AppUserDAO appUserDAO, AppUserService appUserService) {
        this.createTable = createTable;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";
        var serviceCommand = CommandService.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }
        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);



    }
    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = CommandService.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            return appUserService.registerUser(appUser);
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя.";
    }
    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена!";
    }



    @Override
    public void processDocMessage(Update update) {

    }

    @Override
    public void processPhotoMessage(Update update) {

    }

    private AppUser findOrSaveAppUser(Update update){
        var telegramUser = update.getMessage().getFrom();
        var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if(optional.isEmpty()){
            createTable.createTable("telegramUser_"+telegramUser.getId().toString());
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .lastName(telegramUser.getLastName())
                    .firstName(telegramUser.getFirstName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }
    private void doNewUniqTable(Long telegramId){

    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }
}
