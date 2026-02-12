package com.nftmint.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class SupplyManager {

    private final int maxSupply;

    private int nextTokenId = 1;
    private int mintedCount = 0;

    // “1인 1회” 제약
    private final Set<String> mintedUsers = new HashSet<>();

    private final ReentrantLock lock = new ReentrantLock(true);

    public SupplyManager(int maxSupply) {
        this.maxSupply = maxSupply;
    }

    public MintDecision tryMint(String userId) {
        lock.lock();
        try {
            // 1) 이미 민팅한 유저인지 확인
            if (mintedUsers.contains(userId)) {
                return MintDecision.ofDuplicateUser();
            }

            // 2) 남은 수량 확인
            if (mintedCount >= maxSupply) {
                return MintDecision.ofSoldOut();
            }

            // 3) tokenId 발급
            int tokenId = nextTokenId;

            // 4) mintedCount 증가
            mintedCount++;

            // tokenId 증가
            nextTokenId++;

            // 5) 유저 기록(예약)
            mintedUsers.add(userId);

            return MintDecision.ofSuccess(tokenId);

        } finally {
            lock.unlock();
        }
    }

    public int mintedCount() {
        lock.lock();
        try {
            return mintedCount;
        } finally {
            lock.unlock();
        }
    }

    public int nextTokenId() {
        lock.lock();
        try {
            return nextTokenId;
        } finally {
            lock.unlock();
        }
    }

    public record MintDecision(boolean success, boolean soldOut, boolean duplicateUser, Integer tokenId) {
        public static MintDecision ofSuccess(int tokenId) {
            return new MintDecision(true, false, false, tokenId);
        }
        public static MintDecision ofSoldOut() {
            return new MintDecision(false, true, false, null);
        }
        public static MintDecision ofDuplicateUser() {
            return new MintDecision(false, false, true, null);
        }
    }
}