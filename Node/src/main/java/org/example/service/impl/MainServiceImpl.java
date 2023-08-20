package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.enums.CommandService;
import org.example.service.*;
import org.example.utils.ButtonForKeyboard;
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
    private final CreateTable createTable;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;
    private final UtilsService utilsService;

    public MainServiceImpl(WalletService walletService, BuyOrSellService buyOrSellService, CreateTable createTable, AppUserDAO appUserDAO, AppUserService appUserService, UtilsService utilsService) {
        this.walletService = walletService;
        this.buyOrSellService = buyOrSellService;
        this.createTable = createTable;
        this.appUserDAO = appUserDAO;
        this.appUserService = appUserService;
        this.utilsService = utilsService;
    }

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
        if (CANCEL.equals(serviceCommand)) {
            output = utilsService.cancelProcess(appUser);
            utilsService.sendAnswer(output, chatId);
        } else if (BASIC_STATE.equals(userState)) {

            if(!NOT_BUY.equals(buyUserState)) {
                buyOrSellService.onActionBuy(appUser, text, chatId, messageId);
            } else if (!NOT_SELL.equals(sellUserState)) {
                buyOrSellService.onActionSell(appUser, text, chatId, messageId);
            } else if (!NOT_WALLET.equals(walletUserState)) {
                walletService.onActiveWallet(appUser, text, chatId);
            } else {
                processServiceCommand(appUser, text, chatId);
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

    //TODO создание адекватного процесса авторизации
    private void processServiceCommand(AppUser appUser, String cmd, long chatId) {
        var serviceCommand = CommandService.fromValue(cmd);
        if(serviceCommand==null){
            utilsService.sendMessageAnswerWithInlineKeyboard("Неизвестная команда! Чтобы посмотреть список доступных команд введите /help", chatId, new ButtonForKeyboard("Help", "HELP_COMMAND"));
        }
        switch (Objects.requireNonNull(serviceCommand)){
            case START:
                log.info("Новый пользователь с именем " + appUser.getUserName());
                utilsService.sendAnswer("Приветствую, "+appUser.getUserName()+ "!\n {тут будет красивое вступление} \n Чтобы посмотреть список доступных команд введите /help", chatId);
                break;
            case REGISTRATION:
                log.info("Регистрация пользователя "+appUser.getUserName()+" с почтой "+appUser.getEmail());
                utilsService.sendAnswer(appUserService.registerUser(appUser), chatId);
                break;
            case HELP:
                utilsService.sendAnswer(utilsService.help(), chatId);
                break;
            case BUY:
                appUser.setBuyUserState(CHANGE_STONKS);
                appUserDAO.save(appUser);
                utilsService.sendAnswer(appUser.getUserName()+", вы активировали команду /buy! \n"
                        +"Введите код акции, которую хотите купить", chatId);
                break;
            case SELL:
                appUser.setSellUserState(SELL_CHANGE_STOCK);
                appUserDAO.save(appUser);
                utilsService.sendAnswer(createTable.getInfoAboutBag("telegramUser_"+appUser.getTelegramUserId()), appUser.getTelegramUserId());
                List<String> list = createTable.getAllKeysInBag("telegramUser_"+appUser.getTelegramUserId());
                List<ButtonForKeyboard> buttonsList = new ArrayList<>();
                String output = appUser.getUserName()+", вы активировали команду /sell! \n"
                        +"Введите ключ акции, которую хотите продать";

                for (String buttonText : list) {
                    buttonsList.add(new ButtonForKeyboard(buttonText, buttonText));
                }

                utilsService.sendMessageAnswerWithInlineKeyboard(output, chatId, buttonsList.toArray(new ButtonForKeyboard[0]));
                //sendAnswer(appUser.getUserName()+", вы активировали команду /sell! \n"
                     //   +"Введите ключ акции, которую хотите продать", chatId);
                break;
            case WALLET_MONEY:
                appUser.setWalletUserState(WALLET_CHANGE_CMD);
                appUserDAO.save(appUser);
                utilsService.sendAnswer(appUser.getUserName()+", Вы активировали команду, позволящую работать с балансом на вашем кошельке. \n"
                        +"Выберите какую из команд вы хотели бы использовать: \n"
                        +"* /top_up - пополните баланс \n"
                        +" * /look_balance - посмотрите, сколько у вас на счету денег", chatId);
                break;
            default:
                //sendAnswer("Неизвестная команда! Чтобы посмотреть список доступных команд введите /help", chatId);
                utilsService.sendMessageAnswerWithInlineKeyboard("Неизвестная команда! Чтобы посмотреть список доступных команд введите /help", chatId, new ButtonForKeyboard("Help", "HELP_COMMAND"));
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
            createTable.createTable("telegramUser_"+telegramUser.getId());
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .lastName(telegramUser.getLastName())
                    .firstName(telegramUser.getFirstName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .walletMoney(BigDecimal.valueOf(1000.00))
                    //TODO рассказать пользователю, что у него есть на счету косарик
                    .buyUserState(NOT_BUY)
                    .sellUserState(NOT_SELL)
                    .walletUserState(NOT_WALLET)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }




}
