package com.auction.exception;

public class AuctionOptimisticLockException extends RuntimeException {

    public AuctionOptimisticLockException(String message) {
        super(message);
    }
}