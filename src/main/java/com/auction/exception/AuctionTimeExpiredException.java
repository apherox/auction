package com.auction.exception;

public class AuctionTimeExpiredException extends RuntimeException {

	public AuctionTimeExpiredException(String message) {
		super(message);
	}
}
