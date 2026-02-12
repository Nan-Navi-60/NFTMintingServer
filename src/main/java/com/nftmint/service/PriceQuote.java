package com.nftmint.service;

// 금액 객체
public class PriceQuote {
	
	private final long price;
	private final long timestamp;
	
	public PriceQuote(long price, long timestap) {
		this.price = price;
		this.timestamp = timestap;
	}
	
	public long getPrice() {
		return price;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	
	
}
