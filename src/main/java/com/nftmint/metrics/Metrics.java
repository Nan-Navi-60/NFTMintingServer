package com.nftmint.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {

    // 참조 주소를 가지는 변수 선언
    private final AtomicLong produced = new AtomicLong();
    private final AtomicLong consumed = new AtomicLong();

    private final AtomicInteger mintedSuccess = new AtomicInteger();
    private final AtomicInteger mintedRejectedSoldOut = new AtomicInteger();
    private final AtomicInteger mintedRejectedDuplicateUser = new AtomicInteger();
    private final AtomicInteger mintedFailedUnexpected = new AtomicInteger();

    // 현재값에 즉시 값 더하기 함수
    public void incProduced() { produced.incrementAndGet(); }
    public void incConsumed() { consumed.incrementAndGet(); }

    public void incSuccess() { mintedSuccess.incrementAndGet(); }
    public void incSoldOut() { mintedRejectedSoldOut.incrementAndGet(); }
    public void incDuplicate() { mintedRejectedDuplicateUser.incrementAndGet(); }
    public void incUnexpected() { mintedFailedUnexpected.incrementAndGet(); }

    // 반환
    public long produced() { return produced.get(); }
    public long consumed() { return consumed.get(); }

    public int success() { return mintedSuccess.get(); }
    public int soldOut() { return mintedRejectedSoldOut.get(); }
    public int duplicate() { return mintedRejectedDuplicateUser.get(); }
    public int unexpected() { return mintedFailedUnexpected.get(); }

    // Metrix
    @Override
    public String toString() {
        return """
                Metrics
                - produced: %d
                - consumed: %d
                - success: %d
                - rejected(soldOut): %d
                - rejected(duplicateUser): %d
                - failed(unexpected): %d
                """.formatted(
                produced(), consumed(),
                success(), soldOut(), duplicate(), unexpected()
        );
    }
}