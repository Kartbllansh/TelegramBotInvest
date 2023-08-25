package org.example.exception;

public class InsufficientStocksException extends RuntimeException{
    public InsufficientStocksException(String message) {
        super(message);
    }
}
