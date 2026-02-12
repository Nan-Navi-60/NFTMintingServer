package com.nftmint.service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class PriceFeed {
	
    // 현재 시간의 금액으로 초기화
	private final AtomicReference<PriceQuote> currentQuote = 
			new AtomicReference<>(new PriceQuote(1000,System.currentTimeMillis()));
	
	// 랜덤 값 생성
	private final Random random = new Random();
	
	// 현재 금액 반환
	public PriceQuote getCurrentQuote() {
        return currentQuote.get();
    }
	
	// 1초마다 가격 변동
    // Thread 시작
    public void start() {
        Thread priceThread = new Thread(() -> {
            while (true) {
                try {
                    //1초의 딜레이
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}

                // 새로운 금액 랜덤 생성(기본 값은 900)
                long newPrice = 900 + random.nextInt(201); // 900 ~ 1100 랜덤
                // 현재 시간 금액 갱신
                currentQuote.set(new PriceQuote(newPrice, System.currentTimeMillis()));
                // 현재 금액 출력
                System.out.println("[PRICE UPDATE] new price = " + newPrice);
            }
        });

        priceThread.setDaemon(true);
        //Thread 시작
        priceThread.start();
    }
	
}
