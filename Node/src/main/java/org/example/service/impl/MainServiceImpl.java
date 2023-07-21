package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.SellUserState.*;
import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static org.example.enums.CommandService.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final BuyOrSellService buyOrSellService;
    private final CreateTable createTable;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;

    public MainServiceImpl(BuyOrSellService buyOrSellService, CreateTable createTable, ProducerService producerService, AppUserDAO appUserDAO, AppUserService appUserService) {
        this.buyOrSellService = buyOrSellService;
        this.createTable = createTable;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
        var buyUserState = appUser.getBuyUserState();
        var sellUserState = appUser.getSellUserState();
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";
        var serviceCommand = CommandService.fromValue(text);
        var chatId = update.getMessage().getChatId();
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            if(!NOT_BUY.equals(buyUserState)) {
                buyOrSellService.onActionBuy(appUser, text, chatId);
            } else if (!NOT_SELL.equals(sellUserState)) {
                buyOrSellService.onActionSell(appUser, text, chatId);
            }
         else {
                output = processServiceCommand(appUser, text);
            }

        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        sendAnswer(output, chatId);



    }


    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = CommandService.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            log.info("Регистрация пользователя "+appUser.getUserName()+" с почтой "+appUser.getEmail());
            return appUserService.registerUser(appUser);
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            log.info("Новый пользователь с именем " + appUser.getUserName());
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";

        } else if(BUY.equals(serviceCommand)){
            appUser.setBuyUserState(CHANGE_STONKS);
            appUserDAO.save(appUser);
            return "Введите код акции, которую хотите купить";
        } else if (SELL.equals(serviceCommand)) {
            appUser.setSellUserState(SELL_CHANGE_STOCK);
            appUserDAO.save(appUser);
            sendAnswer(createTable.getInfoAboutBag("telegramUser_"+appUser.getTelegramUserId()), appUser.getTelegramUserId());
            return "Введите ключ акции, которую хотите продать";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя;\n"
                + "/buy - покупка ценной бумаги;\n"
                + "/sell - продажа ценной бумаги";
    }
    private String cancelProcess(AppUser appUser) {
        if(!appUser.getBuyUserState().equals(NOT_BUY)){
            appUser.setBuyUserState(NOT_BUY);
        }
        if (!appUser.getSellUserState().equals(NOT_SELL)){
            appUser.setSellUserState(NOT_SELL);
        }
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
                    .buyUserState(NOT_BUY)
                    .sellUserState(NOT_SELL)
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
