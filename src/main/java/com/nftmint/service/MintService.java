package com.nftmint.service;

import com.nftmint.domain.MintRequest;
import com.nftmint.domain.MintResult;
import com.nftmint.repo.MintRepository;

public class MintService {
	
	private final SupplyManager supplyManager;
    private final MintRepository repository;
    
    private static final long QUOTE_TTL_MS = 3000; // 3초 유효
    
    public MintService(SupplyManager supplyManager, MintRepository repository) {
    	this.supplyManager = supplyManager;
    	this.repository = repository; 
	}
    
    public MintResult mint(MintRequest request) {

        long now = System.currentTimeMillis();

        // 가격 TTL 체크
        if (now - request.getQuotedAt() > QUOTE_TTL_MS) {
            throw new MintingException("PRICE_QUOTE_EXPIRED");
        }

        // 민팅 시도
        SupplyManager.MintDecision decision = supplyManager.tryMint(request.getUserId());

        if (decision.duplicateUser()) {
            throw new MintingException("DUPLICATE_MINT");
        }
        if (decision.soldOut()) {
            throw new MintingException("SOLD_OUT");
        }

        int tokenId = decision.tokenId();

        MintResult result = new MintResult(request.getUserId(), tokenId, request.getRequestedPrice());

        repository.save(result);
        return result;
    }

}