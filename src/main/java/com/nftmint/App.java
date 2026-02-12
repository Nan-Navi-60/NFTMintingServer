package com.nftmint;

import com.nftmint.domain.MintRequest;
import com.nftmint.log.AuditLogger;
import com.nftmint.metrics.Metrics;
import com.nftmint.producer.MintRequestProducer;
import com.nftmint.consumer.MintWorker;
import com.nftmint.repo.MintRepository;
import com.nftmint.service.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {

    public static void main(String[] args) throws Exception {

        int MAX_SUPPLY = 10000;

        BlockingQueue<MintRequest> queue = new LinkedBlockingQueue<>(5000);

        SupplyManager supplyManager = new SupplyManager(MAX_SUPPLY);
        MintRepository repository = new MintRepository();
        PriceFeed priceFeed = new PriceFeed();
        AtomicBoolean stopSignal = new AtomicBoolean(false);
        MintService mintService = new MintService(supplyManager, repository);
        Metrics metrics = new Metrics();
        AuditLogger auditLogger = new AuditLogger();
        
        priceFeed.start();

        int producerCount = 20;
        int workerCount = 8;
        int requestsPerProducer = 1000;
        int userPoolSize = 5000;

        ExecutorService producerPool = Executors.newFixedThreadPool(producerCount);
        ExecutorService workerPool = Executors.newFixedThreadPool(workerCount);

        for (int i = 0; i < producerCount; i++) {
            producerPool.submit(
                    new MintRequestProducer(queue, priceFeed, metrics,
                            requestsPerProducer, userPoolSize)
            );
        }

        for (int i = 0; i < workerCount; i++) {
            workerPool.submit(new MintWorker(queue, mintService, metrics, auditLogger, stopSignal));
        }

        producerPool.shutdown();
        producerPool.awaitTermination(1, TimeUnit.MINUTES);
        
        stopSignal.set(true);
        
        workerPool.shutdown();
        if (!workerPool.awaitTermination(30, TimeUnit.SECONDS)) {
            workerPool.shutdownNow();
        }

        System.out.println("Final Metrics: " + metrics);
        System.out.println("Total Minted (Repo): " + repository.totalMintedUsers());
        
        System.exit(0);
    }
}