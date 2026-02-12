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

    	// 최대 수
        int MAX_SUPPLY = 10000;

        // 최대수 크기의 BlockingQueue 생성
        BlockingQueue<MintRequest> queue = new LinkedBlockingQueue<>(5000);

        // 저장할 최대한의 유저 수
        SupplyManager supplyManager = new SupplyManager(MAX_SUPPLY);
        // 
        MintRepository repository = new MintRepository();
        // 가격 변동 Thread 클래스 호출
        PriceFeed priceFeed = new PriceFeed();
        // 종료 신호(초기 설정 False)
        // 참조하는 위치의 값이 false인지 true인지 검증 가능
        AtomicBoolean stopSignal = new AtomicBoolean(false);
        // 민팅 동작 클래스 호출
        MintService mintService = new MintService(supplyManager, repository);
        // 최종 결과 출력 클래스 호출
        Metrics metrics = new Metrics();
        // Log 클래스 호출
        AuditLogger auditLogger = new AuditLogger();
        
        // 가격 변동 Thread 시작
        priceFeed.start();

        // 기본 세팅 값
        int producerCount = 20;
        int workerCount = 8;
        int requestsPerProducer = 1000;
        int userPoolSize = 5000;

        // 병렬 객체 생성
        ExecutorService producerPool = Executors.newFixedThreadPool(producerCount);
        ExecutorService workerPool = Executors.newFixedThreadPool(workerCount);

        for (int i = 0; i < producerCount; i++) {
        	// 제한된 횟수 만큼 요청 스레드 실행
            // BlockingQueue와 금액, 결과 표, 기본 세팅 값 전달
            producerPool.submit(
                    new MintRequestProducer(queue, priceFeed, metrics,
                            requestsPerProducer, userPoolSize)
            );
        }

        for (int i = 0; i < workerCount; i++) {
            // 제한된 횟수 만큼 처리(Mint) 스레드 실행
            // 요청객체가 포함된 BlockingQueue, mint 동작 객체, Log 객체, 종료 신호 전달
            workerPool.submit(new MintWorker(queue, mintService, metrics, auditLogger, stopSignal));
        }

        // Thread 종료
        producerPool.shutdown();
        // 1분 후 까지 대기
        producerPool.awaitTermination(1, TimeUnit.MINUTES);
        
        // 종료 시그널 True
        stopSignal.set(true);
        
        // Thread 종료
        workerPool.shutdown();
        // 만약 30초 동안 Thread가 종료되지 않으면,
        if (!workerPool.awaitTermination(30, TimeUnit.SECONDS)) {
            // Thread 즉시 종료
            workerPool.shutdownNow();
        }

        // Metrics 출력
        System.out.println("Final Metrics: " + metrics);
        // 총 거래 유저 수 출력
        System.out.println("Total Minted (Repo): " + repository.totalMintedUsers());
        
        System.exit(0);
    }
}