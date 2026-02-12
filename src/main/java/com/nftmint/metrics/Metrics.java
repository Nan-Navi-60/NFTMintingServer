package com.nftmint.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {

    private final AtomicLong produced = new AtomicLong();
    private final AtomicLong consumed = new AtomicLong();

    private final AtomicInteger mintedSuccess = new AtomicInteger();
    private final AtomicInteger mintedRejectedSoldOut = new AtomicInteger();
    private final AtomicInteger mintedRejectedDuplicateUser = new AtomicInteger();
    private final AtomicInteger mintedFailedUnexpected = new AtomicInteger();

    public void incProduced() { produced.incrementAndGet(); }
    public void incConsumed() { consumed.incrementAndGet(); }

    public void incSuccess() { mintedSuccess.incrementAndGet(); }
    public void incSoldOut() { mintedRejectedSoldOut.incrementAndGet(); }
    public void incDuplicate() { mintedRejectedDuplicateUser.incrementAndGet(); }
    public void incUnexpected() { mintedFailedUnexpected.incrementAndGet(); }

    public long produced() { return produced.get(); }
    public long consumed() { return consumed.get(); }

    public int success() { return mintedSuccess.get(); }
    public int soldOut() { return mintedRejectedSoldOut.get(); }
    public int duplicate() { return mintedRejectedDuplicateUser.get(); }
    public int unexpected() { return mintedFailedUnexpected.get(); }

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