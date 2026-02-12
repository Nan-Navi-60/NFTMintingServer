package com.nftmint.producer;

import com.nftmint.domain.MintRequest;
import com.nftmint.metrics.Metrics;
import com.nftmint.service.PriceFeed;

import java.util.concurrent.BlockingQueue;

public class MintRequestProducer implements Runnable {

    private final BlockingQueue<MintRequest> queue;
    private final PriceFeed priceFeed;
    private final Metrics metrics;
    private final int requestCount;
    private final int userPoolSize;

    public MintRequestProducer(BlockingQueue<MintRequest> queue, PriceFeed priceFeed, Metrics metrics, int requestCount, int userPoolSize) {
        this.queue = queue;
        this.priceFeed = priceFeed;
        this.metrics = metrics;
        this.requestCount = requestCount;
        this.userPoolSize = userPoolSize;
    }

    @Override
    public void run() {
        for (int i = 0; i < requestCount; i++) {
            try {
                String userId = "user-" + (int)(Math.random() * userPoolSize);

                var quote = priceFeed.getCurrentQuote();

                MintRequest request = new MintRequest( userId, quote.getPrice(), quote.getTimestamp());

                queue.put(request);
                metrics.incProduced();

            } catch (InterruptedException ignored) {}
        }
    }
}