package com.example.forum.exceptions.domain;

public class PasswordNotMatchingException extends Exception {
    public PasswordNotMatchingException(String message) {
        super(message);
    }
}
