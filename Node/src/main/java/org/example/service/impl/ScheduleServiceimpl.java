package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AdsDAO;
import org.example.entity.Ads;
import org.example.service.ScheduleService;
import org.example.service.UtilsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class ScheduleServiceimpl implements ScheduleService {
        @Value("${bot.owner}")
        private Long idOwner;
    private final AdsDAO adsDAO;
    private final UtilsService utilsService;

    public ScheduleServiceimpl(AdsDAO adsDAO, UtilsService utilsService) {
        this.adsDAO = adsDAO;
        this.utilsService = utilsService;
    }

    @Override
    @Scheduled(fixedRate = 10800000) // Запуск каждые 10 минут (600000 миллисекунд)
    public void sendAboutUpgradeMoex() {
        var ads = adsDAO.findAll();
        for(Ads ad : ads){
            utilsService.sendAnswer(ad.getAd(), idOwner);
            adsDAO.delete(ad);
         log.info("Проверка объявлений");
        }
    }
}
