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
        return "На вашем счету на данный момент "+balance+"₽";
    }



    @Override
    public String topUpWallet(BigDecimal summa, AppUser appUser) {
        // Проверка, что переданная сумма положительна
        if (summa.compareTo(BigDecimal.ZERO) <= 0) {
            return "Сумма для пополнения должна быть положительной.";
        }

        appUser.setWalletMoney(appUser.getWalletMoney().add(summa));
        appUserDAO.save(appUser);
        return "Ваш счет увеличился на " + summa + ". Теперь у вас на счету " + appUser.getWalletMoney()+"₽";
    }


    @Override
    public String topDownWallet(BigDecimal summa, AppUser appUser) {
        BigDecimal currentBalance = appUser.getWalletMoney();

        // Проверка, достаточно ли средств на счете
        if (currentBalance.compareTo(summa) < 0) {
            return "Недостаточно средств на вашем счету.";
        }

        appUser.setWalletMoney(currentBalance.subtract(summa));
        appUserDAO.save(appUser);
        return "С вашего счета снято " + summa + ". Теперь у вас на счету " + appUser.getWalletMoney()+"₽";
    }

    @Override
    public boolean checkAbilityBuy(BigDecimal summa, AppUser appUser) {
        BigDecimal currentBalance = appUser.getWalletMoney();
        return currentBalance.compareTo(summa) >= 0;

    }

}
