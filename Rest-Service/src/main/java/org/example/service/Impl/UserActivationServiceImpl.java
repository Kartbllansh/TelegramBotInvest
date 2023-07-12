package org.example.service.Impl;

import org.example.dao.AppUserDAO;
import org.example.service.ProducerService;
import org.example.service.UserActivationService;
import org.example.utils.CryptoTool;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class UserActivationServiceImpl implements UserActivationService {
private final CryptoTool cryptoTool;
private final AppUserDAO appUserDAO;
private final ProducerService producerService;

    public UserActivationServiceImpl(CryptoTool cryptoTool, AppUserDAO appUserDAO, ProducerService producerService) {
        this.cryptoTool = cryptoTool;
        this.appUserDAO = appUserDAO;
        this.producerService = producerService;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        var optional = appUserDAO.findById(userId);
        if (optional.isPresent()) {
            var user = optional.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
@Override
    public void getMessageAboutRegist(long id){
        var optional = appUserDAO.findById(id);
        if(optional.isPresent()){
            var user = optional.get();
            long chatId = user.getChatId();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Регистрация успешно выволнена. Тут будут подсказки, но пока без них");
            producerService.producerAnswer(sendMessage);

        }
    }
}
