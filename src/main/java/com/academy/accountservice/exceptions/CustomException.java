package com.academy.accountservice.exceptions;

public class CustomException extends RuntimeException {

    public String getExceptionName() {
        return this.getClass().getSimpleName();
    }
}
