package com.auction.exception;

import lombok.Getter;

@Getter
public class UserConflictException extends RuntimeException {

    public UserConflictException(String message) {
        super(message);
    }
}