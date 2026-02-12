package com.nftmint.log;

import com.nftmint.domain.MintResult;

public class AuditLogger {

    // 단순 출력. (필요하면 파일 출력/비동기 큐로 바꿔도 됨)
    public void success(MintResult result) {
        // 너무 많이 찍히면 콘솔이 느려져서 샘플로만 찍고 싶으면 조건 추가 가능
        // System.out.printf("[MINTED] user=%s tokenId=%d req=%d%n",
        //         result.userId(), result.tokenId(), result.requestId());
    }

    public void soldOut(String userId, long requestId) {
        // System.out.printf("[SOLD_OUT] user=%s req=%d%n", userId, requestId);
    }

    public void duplicateUser(String userId, long requestId) {
        // System.out.printf("[DUPLICATE] user=%s req=%d%n", userId, requestId);
    }
}