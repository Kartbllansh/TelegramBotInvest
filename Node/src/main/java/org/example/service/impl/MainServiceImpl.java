package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.SellUserState.*;
import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static org.example.enums.CommandService.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final StocksInformationService stockInformationService;
    private final CreateTable createTable;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;

    public MainServiceImpl(StocksInformationService stockInformationService, CreateTable createTable, ProducerService producerService, AppUserDAO appUserDAO, AppUserService appUserService) {
        this.stockInformationService = stockInformationService;
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
                onActionBuy(appUser, text, chatId);
            } else if (!NOT_SELL.equals(sellUserState)) {
                onActionSell(appUser, text, chatId);
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

    private void onActionSell(AppUser appUser, String text, Long chatId) {
        String info ="";
        switch (appUser.getSellUserState()){
            case SELL_CHANGE_COUNT:
                if (text.trim().matches("\\d+")){
                    long count = Long.parseLong(text);
                    info ="Успешно продано "+count+" акций Сбербанка";
                } else {
                    info = "Введено неправильно значение. Бот ожидает число.";
                }
                sendAnswer(info, chatId);
                sendAnswer("Подтверждение! Если вы подтверждаете продажу введите Да, если отменяете Нет", chatId);
                appUser.setBuyUserState(BUY_PROOF);
                appUserDAO.save(appUser);
                break;
            case SELL_CHANGE_STOCK:
                sendAnswer("Выбрана акция "+text, chatId);
                sendAnswer("Введите также количество акций, которое вы хоите продать. Сейчас у вас - 3", chatId);
                appUser.setSellUserState(SELL_CHANGE_COUNT);
                appUserDAO.save(appUser);
                break;
            case SELL_PROOF:
                if(text.equalsIgnoreCase("ДА")){
                    info = "ЕЕЕ. Успешная сделка!";
                    //TODO добавить для продажи работу с API и DB
                    //createTable.addNoteAboutSell("telegramuser_"+appUser.getId(), );
                    appUser.setBuyUserState(NOT_BUY);
                    appUserDAO.save(appUser);
                } else if (text.equalsIgnoreCase("НЕТ")) {
                    info = "Сделка отменена. Если захотите опять что-то купить введите команду /buy";
                    appUser.setBuyUserState(NOT_BUY);
                    appUserDAO.save(appUser);
                } else {
                    info = "Введите Да или Нет. Или же команду /cancel";
                }
                sendAnswer(info, chatId);
                break;
        }

        }


    private void onActionBuy(AppUser appUser, String cmd, Long chatId) {
        String info = "";
    switch (appUser.getBuyUserState()){

        case CHANGE_COUNT:
            if (cmd.trim().matches("\\d+")){
             long count = Long.parseLong(cmd);
             info ="Успешно куплено "+count+" акций";
                String temporaryValue = appUser.getActiveBuy();
                String newValue = temporaryValue+":"+count;
                appUser.setActiveBuy(newValue);
            } else {
                info = "Введено неправильно значение. Бот ожидает число.";
            }
            sendAnswer(info, chatId);
            sendAnswer("Подтверждение! Если вы подтверждаете покупку введите Да, если отменяете Нет", chatId);
            appUser.setBuyUserState(BUY_PROOF);
            appUserDAO.save(appUser);
            break;
        case CHANGE_STONKS:
            if(!(stockInformationService.getInfoAboutStocks(cmd)==null)) {
                String cost = stockInformationService.getInfoAboutStocks(cmd).getPrice();
                String symbol = stockInformationService.getInfoAboutStocks(cmd).getSymbol();
                info = "Цена ценной бумаги " + cmd + " равняется " + cost + " это цена на момент " + stockInformationService.getInfoAboutStocks(cmd).getLatestTradingDay();
                sendAnswer(info, chatId);
                sendAnswer("Какое количество акций вы хотите приобрести?", chatId);

                appUser.setBuyUserState(CHANGE_COUNT);
                appUser.setActiveBuy(symbol+":"+cost);
                appUserDAO.save(appUser);
            } else {
                sendAnswer("Ценной бумаги с таким символом нет", chatId);
            }
            break;
        case BUY_PROOF:
        if(cmd.equalsIgnoreCase("ДА")){
           info = "ЕЕЕ. Успешная сделка!";
           String activeBuy = appUser.getActiveBuy();
           createTable.addNoteAboutBuy("telegramuser_"+appUser.getId(), parseStringFromBD(activeBuy, 0), Integer.valueOf(parseStringFromBD(activeBuy, 2)), LocalDateTime.now(), Float.valueOf(parseStringFromBD(activeBuy, 1)));
           appUser.setBuyUserState(NOT_BUY);
           appUserDAO.save(appUser);
        } else if (cmd.equalsIgnoreCase("НЕТ")) {
           info = "Сделка отменена. Если захотите опять что-то купить введите команду /buy";
           appUser.setBuyUserState(NOT_BUY);
           appUserDAO.save(appUser);
        } else {
            info = "Введите Да или Нет. Или же команду /cancel";
        }
        sendAnswer(info, chatId);
        break;
    }

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
            return "Введите стоимость акции, которую хотите продать";
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

    private String parseStringFromBD(String s, int i){
        String[] parts = s.split(":");
        if(i==1){
            return parts[0];
        } else if (i==2) {
            return parts[1];

        } else {
            return parts[2];
        }
    }
}
