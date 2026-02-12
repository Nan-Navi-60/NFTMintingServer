# 🚀 NFT Minting Server

## 1. 프로젝트 목적
멀티스레드 환경에서의 동시성 문제와 데이터 정합성을 확인하기 위한 프로젝트입니다.
대량의 동시 민팅 요청을 Producer-Consumer 패턴으로 처리하고, 제한된 공급량/1인 1/가격 TTL 검증을 적용한 NFT 민팅 시뮬레이션 서버입니다.

## 2. 시나리오
### 2.1. 파라미터
| 항목 | 값 | 설명 |
|------|-----|------|
| **MAX_SUPPLY** | 3000개 | 최대 민팅 가능 수량 |
| **producerCount** | 20 | Producer 스레드 수 |
| **workerCount** | 8 | Worker 스레드 수 |
| **requestsPerProducer** | 1,000 | Producer당 생성 요청 수 (총 20,000건) |
| **userPoolSize** | 5,000 | 사용자 풀 크기 (중복 클릭 재현) |
| **queueSize** | 5,000 | BlockingQueue 용량 |

### 2.2. 시나리오 설명
- **NFT 공급 규모**: 최대 3000개 한정판 NFT 발행
- **요청 폭주 시뮬레이션**: 20개의 Producer 스레드가 각각 1,000건씩, 총 20,000건의 민팅 요청을 짧은 시간 내에 쏟아냄
- **사용자 행위 모델링**: 전체 사용자 풀을 5,000명으로 제한. 20,000건의 요청이 5,000명에게서 발생하므로, 특정 사용자의 '연타/중복 요청'이 빈번하게 발생하는 상황을 재현
- **비즈니스 제약 조건**: 1인 1회 민팅만 허용

### 2.2. 
1. NFT 초과 발행 방지
2. 중복 요청 차단 - 1인 1개
3. producer의 폭주 상황에서의 안정성

## 3. 아키텍처
### 2.1. 전체 구조
- **Producer**: 요청 생성 및 큐에 삽입
- **BlockingQueue**: Producer와 Consumer 간 버퍼
- **Consumer (Worker)**: 큐에서 요청을 가져와 처리
- **Service Layer**: 비즈니스 로직 처리

```
  [Producer x20]  ──►  [BlockingQueue]  ──►  [MintWorker x8]
       │                      │                      │
       │                      │                      ▼
       │                      │              [MintService]
       │                      │                      │
       │                      │         ┌────────────┼────────────┐
       │                      │         ▼            ▼            ▼
       │                      │   SupplyManager  MintRepository  PriceFeed
       │                      │
       └──────────────────────┴──────────► Metrics / AuditLogger
```

## 4. 주요 컴포넌트 상세 설명
### 4.1. Producer & Consumer
* **MintRequestProduce**
  - 역할: 실시간 시세를 반영한 NFT 민팅 요청 생성 및 큐 삽입
  - 특징: `PriceFeed`에서 현재 가격을 조회하며, `userPoolSize` 내에서 랜덤 유저를 선택해 실제 중복 클릭 상황을 재현
* **MintWorker**
  - 역할: 큐에서 요청을 인출하여 `MintService`에 전달
  - 특징: `poll(200ms)` 방식을 사용하여 대기 상태에서도 `stopSignal`에 반응

### 4.2. Service Layer
* **MintService**
  - 가격 유효 시간(3s) 검증을 통해 변화된 시세 기반 요청 차단
  - `SupplyManager`를 사용하여 민팅 성공 여부를 판결하고 결과를 `Repository`에 저장
* **SupplyManager**
  - **ReentrantLock**을 사용하여 최대 공급량 관리 및 1인 1회 중복 방지를 원자적으로 처리
* **PriceFeed**
  - 백그라운드 스레드에서 실시간으로 시세를 갱신하며 가격 정보를 제공

### 4.3. 인프라 계층
* **Metrics**: `AtomicLong`, `AtomicInteger`를 활용해 성공, 매진, 중복, 오류 수치를 스레드 안전하게 집계
* **AuditLogger**: `[MINTED]`, `[SOLD_OUT]`, `[DUPLICATE]` 등의 포맷으로 처리 현황 출력

## 5. 동작흐름
1. **초기화 단계**: `SupplyManager`, `MintRepository`, `PriceFeed` 생성 및 시세 엔진 가동
2. **실행 단계**: Producer 스레드들이 요청을 생성해 큐에 삽입하고, Worker 스레드들이 이를 가져와 비즈니스 로직 수행
3. **검증 단계**: `MintService`가 가격 만료, 재고 유무, 유저 중복 여부를 순차적으로 검증
4. **종료 단계**: 모든 요청 생성 완료 시 `stopSignal`을 작동시켜, 큐에 남은 잔무를 처리한 후 안전하게 종료 및 최종 리포트 출력

## 6. 동시성 제어 및 공유 자원 관리
### 6.1. 공유자원
| 공유 자원 | 설명 | thread-safe 처리 방식 |
|-----------|------|------------------------|
| **BlockingQueue&lt;MintRequest&gt;** | 대기열에 들어온 민팅 요청 | BlockingQueue 사용 → put()/poll()로 producer-consumer 패턴 구현 |
| **SupplyManager** | 남은 공급량·토큰 ID 발급·이미 민팅한 유저 집합 | ReentrantLock 사용 → tryMint() 메서드 전체를 임계 영역으로 보호, 단일 스레드만 성공 시 토큰 발급 |
| **MintRepository** | userId별 민팅 결과 저장 | ConcurrentHashMap 사용 → putIfAbsent 등으로 동시 쓰기 안전 |
| **Metrics** | 요청/성공/거부 수치 집계 | AtomicLong, AtomicInteger 사용 |
| **PriceFeed** | 현재 시세 | 백그라운드 스레드가 갱신, Worker는 읽기만 → 공유 읽기 허용 |
| **stopSignal** | Producer 종료 후 Worker 종료 알림 | AtomicBoolean 사용 |

### 6.2. 특징
- **ReentrantLock**: `SupplyManager`에서 잔여 재고 확인, 토큰 ID 발급, 중복 유저 체크를 한 묶음으로 원자적 처리
- **Atomic 계열**: `Metrics`, `stopSignal` 

## 7. 예제 출력
### 7.1 실행 중 로그
```
- [MINTED] user=user-1234 tokenId=1 req=100000
- [SOLD_OUT] user=user-3266 req=1000
- [DUPLICATE] user=user-3084 req=1000
```

### 7.2 최종 통계 출력
```
Final Metrics: Metrics
- produced: 20000
- consumed: 20000
- success: 3000
- rejected(soldOut): 6188
- rejected(duplicateUser): 10812
- failed(unexpected): 0
```

## 8. 프로젝트 구조
```
src/main/java/com/nftmint/
├── App.java
├── consumer/
│   └── MintWorker.java
├── producer/
│   └── MintRequestProducer.java
├── domain/
│   ├── MintRequest.java
│   └── MintResult.java
├── service/
│   ├── MintService.java
│   ├── SupplyManager.java
│   ├── PriceFeed.java
│   ├── PriceQuote.java
│   └── MintingException.java
├── repo/
│   └── MintRepository.java
├── metrics/
│   └── Metrics.java
└── log/
    └── AuditLogger.java
```
