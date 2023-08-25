package org.example.exception;

public class UserStockNotFoundException extends RuntimeException {
    public UserStockNotFoundException(String message) {
        super(message);
    }
}
