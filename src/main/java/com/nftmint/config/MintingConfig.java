package com.nftmint.config;

public final class MintingConfig {

    public final int maxSupply;
    public final int producerThreads;
    public final int requestsPerProducer;
    public final int consumerThreads;
    public final int queueCapacity;

    //todo : safemode 여부 체크
    public final boolean safeMode;

    public MintingConfig(
            int maxSupply,
            int producerThreads,
            int requestsPerProducer,
            int consumerThreads,
            int queueCapacity,
            boolean safeMode
    ) {
        this.maxSupply = maxSupply;
        this.producerThreads = producerThreads;
        this.requestsPerProducer = requestsPerProducer;
        this.consumerThreads = consumerThreads;
        this.queueCapacity = queueCapacity;
        this.safeMode = safeMode;
    }

    public static MintingConfig defaultConfig(boolean safeMode) {
        return new MintingConfig(
                10000,   // 총 만개 제한
                20,       // producer 구매자 20명
                1000,    // 구매자 1명당 1000개 구매요청
                8,        // consumer 처리하는 직원 8명
                1000,    // 대기열 큐
                safeMode
        );
    }
}
