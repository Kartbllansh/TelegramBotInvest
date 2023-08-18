package org.example.service.impl;

import org.example.entity.AppUser;
import org.example.service.CallBackMainService;
import org.example.service.MainService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
@Service
public class CallBackMainServiceImpl implements CallBackMainService {
    private final MainService mainService;

    public CallBackMainServiceImpl(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    public void processCallBackQuery(Update update) {
        AppUser appUser = mainService.findOrSaveAppUser(update);
    long messageId = update.getCallbackQuery().getMessage().getMessageId();
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String callBackData = update.getCallbackQuery().getData();
    if(callBackData.equals("YES_BUTTON")){
      processYesButton(appUser, messageId, chatId);
    } else if (callBackData.equals("NO_BUTTON")) {
        processNoButton(appUser, messageId, chatId);
    }
    }

    private void processNoButton(AppUser appUser, long messageId, long chatId) {

    }

    private void processYesButton(AppUser appUser, long messageId, long chatId) {

    }
}
