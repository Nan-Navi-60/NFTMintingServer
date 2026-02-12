# 🚀 NFT Minting Server

## 1. 프로젝트 목적


## 2. 시나리오
- NFT의 개수 : 1000개
- producerCount : producer 스레드 수 - 20
- workerCount : worker 스레드 수 - 8
- requestsPerProducer: Producer당 생성 요청 수 - 1000
- userPool : 사용자 풀 크기 - 5000
- queueSize : blockingQueue 크기 - 5000


## 3. 아키텍처
### 2.1. 전체 구조
- Producer: 요청 생성 및 큐에 삽입
- BlockingQueue: Producer와 Consumer 간 버퍼
- Consumer (Worker): 큐에서 요청을 가져와 처리
- Service Layer: 비즈니스 로직 처리

## 3. 주요 컴포넌트 상세 설명
### 3.1. Producer & Consumer
* **MintRequestProducer (생산자)**
  - 역할: 실시간 시세를 반영한 NFT 민팅 요청 생성 및 큐 삽입
  - 특징: `PriceFeed`에서 현재 가격을 조회하며, `userPoolSize` 내에서 랜덤 유저를 선택해 실제 중복 클릭 상황을 재현
* **MintWorker (소비자)**
  - 역할: 큐에서 요청을 인출하여 `MintService`에 전달
  - 특징: `poll(200ms)` 방식을 사용하여 대기 상태에서도 `stopSignal`에 반응

### 3.2. Service Layer
* **MintService**
  - 가격 유효 시간(3s) 검증을 통해 변화된 시세 기반 요청 차단
  - `SupplyManager`를 사용하여 민팅 성공 여부를 판결하고 결과를 `Repository`에 저장
* **SupplyManager**
  - **ReentrantLock**을 사용하여 최대 공급량 관리 및 1인 1회 중복 방지를 원자적으로 처리
* **PriceFeed**
  - 백그라운드 스레드에서 실시간으로 시세를 갱신하며 가격 정보를 제공

### 3.3. 인프라 계층
* **Metrics**: `AtomicLong`, `AtomicInteger`를 활용해 성공, 매진, 중복, 오류 수치를 스레드 안전하게 집계
* **AuditLogger**: `[MINTED]`, `[SOLD_OUT]`, `[DUPLICATE]` 등의 포맷으로 처리 현황 출력

## 4. 동작흐름
1. **초기화 단계**: `SupplyManager`, `MintRepository`, `PriceFeed` 생성 및 시세 엔진 가동
2. **실행 단계**: Producer 스레드들이 요청을 생성해 큐에 삽입하고, Worker 스레드들이 이를 가져와 비즈니스 로직 수행
3. **검증 단계**: `MintService`가 가격 만료, 재고 유무, 유저 중복 여부를 순차적으로 검증
4. **종료 단계**: 모든 요청 생성 완료 시 `stopSignal`을 작동시켜, 큐에 남은 잔무를 처리한 후 안전하게 종료 및 최종 리포트 출력

## 5. 동시성 제어 및 공유 자원 관리
### 5.1. 공유자원


### 5.2. 동시성 제어 포인트
- ReentrantLock : SupplyManager에서 잔여 재고 확인과
- `Atomic` 계열 변수

## 6. 예제 출력
### 6.1 실행 중 로그
- [MINTED] user=user-1234 tokenId=1 req=100000
- [DUPLICATE] user=user-1234 req=100000

### 6.2 최종 통계 출력
- produced: 20000
- consumed: 20000
- success: 4892
- rejected(soldOut): 0
- rejected(duplicateUser): 4892
- failed(unexpected): 0
