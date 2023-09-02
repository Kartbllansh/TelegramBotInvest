package org.example.service.impl;

import com.vdurmont.emoji.EmojiParser;
import org.example.dao.StockQuoteRepository;
import org.example.dao.UserStockDAO;
import org.example.entity.AppUser;
import org.example.entity.UserStock;
import org.example.exception.InsufficientStocksException;
import org.example.exception.UserStockNotFoundException;
import org.example.service.AppUserStockService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class AppUserStockServiceImpl implements AppUserStockService {

    private final UserStockDAO userStockDAO;
    private final StockQuoteRepository stockQuoteRepository;

    public AppUserStockServiceImpl(UserStockDAO userStockDAO, StockQuoteRepository stockQuoteRepository) {
        this.userStockDAO = userStockDAO;
        this.stockQuoteRepository = stockQuoteRepository;
    }


    //checkAboutCodeStock
    @Override
    public boolean hasUserBoughtStock(AppUser user, String codeStock) {
        Optional<UserStock> userStocks = userStockDAO.findByAppUserAndStockQuote_SecId(user, codeStock);
        return userStocks.isPresent();
    }

    //addNoteAboutBuy
    public void saveOrUpdateUserStock(AppUser user, String code, int countStock, LocalDateTime purchaseTime, BigDecimal price) {
        Optional<UserStock> existingUserStock = userStockDAO.findByAppUserAndStockQuote_SecId(user, code);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = purchaseTime.format(formatter);
        if (existingUserStock.isPresent()) {
            UserStock userStock = existingUserStock.get();
            String oldInfoAboutBuy = userStock.getNoticeBuyOrSell();
            int newTotalCount = userStock.getCountStock() + countStock;
            BigDecimal newTotalPrice = userStock.getPrice().multiply(BigDecimal.valueOf(userStock.getCountStock()))
                    .add(price.multiply(BigDecimal.valueOf(countStock)))
                    .divide(BigDecimal.valueOf(newTotalCount), RoundingMode.HALF_UP);

            userStock.setCountStock(newTotalCount);
            userStock.setNoticeBuyOrSell(oldInfoAboutBuy+" \n Покупка "+countStock+" акций в "+formattedDateTime);
            userStock.setPrice(newTotalPrice);
            userStockDAO.save(userStock);
        } else {
            UserStock newUserStock = UserStock.builder()
                    .appUser(user)
                    .stockQuote(stockQuoteRepository.findBySecId(code).get())
                    .countStock(countStock)
                    .noticeBuyOrSell("Покупка "+countStock+" акций в "+formattedDateTime)
                    .price(price)
                    .build();

            userStockDAO.save(newUserStock);
        }
    }


    //addNoteAboutSell
    public void sellUserStock(AppUser user, String codeStock, int countStockToSell) {
        Optional<UserStock> existingUserStock = userStockDAO.findByAppUserAndStockQuote_SecId(user, codeStock);

        if (existingUserStock.isPresent()) {
            UserStock userStock = existingUserStock.get();

            // Проверка, достаточно ли акций для продажи
            if (userStock.getCountStock() < countStockToSell) {
                throw new InsufficientStocksException("Not enough stocks to sell.");
            }
            int newTotalCount = userStock.getCountStock() - countStockToSell;
            if (newTotalCount == 0) {
                userStockDAO.delete(userStock);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDateTime = LocalDateTime.now().format(formatter);
                userStock.setNoticeBuyOrSell(userStock.getNoticeBuyOrSell()+"\n Продажа "+countStockToSell+" акций в "+formattedDateTime);
                userStock.setCountStock(newTotalCount);
                userStockDAO.save(userStock);
            }

        } else {
            throw new UserStockNotFoundException("User stock not found.");
        }
    }

    public String getInfoAboutBag(AppUser user) {
        List<UserStock> userStocks = userStockDAO.findByAppUser(user);

        if (userStocks.isEmpty()) {
            return "На данный момент ваш инвестиционный портфель пуст"+ EmojiParser.parseToUnicode(":unamused:");
        } else {
            StringBuilder result = new StringBuilder();
            result.append("На данный момент в вашем инвестиционном портфеле находятся следующие активы:\n");
            for (UserStock userStock : userStocks) {
                String codeStock = userStock.getStockQuote().getSecId();
                String shortName = userStock.getStockQuote().getShortName();
                int countStock = userStock.getCountStock();
                BigDecimal price = userStock.getPrice();
                result.append(EmojiParser.parseToUnicode(":small_blue_diamond: ")).append(shortName).append("( ").append(codeStock).append(" )").append(" - в количестве ").append(countStock).append(" акций, купленных по цене ").append(price).append("\n");
            }
            return result.toString();
        }
    }

    public List<String> getAllKeysInBag(AppUser appUser) {
        List<UserStock> userStocks = userStockDAO.findByAppUser(appUser);
        List<String> list = new ArrayList<>();
        if (userStocks.isEmpty()) {
            return null;
        } else {
            for (UserStock userStock : userStocks) {
                String codeStock = userStock.getStockQuote().getSecId();
                list.add(codeStock);
            }
            return list;
        }

    }

    public Long countOfTheBag(AppUser user, String code) {
        Optional<UserStock> userStock = userStockDAO.findByAppUserAndStockQuote_SecId(user, code );
        return userStock.map(stock -> (long) stock.getCountStock()).orElse(0L);
    }
    public Integer checkAboutCountSell(AppUser user, Integer count, String codeStocks) {
        Optional<UserStock> userStock = userStockDAO.findByAppUserAndStockQuote_SecId(user, codeStocks);
        if (userStock.isPresent()) {
            int countStocks =  userStock.get().getCountStock();
            return countStocks - count;
        } else {
            return 0; // Если акции не найдены, считаем, что их 0
        }
    }

    public String reportAboutInvestBag(AppUser appUser){
        List<UserStock> userStocks = userStockDAO.findByAppUser(appUser);
        StringBuilder stringBuilder = new StringBuilder("Ваш инвестиционный портфель"+EmojiParser.parseToUnicode(":school_satchel:")+": \n \n");
        BigDecimal allSumma = new BigDecimal(BigInteger.ZERO);
        if (userStocks.isEmpty()) {
            stringBuilder.append("В вашем портфеле нет ни одного актива").append(EmojiParser.parseToUnicode(":face_with_peeking_eye:")).append("\n _________________ \n");
        } else {


            for(UserStock userStock : userStocks){
                BigDecimal countBuy = userStock.getPrice().multiply(BigDecimal.valueOf(userStock.getCountStock()));
                BigDecimal countFactNow = userStock.getStockQuote().getPrevLegalClosePrice().multiply(BigDecimal.valueOf(userStock.getCountStock()));
                BigDecimal inCome = countFactNow.subtract(countBuy);
                BigDecimal incomeWithProcent = inCome.divide(countBuy);
              allSumma =  allSumma.add(countFactNow);
               stringBuilder.append(EmojiParser.parseToUnicode(":large_blue_diamond: ")).append(userStock.getStockQuote().getShortName()).append("(")
                       .append(userStock.getStockQuote().getSecId()).append(")")
                       .append(" - ").append(userStock.getCountStock()).append(" акций \n").append("Доход").append(EmojiParser.parseToUnicode(":money_with_wings:")).append(":   ").append(inCome).append(" | ")
                       .append(incomeWithProcent).append("%").append("\n").append("Время покупок и продаж").append(EmojiParser.parseToUnicode(":hourglass:")).append(": \n").append(userStock.getNoticeBuyOrSell()).append("\n")
                       .append("_________________________ \n");
            }
        }
            BigDecimal s = allSumma.add(appUser.getWalletMoney());
            BigDecimal inComeAll = s.subtract(appUser.getTopUpAmount());
            BigDecimal inComeWithProcent = inComeAll.divide(appUser.getTopUpAmount());
            stringBuilder.append("На кошельке: ").append(appUser.getWalletMoney()).append("₽ \n").append("Стоимость всех активов: ")
                    .append(allSumma).append("₽ \n").append("Пополнения за все время: ").append(appUser.getTopUpAmount()).append("₽ \n").append("Прибыль за все время").append(EmojiParser.parseToUnicode(":chart:")).append(":   ").append(inComeAll).append("₽ | ").append(inComeWithProcent).append("%");
            return stringBuilder.toString();

        }
    }










