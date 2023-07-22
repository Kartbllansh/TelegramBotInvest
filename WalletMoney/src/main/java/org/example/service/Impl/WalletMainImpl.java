package org.example.service.Impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.WalletMain;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class WalletMainImpl implements WalletMain {
private final AppUserDAO appUserDAO;
//TODO механизм, позволяющий работать с разными валютами

    public WalletMainImpl(AppUserDAO appUserDAO) {
        this.appUserDAO = appUserDAO;
    }


    @Override
    public String toKnowBalance(AppUser appUser) {
        BigDecimal balance = appUser.getWalletMoney();
        return "На вашем счету на данный момент "+balance+" $";
    }

    @Override
    public String topUpWallet(BigDecimal summa, AppUser appUser) {
        appUser.setWalletMoney(appUser.getWalletMoney().add(summa));
        appUserDAO.save(appUser);
        return "Ваш счет увеличился на "+summa+" теперь у вас на счету"+ appUser.getWalletMoney();
    }

    @Override
    public String topDownWallet(BigDecimal summa, AppUser appUser) {
        appUser.setWalletMoney(appUser.getWalletMoney().subtract(summa));
        appUserDAO.save(appUser);
        return "С вашего счета снято "+summa+" сейчас у вас на счету "+appUser.getWalletMoney();
    }
}
