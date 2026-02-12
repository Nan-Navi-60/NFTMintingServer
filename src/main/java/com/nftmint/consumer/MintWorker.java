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

	// Thread 실행
	@Override
	public void run() {
		while (true) {
			try {
				// 만약 큐가 비었거나 스탑 시그널을 받는 다면 즉시 종료
				if (queue.isEmpty() && stopSignal.get())
					return;
				
				// take는 값 변경이 없으면 슬립
				// poll은 아직 값이 없다면 200초 대기 후 값 전달 && stopSignal까지 확인
				MintRequest req = queue.poll(200, TimeUnit.MILLISECONDS);

				// 요청이 비어있는 지 확인
				if (req == null)
					continue;

				// 수행되는 횟수 증가
				metrics.incConsumed();
				
				try {
					// 민팅 결과
					var result = mintService.mint(req);
					// 성공 횟수 증가
					metrics.incSuccess();
					// 성공 로그 출력
					auditLogger.success(result);
				} catch (MintingException e) {
					// 에러 메시지 저장
					String errorMessage = e.getMessage();
					// errorMessage에 포함된 내용 확인
					// errorMessage에 "SOLD_OUT" 포함 시
					if ("SOLD_OUT".equals(errorMessage)) {
						// SoldOut 횟 수 증가
						metrics.incSoldOut();
						// SoldOut 로그 출력
						auditLogger.soldOut(req.getUserId(), req.getRequestedPrice());
					// errorMessage에 "DUPLICATE_MINT" 포함 시
					} else if ("DUPLICATE_MINT".equals(errorMessage)) {
						// DUPLICATE_MINT 횟 수 증가
						metrics.incDuplicate();
						// DUPLICATE_MINT 로그 출력
						auditLogger.duplicateUser(req.getUserId(), req.getRequestedPrice());
					} else {
						// 예상하지 못한 문제 횟 수 추가
						metrics.incUnexpected();
					}
				} catch (Exception e) {
					// 예상하지 못한 문제 횟 수 추가
					metrics.incUnexpected();
				}
			} catch (InterruptedException e) {
				// Thread의 현재 상태가 일시 정지 상태일 Thread 종료
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}