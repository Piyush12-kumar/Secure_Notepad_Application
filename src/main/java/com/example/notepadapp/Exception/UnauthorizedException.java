package com.example.notepadapp.Exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String userIsNotAuthenticated) {
        super(userIsNotAuthenticated);
    }
}
