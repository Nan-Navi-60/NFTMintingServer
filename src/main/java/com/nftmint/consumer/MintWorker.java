package com.nftmint.consumer;

import com.nftmint.domain.MintRequest;
import com.nftmint.log.AuditLogger;
import com.nftmint.metrics.Metrics;
import com.nftmint.service.MintService;
import com.nftmint.service.MintingException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// 큐를 돌면서 큐에 req가 있으면 mintService에 넘김
public class MintWorker implements Runnable {

	private final BlockingQueue<MintRequest> queue;
	private final MintService mintService;
	private final Metrics metrics;
	private final AuditLogger auditLogger;
	private final AtomicBoolean stopSignal;

	public MintWorker(BlockingQueue<MintRequest> queue, MintService mintService, Metrics metrics,
			AuditLogger auditLogger, AtomicBoolean stopSignal) {
		this.queue = queue;
		this.mintService = mintService;
		this.metrics = metrics;
		this.auditLogger = auditLogger;
		this.stopSignal = stopSignal;
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (queue.isEmpty() && stopSignal.get())
					return;
				
				// poll과 take의 차이
				MintRequest req = queue.poll(200, TimeUnit.MILLISECONDS);
				if (req == null)
					continue;

				metrics.incConsumed();
				
				try {
					var result = mintService.mint(req);
					metrics.incSuccess();
					auditLogger.success(result);
				} catch (MintingException e) {
					String errorMessage = e.getMessage();
					if ("SOLD_OUT".equals(errorMessage)) {
						metrics.incSoldOut();
						auditLogger.soldOut(req.getUserId(), req.getRequestedPrice());
					} else if ("DUPLICATE_MINT".equals(errorMessage)) {
						metrics.incDuplicate();
						auditLogger.duplicateUser(req.getUserId(), req.getRequestedPrice());
					} else {
						metrics.incUnexpected();
					}
				} catch (Exception e) {
					metrics.incUnexpected();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}