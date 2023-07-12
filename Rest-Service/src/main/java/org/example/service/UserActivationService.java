package org.example.service;

public interface UserActivationService {
    boolean activation(String cryptoUserId);
    void getMessageAboutRegist(long chatId);
}
