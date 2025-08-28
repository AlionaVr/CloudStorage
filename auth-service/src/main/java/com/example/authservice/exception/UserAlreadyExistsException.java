package com.example.authservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String login) {
        super("User with login '" + login + "' already exists");
    }
}
