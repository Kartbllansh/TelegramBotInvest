package org.example.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallBackMainService {
    void processCallBackQuery(Update update);
}
