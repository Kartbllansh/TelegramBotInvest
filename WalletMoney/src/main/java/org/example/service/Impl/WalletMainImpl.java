package org.example.service.Impl;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.entity.AppUser;
import org.example.service.WalletMain;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
@Log4j
public class WalletMainImpl implements WalletMain {
private final AppUserDAO appUserDAO;
//TODO механизм, позволяющий работать с разными валютами

    private final BigDecimal maxBalance = new BigDecimal("1000000000"); // 1 миллиард
    private final BigDecimal maxPopolneniy = new BigDecimal("10000000000");
    public WalletMainImpl(AppUserDAO appUserDAO) {
        this.appUserDAO = appUserDAO;
    }


    @Override
    public String toKnowBalance(AppUser appUser) {
        BigDecimal balance = appUser.getWalletMoney();
        return appUser.getUserName()+", \n На вашем счету на данный момент "+balance+"₽";
    }



    @Override
    public String topUpWallet(BigDecimal summa, AppUser appUser, boolean whoTopUp) {
        if(whoTopUp){
            if (appUser.getWalletMoney().add(summa).compareTo(maxBalance) > 0) {
                return EmojiParser.parseToUnicode("Извините, но вы не можете пополнить счет на сумму, которая превышает 1 миллиард." +EmojiParser.parseToUnicode(":money_with_wings:") );
            }

            if(appUser.getTopUpAmount().add(summa).compareTo(maxPopolneniy) > 0){
                return "За все время использования бота вы пополнили кошелек больше чем на сумму 10 миллиардов:) \n К сожалению, пополнения кошелька дальше блокируется:)";
            }
            appUser.setTopUpAmount(appUser.getTopUpAmount().add(summa));
            appUserDAO.save(appUser);
        }
        // Проверка, что переданная сумма положительна
        if (summa.compareTo(BigDecimal.ZERO) < 0) {
            return EmojiParser.parseToUnicode("К сожалению, наш бот пока не умеет пополнять счет на отрицательную сумму"+":thinking:"+"\n Если вы хотите избавиться от денег на счету напишите нам в поддержку \n Мы обязательно вам поможем"+":revolving_hearts:") ;
        }



        appUser.setWalletMoney(appUser.getWalletMoney().add(summa));
        appUserDAO.save(appUser);
        log.info("Пополнение баланса "+appUser.getUserName()+" на "+summa);
        return "Пополнение счета  на " + summa + " выполнено успешно"+EmojiParser.parseToUnicode(":dollar:")+ " \n Теперь на вашем счету " + appUser.getWalletMoney()+"₽";
    }


    @Override
    public String topDownWallet(BigDecimal summa, AppUser appUser) {
        BigDecimal currentBalance = appUser.getWalletMoney();

        // Проверка, достаточно ли средств на счете
        if (currentBalance.compareTo(summa) < 0) {
            return EmojiParser.parseToUnicode("Недостаточно средств на вашем счету."+":worried:");
        }

        appUser.setWalletMoney(currentBalance.subtract(summa));
        log.info("Уменьшение баланса "+appUser.getUserName()+" на "+summa);
        appUserDAO.save(appUser);
        return "С вашего счета снято " + summa + ". \n Теперь у вас на счету " + appUser.getWalletMoney()+"₽";
    }

    @Override
    public boolean checkAbilityBuy(BigDecimal summa, AppUser appUser) {
        BigDecimal currentBalance = appUser.getWalletMoney();
        return currentBalance.compareTo(summa) >= 0;

    }

}
