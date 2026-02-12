package com.nftmint.service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class PriceFeed {
	
	private final AtomicReference<PriceQuote> currentQuote = 
			new AtomicReference<>(new PriceQuote(1000,System.currentTimeMillis()));
	
	private final Random random = new Random();
	
	public PriceQuote getCurrentQuote() {
        return currentQuote.get();
    }
	
	// 1초마다 가격 변동
    public void start() {
        Thread priceThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}

                long newPrice = 900 + random.nextInt(201); // 900 ~ 1100 랜덤
                currentQuote.set(new PriceQuote(newPrice, System.currentTimeMillis()));
                System.out.println("[PRICE UPDATE] new price = " + newPrice);
            }
        });

        priceThread.setDaemon(true);
        priceThread.start();
    }
	
}
