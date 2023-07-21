package org.example.service.Impl;

import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.WalletMain;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class WalletMainImpl implements WalletMain {
private final AppUserDAO appUserDAO;
private final AppUser appUser;
//TODO механизм, позволяющий работать с разными валютами

    public WalletMainImpl(AppUserDAO appUserDAO, AppUser appUser) {
        this.appUserDAO = appUserDAO;
        this.appUser = appUser;
    }


    @Override
    public String toKnowBalance(AppUser appUser) {
        BigDecimal balance = appUser.getWalletMoney();
        return "На вашем счету на данный момент "+balance;
    }

    @Override
    public void topUpWallet(BigDecimal summa, AppUser appUser) {

    }
}
