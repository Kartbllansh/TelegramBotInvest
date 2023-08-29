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
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

        if (existingUserStock.isPresent()) {
            UserStock userStock = existingUserStock.get();
            String oldInfoAboutBuy = userStock.getNoticeBuyOrSell();
            int newTotalCount = userStock.getCountStock() + countStock;
            BigDecimal newTotalPrice = userStock.getPrice().multiply(BigDecimal.valueOf(userStock.getCountStock()))
                    .add(price.multiply(BigDecimal.valueOf(countStock)))
                    .divide(BigDecimal.valueOf(newTotalCount), RoundingMode.HALF_UP);

            userStock.setCountStock(newTotalCount);
            userStock.setNoticeBuyOrSell(oldInfoAboutBuy+" \n BUY "+countStock+" "+purchaseTime);
            userStock.setPrice(newTotalPrice);
            userStockDAO.save(userStock);
        } else {
            UserStock newUserStock = UserStock.builder()
                    .appUser(user)
                    .stockQuote(stockQuoteRepository.findBySecId(code).get())
                    .countStock(countStock)
                    .noticeBuyOrSell("BUY "+countStock+" "+purchaseTime)
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
                userStock.setNoticeBuyOrSell(userStock.getNoticeBuyOrSell()+"\n SELL "+countStockToSell+" "+LocalDateTime.now());
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
}










