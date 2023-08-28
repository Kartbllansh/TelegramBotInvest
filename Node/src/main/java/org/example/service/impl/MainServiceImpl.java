package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.*;
import org.example.dto.ButtonForKeyboard;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.entity.BuyUserState.*;
import static org.example.entity.SellUserState.*;
import static org.example.entity.UserState.BASIC_STATE;
import static org.example.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static org.example.entity.WalletUserState.NOT_WALLET;
import static org.example.entity.WalletUserState.WALLET_CHANGE_CMD;
import static org.example.enums.CommandService.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final WalletService walletService;
    private final BuyOrSellService buyOrSellService;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;
    private final UtilsService utilsService;
    private final AppUserStockService appUserStockService;

    public MainServiceImpl(WalletService walletService, BuyOrSellService buyOrSellService, AppUserDAO appUserDAO, AppUserService appUserService, UtilsService utilsService, AppUserStockService appUserStockService) {
        this.walletService = walletService;
        this.buyOrSellService = buyOrSellService;
        this.appUserDAO = appUserDAO;
        this.appUserService = appUserService;
        this.utilsService = utilsService;
        this.appUserStockService = appUserStockService;
    }
//c
    @Override
    public void processTextMessage(Update update) {
        long messageId = update.getMessage().getMessageId();
        var appUser = findOrSaveAppUser(update);
        var buyUserState = appUser.getBuyUserState();
        var sellUserState = appUser.getSellUserState();
        var walletUserState = appUser.getWalletUserState();
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";
        var serviceCommand = CommandService.fromValue(text);
        var chatId = update.getMessage().getChatId();
        checlAboutConsent(appUser, chatId);
        if (CANCEL.equals(serviceCommand)) {
            output = utilsService.cancelProcess(appUser);
            utilsService.sendAnswer(output, chatId);
        } else if (BASIC_STATE.equals(userState)) {

            if(!NOT_BUY.equals(buyUserState)) {
                buyOrSellService.onActionBuy(appUser, text, chatId, messageId);
            } else if (!NOT_SELL.equals(sellUserState)) {
                buyOrSellService.onActionSell(appUser, text, chatId, messageId);

            } else if (!NOT_WALLET.equals(walletUserState)) {
                walletService.onActiveWallet(appUser, text, chatId, messageId);
            } else {
                processServiceCommand(appUser, text, chatId, messageId);
            }

        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
            utilsService.sendAnswer(output, chatId);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! \n"
                    +  "Введите /cancel и попробуйте снова! \n"
                    +"Либо введите /help и посмотрите допустимые команды";
            utilsService.sendAnswer(output, chatId);
        }





    }

    private void checlAboutConsent(AppUser appUser, long chatId) {
        boolean check = appUser.getIsActiveConsent();
        if(!check){
          utilsService.sendMessageAnswerWithInlineKeyboard("Прежде чем использовать бота прочитайте правила использования бота", chatId, true, new ButtonForKeyboard("Правила", "CONSENT_STATE"));
        }
    }

    private void processServiceCommand(AppUser appUser, String cmd, long chatId, long messageId) {
        var serviceCommand = CommandService.fromValue(cmd);
        if(serviceCommand==null){
            utilsService.sendMessageAnswerWithInlineKeyboard("Неизвестная команда! Чтобы посмотреть список доступных команд введите /help", chatId, true, new ButtonForKeyboard("Help", "HELP_COMMAND"));
        }
        log.info("MESSAGE"+cmd);
        switch (Objects.requireNonNull(serviceCommand)){
            case START:
                log.info("Новый пользователь с именем " + appUser.getUserName());
                utilsService.sendMessageAnswerWithInlineKeyboard("Приветствую, "+appUser.getUserName()+ "!\n {тут будет красивое вступление} \n Чтобы посмотреть список доступных команд введите /help", chatId, false, new ButtonForKeyboard("Согласие", "CONSENT_STATE"));
                break;
            case REGISTRATION:
                log.info("Регистрация пользователя "+appUser.getUserName()+" с почтой "+appUser.getEmail());
                utilsService.sendAnswer(appUserService.registerUser(appUser), chatId);
                appUser.setState(WAIT_FOR_EMAIL_STATE);
                appUserDAO.save(appUser);
                break;
            case HELP:
                utilsService.sendAnswer(utilsService.help(), chatId);
                break;
            case BUY:
                appUser.setBuyUserState(CHANGE_STOCK);
                appUserDAO.save(appUser);
                utilsService.sendMessageAnswerWithInlineKeyboard(appUser.getUserName()+", вы активировали команду /buy! \n"
                        +"Введите код акции, которую хотите купить", chatId, true, new ButtonForKeyboard("Узнать ticket(not work)", "RECOGNIZE_TICKET"));

                break;
            case SELL:
                appUser.setSellUserState(SELL_CHANGE_STOCK);
                appUserDAO.save(appUser);
                utilsService.sendAnswer(appUserStockService.getInfoAboutBag(appUser), appUser.getTelegramUserId());
                List<String> list = appUserStockService.getAllKeysInBag(appUser);
                List<ButtonForKeyboard> buttonsList = new ArrayList<>();
                String output = appUser.getUserName()+", вы активировали команду /sell! \n"
                        +"Введите ключ акции, которую хотите продать";

                for (String buttonText : list) {
                    buttonsList.add(new ButtonForKeyboard(buttonText, buttonText));
                }

                utilsService.sendMessageAnswerWithInlineKeyboard(output, chatId, true, buttonsList.toArray(new ButtonForKeyboard[0]));
                break;
            case WALLET_MONEY:
                appUser.setWalletUserState(WALLET_CHANGE_CMD);
                appUserDAO.save(appUser);
                utilsService.sendMessageAnswerWithInlineKeyboard(appUser.getUserName()+", Вы активировали команду, позволящую работать с балансом на вашем кошельке. \n"
                        +"Выберите какую из команд вы хотели бы использовать: \n"
                        +"* /top_up - пополните баланс \n"
                        +" * /look_balance - посмотрите, сколько у вас на счету денег", chatId, false, new ButtonForKeyboard("Пополнить", "TOP_UP_COMMAND"), new ButtonForKeyboard("Посмотреть", "LOOK_BALANCE_COMMAND"));
                break;
            case SUPPORT:
                utilsService.sendAnswer("Бла, бла... Красивый тeкст \n @Kartbllansh", chatId);
                break;
            default:
                //sendAnswer("Неизвестная команда! Чтобы посмотреть список доступных команд введите /help", chatId);
                utilsService.sendMessageAnswerWithInlineKeyboard("Неизвестная команда! Чтобы посмотреть список доступных команд введите /help", chatId, true, new ButtonForKeyboard("Help", "HELP_COMMAND"));
                break;
        }

    }


    @Override
    public void processDocMessage(Update update) {

    }

    @Override
    public void processPhotoMessage(Update update) {

    }
@Override
    public AppUser findOrSaveAppUser(Update update){

        var telegramUser = update.getMessage().getFrom();

        var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if(optional.isEmpty()){
            //тут дожно быть создание таблицы с именем "telegramUser_"+telegramUser.getId()
            //createTable.createTable("telegramUser_"+telegramUser.getId());
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .lastName(telegramUser.getLastName())
                    .firstName(telegramUser.getFirstName())
                    .isActiveMail(false)
                    .isActiveConsent(false)
                    .state(BASIC_STATE)
                    .walletMoney(BigDecimal.valueOf(1000.00))
                    .buyUserState(NOT_BUY)
                    .sellUserState(NOT_SELL)
                    .walletUserState(NOT_WALLET)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }




}
