package com.nftmint.repo;

import com.nftmint.domain.MintResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintRepository {

    // userId -> MintResult
    private final Map<String, MintResult> mintedByUser = new ConcurrentHashMap<>();

    public boolean alreadyMinted(String userId) {
        return mintedByUser.containsKey(userId);
    }

    public void save(MintResult result) {
        // userId 기준으로 1회만 저장되도록 putIfAbsent를 사용 (방어)
        mintedByUser.putIfAbsent(result.getUserId(), result);
    }

    public int totalMintedUsers() {
        return mintedByUser.size();
    }

    public Map<String, MintResult> snapshot() {
        return Map.copyOf(mintedByUser);
    }
}