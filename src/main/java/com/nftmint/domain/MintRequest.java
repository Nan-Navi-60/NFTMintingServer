package com.nftmint.domain;

public class MintRequest {

    private final String userId;
    private final long requestedPrice;
    private final long quotedAt;

    public MintRequest(String userId, long requestedPrice, long quotedAt) {
        this.userId = userId;
        this.requestedPrice = requestedPrice;
        this.quotedAt = quotedAt;
    }

    public String getUserId() {
        return userId;
    }

    public long getRequestedPrice() {
        return requestedPrice;
    }

    public long getQuotedAt() {
        return quotedAt;
    }
}