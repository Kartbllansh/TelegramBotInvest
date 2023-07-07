package org.example.service.Impl;

import org.example.dao.AppUserDAO;
import org.example.service.UserActivationService;
import org.example.utils.CryptoTool;
import org.springframework.stereotype.Service;

@Service
public class UserActivationServiceImpl implements UserActivationService {
private final CryptoTool cryptoTool;
private final AppUserDAO appUserDAO;

    public UserActivationServiceImpl(CryptoTool cryptoTool, AppUserDAO appUserDAO) {
        this.cryptoTool = cryptoTool;
        this.appUserDAO = appUserDAO;
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
}
