package com.nftmint.domain;

public class MintResult {

    private final String userId;
    private final int tokenId;
    private final long pricePaid;

    public MintResult(String userId, int tokenId, long pricePaid) {
        this.userId = userId;
        this.tokenId = tokenId;
        this.pricePaid = pricePaid;
    }

    public String getUserId() {
        return userId;
    }

    public int getTokenId() {
        return tokenId;
    }

    public long getPricePaid() {
        return pricePaid;
    }
}