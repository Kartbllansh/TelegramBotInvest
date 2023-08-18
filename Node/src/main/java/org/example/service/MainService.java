package org.example.service;

import org.example.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    void processTextMessage(Update update);
    AppUser findOrSaveAppUser(Update update);
    void processDocMessage(Update update);
    void processPhotoMessage(Update update);
}
