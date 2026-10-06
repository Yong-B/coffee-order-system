# ☕ 커피숍 주문 시스템

> 포인트를 충전하여 커피를 주문하고, 최근 7일간의 주문 내역으로 인기 메뉴를 조회하는 시스템입니다.  
> 주문·결제 완료 후 Mock 데이터 수집 플랫폼으로 주문 정보를 전송하고, Kafka를 통해 결제 이력을 비동기로 기록합니다.

프론트엔드 없이 REST API로 구성했으며, Postman으로 주요 기능을 수동 확인했습니다.

---

## 🛠️ 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 17 |
| Backend | Spring Boot 4.1.1 · Spring Web MVC |
| Persistence | Spring Data JPA · MySQL |
| Validation | Jakarta Bean Validation |
| Cache | Redis · Spring Data Redis |
| Messaging | Apache Kafka · Spring Kafka |
| Authentication | HttpSession 기반 로그인 |
| Build | Gradle |
| Local Infra | Docker Desktop |
| API 확인 | Postman |
| 기타 | Lombok |

---

## 📦 패키지 구조

```text
com.example.coffee_order_system
├── domain
│   ├── member
│   │   ├── controller       # 로그인 및 회원 정보 조회
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   ├── menu
│   │   ├── controller       # 메뉴 목록 및 인기 메뉴 조회
│   │   ├── service          # DB 집계 및 캐시 조회
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   ├── point
│   │   ├── controller       # 포인트 충전
│   │   ├── service
│   │   ├── repository
│   │   ├── entity           # 포인트 계정 및 변동 이력
│   │   └── dto
│   ├── order
│   │   ├── controller       # 주문·결제 요청
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   └── payment
│       ├── service          # Kafka 결제 이력 저장
│       ├── repository
│       ├── entity           # 결제 원본 및 결제 이력
│       └── event            # PaymentCompletedEvent
├── infra
│   ├── kafka
│   │   ├── producer         # 결제 완료 이벤트 발행
│   │   └── consumer         # 결제 완료 이벤트 수신
│   ├── redis                # 인기 메뉴 캐시
│   └── dataplatform         # HTTP 클라이언트 및 Mock API
└── global
    ├── config
    ├── error                # ErrorCode, BusinessException, 예외 처리
    └── response             # ApiResponse
```

---

## 🗂️ ERD

<img width="262" height="657" alt="image" src="https://github.com/user-attachments/assets/40b417d8-bbac-46a7-9bc0-ff08878e1ef9" />

---

## 🧱 데이터 모델

| 테이블 | 역할 |
| --- | --- |
| `members` | 회원 정보와 로그인 정보 |
| `menus` | 메뉴 이름 및 현재 가격 |
| `point_accounts` | 회원별 현재 포인트 잔액 |
| `point_histories` | 포인트 충전·사용 이력 |
| `orders` | 주문한 회원·메뉴·주문금액·주문 상태 |
| `payments` | 실제 결제금액·결제 상태·결제 시각 |
| `payment_histories` | Kafka 이벤트를 수신하여 기록한 결제 이력 |

### 주요 관계

- 회원은 하나의 포인트 계정을 가집니다.
- 회원은 여러 주문을 생성할 수 있습니다.
- 주문 한 건은 메뉴 한 종류, 한 잔에 해당합니다.
- 주문과 결제는 1:1 관계입니다.
- 포인트 계정에는 여러 포인트 변동 이력이 연결됩니다.
- 결제 이력은 결제 ID를 기준으로 관리합니다.

### 이력 테이블 구분

`point_histories`는 포인트가 얼마나 증가하거나 감소했는지 기록합니다.  
`payments`는 실제 결제의 원본입니다.

`payment_histories`는 결제 완료 이벤트를 Kafka로 전달하고 Consumer가 저장하는 과정을 학습하기 위해 추가했습니다. Kafka 사용 자체에 필수인 테이블은 아니며, 현재는 결제 원본과 일부 정보가 중복됩니다.

---

## ✨ 핵심 기능

### 1. 커피 메뉴 목록 조회

- 메뉴 ID, 이름, 가격을 조회합니다.
- 메뉴 ID 오름차순으로 반환합니다.
- 등록된 메뉴가 없으면 빈 목록을 반환합니다.

### 2. 포인트 충전

- 로그인한 회원의 포인트를 충전합니다.
- 사용자 식별값은 로그인 세션에서 가져옵니다.
- 요청 본문으로 충전금액을 받습니다.
- 1원은 1P이며, 양의 정수만 충전할 수 있습니다.
- 충전 후 잔액과 포인트 충전 이력을 저장합니다.

### 3. 커피 주문·결제

- 로그인 세션의 사용자 식별값과 요청 본문의 메뉴 ID로 주문합니다.
- 주문 한 건은 메뉴 한 종류, 한 잔입니다.
- 서버에서 조회한 메뉴 가격만큼 포인트를 차감합니다.
- 포인트가 부족하면 주문을 거절합니다.
- 성공 시 주문과 결제 상태를 `PAID`로 저장합니다.
- 주문 당시의 결제금액을 별도로 보관합니다.
- 응답으로 주문·결제 ID, 결제금액, 결제 후 잔액 등을 반환합니다.

### 4. 데이터 수집 플랫폼 전송

- 주문·결제 DB 커밋 직후 HTTP 전송을 시도합니다.
- 사용자 ID, 메뉴 ID, 결제금액을 포함합니다.
- 로컬 Mock API를 데이터 수집 플랫폼으로 사용합니다.
- 수신 데이터는 서버 로그로 확인합니다.

### 5. 인기 메뉴 조회

- 집계 기준 시각 직전 168시간의 `PAID` 주문을 집계합니다.
- 주문 횟수가 많은 메뉴를 최대 3개 반환합니다.
- 주문 횟수가 같으면 메뉴 ID 오름차순으로 정렬합니다.
- 주문이 없는 메뉴는 제외합니다.
- MySQL 집계 결과를 Redis에 30초간 캐싱합니다.
- Redis를 사용할 수 없으면 MySQL에서 직접 조회합니다.

### 6. Kafka 결제 이력 기록

- 결제 완료 후 `PaymentCompletedEvent`를 발행합니다.
- Consumer가 이벤트를 읽어 `payment_histories`에 저장합니다.
- 실제 결제 처리는 Kafka 발행 전에 완료됩니다.

---

## 📋 구현 범위 및 정책

| 항목 | 정책 |
| --- | --- |
| 회원 등록 | SQL로 초기 회원 등록 |
| 로그인 | 로그인 ID·비밀번호 기반 세션 로그인 |
| 비밀번호 | 학습용 범위에서 평문 저장. 운영 환경 적용 대상 아님 |
| 메뉴 등록 | SQL로 초기 메뉴 등록 |
| 사용자 식별 | 세션의 `LOGIN_MEMBER_ID` 사용 |
| 초기 포인트 | 신규 계정 기준 0P |
| 결제 수단 | 포인트 |
| 주문 단위 | 메뉴 한 종류, 한 잔 |
| 금액 | 소수점 없는 정수 |
| 주문 취소·환불·재고 | 구현 범위에서 제외 |
| 시간 | UTC 기준 저장 및 응답 |
| 최근 7일 | 집계 기준 시각 직전 168시간 |
| 집계 구간 | 시작 시각 포함, 종료 시각 제외 |
| 캐시 유효기간 | 기본 30초 |
| 프론트엔드 | 구현 범위에서 제외 |
| API 확인 방식 | Postman 수동 확인 |

---

## 📡 API 명세

### 필수 API

| 기능 | Method | URL | 요청 | 성공 상태 |
| --- | --- | --- | --- | --- |
| 메뉴 목록 조회 | GET | `/api/menus` | 없음 | 200 |
| 포인트 충전 | POST | `/api/points/charges` | `amount` | 200 |
| 주문·결제 | POST | `/api/orders` | `menuId` | 201 |
| 인기 메뉴 조회 | GET | `/api/menus/popular` | 없음 | 200 |

포인트 충전과 주문은 로그인이 필요합니다. 사용자 ID를 요청 본문으로 받는 대신 세션에서 식별합니다.

### 로그인 및 확인용 API

| 기능 | Method | URL | 설명 |
| --- | --- | --- | --- |
| 로그인 | POST | `/api/members/login` | 세션 생성 |
| 내 정보 조회 | GET | `/api/members/me` | 회원 정보 및 포인트 잔액 조회 |
| Mock 주문 정보 수신 | POST | `/mock/order-events` | 서버가 호출하는 로컬 수집 API |

Mock API는 `local` 프로필에서 사용합니다.

### 로그인 요청

```http
POST /api/members/login
Content-Type: application/json
```

```json
{
  "loginId": "member1",
  "password": "password"
}
```

로그인 후 발급되는 `JSESSIONID` 쿠키로 인증 상태를 유지합니다.

### 메뉴 목록 조회

```http
GET /api/menus
```

```json
{
  "code": "SUCCESS",
  "data": {
    "menus": [
      {
        "menuId": 1,
        "name": "아메리카노",
        "price": 3000
      }
    ]
  }
}
```

### 포인트 충전

```http
POST /api/points/charges
Content-Type: application/json
```

```json
{
  "amount": 10000
}
```

```json
{
  "code": "SUCCESS",
  "data": {
    "memberId": 1,
    "chargedAmount": 10000,
    "balance": 10000
  }
}
```

### 주문·결제

```http
POST /api/orders
Content-Type: application/json
```

```json
{
  "menuId": 1
}
```

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 2,
    "paymentId": 2,
    "memberId": 1,
    "menuId": 1,
    "paymentAmount": 3000,
    "remainingPoints": 7000,
    "orderStatus": "PAID",
    "paymentStatus": "PAID",
    "paidAt": "2026-10-06T08:11:23.357622Z"
  }
}
```

위 잔액은 결제 전 10,000P인 경우의 예시입니다.

### 인기 메뉴 조회

```http
GET /api/menus/popular
```

```json
{
  "code": "SUCCESS",
  "data": {
    "periodStart": "2026-09-29T10:57:36.127823Z",
    "periodEnd": "2026-10-06T10:57:36.127823Z",
    "menus": [
      {
        "menuId": 1,
        "name": "아메리카노",
        "price": 3000,
        "orderCount": 2
      }
    ]
  }
}
```

- `periodStart`: 집계 시작 시각
- `periodEnd`: 집계 종료 시각
- `orderCount`: 해당 구간의 결제 완료 주문 횟수
- `price`: 현재 메뉴 가격

캐시를 재사용하면 집계 구간도 동일하게 반환합니다.

---

## 🚨 응답 및 예외 처리

핵심 API는 `ApiResponse<T>`로 응답합니다.

```json
{
  "code": "SUCCESS",
  "data": {}
}
```

실패 응답에는 오류 코드와 메시지를 반환합니다.

```json
{
  "code": "POINT_002",
  "message": "포인트가 부족합니다."
}
```

메시지는 `ErrorCode`에 정의한 값을 사용합니다.

`GlobalExceptionHandler`의 `@ExceptionHandler`에서 예외를 처리합니다.

| 상황 | HTTP 상태 |
| --- | --- |
| 잘못된 입력값·충전금액 | 400 |
| 로그인 필요·로그인 실패 | 401 |
| 회원·메뉴 등 대상 없음 | 404 |
| 포인트 부족 | 409 |
| 서버 내부 오류 | 500 |

업무 예외는 `BusinessException`으로 전달하며, DTO 입력 검증에는 Bean Validation을 사용합니다.

---

## 🎯 설계 의도 및 문제 해결 전략

### 회원과 포인트 계정 분리

회원 정보와 포인트 잔액의 역할을 구분하기 위해 `members`와 `point_accounts`를 분리했습니다.

회원은 로그인과 사용자 식별을 담당하고, 포인트 계정은 잔액을 관리합니다. 포인트 변동 내역은 `point_histories`에서 별도로 확인할 수 있습니다.

### 주문과 결제 분리

주문은 어떤 회원이 어떤 메뉴를 주문했는지 나타내고, 결제는 얼마를 어떤 상태로 결제했는지 나타냅니다.

두 역할을 분리하여 `orders`와 `payments`로 관리하고, 결제 성공 시 각각 `PAID` 상태를 저장합니다.

현재는 하나의 주문 API에서 포인트 결제까지 처리합니다. 내부 처리 중의 `PENDING`은 성공 시 `PAID`로 변경되며, 실패한 요청을 별도 주문 내역으로 보존하는 기능은 포함하지 않았습니다.

### 주문 당시 금액 보관

결제금액은 요청에서 받지 않고 서버의 메뉴 가격으로 결정합니다.

또한 메뉴 가격이 변경되더라도 기존 결제 내역을 유지할 수 있도록 주문과 결제에 당시 금액을 저장합니다.

### 인기 메뉴는 MySQL 집계 결과를 Redis에 캐싱

메뉴별 주문 횟수는 MySQL에 저장된 결제 완료 주문을 기준으로 계산합니다.

| 후보 방식 | 분석 |
| --- | --- |
| 매 요청마다 MySQL 집계 | 구현이 단순하지만 동일 집계가 반복됨 |
| Kafka 이벤트로 Redis 점수 누적 | 중복 이벤트, 반영 누락, 168시간이 지난 주문의 제외 처리 필요 |
| MySQL 집계 결과를 Redis에 캐싱 | 주문 원본을 기준으로 계산하면서 반복 집계를 줄일 수 있음 |

현재는 MySQL 집계 결과를 Redis에 캐싱하는 방식을 선택했습니다.

Redis에는 주문 횟수를 계속 증가시키는 방식이 아니라, 집계 결과 전체를 JSON으로 저장합니다.

```text
키: coffee:menus:popular:7days
유효기간: 30초
값: 집계 구간 및 인기 메뉴 목록
```

캐시가 없거나 만료되면 MySQL에서 다시 집계합니다. Redis 연결이나 저장에 실패하더라도 DB 조회 결과를 반환하도록 구성했습니다.

주문 횟수는 DB 집계 시점에 보이는 주문을 기준으로 계산합니다. 캐시가 유지되는 동안 최신 주문 반영은 지연될 수 있으므로 응답에 집계 구간을 함께 제공합니다.

### Kafka로 결제 후속 처리 경험

Kafka는 결제를 실행하는 용도로 사용하지 않습니다.

결제 완료 이벤트를 전달하고 Consumer가 별도의 결제 이력을 기록하도록 구성하여, 결제 처리와 후속 작업의 실행 시점을 분리했습니다.

현재의 결제 이력 기록은 Kafka 학습 목적을 포함합니다. 이 구조만으로 업무상 반드시 별도 결제 이력 테이블이 필요한 것은 아닙니다.

### Mock HTTP API로 주문 정보 전송

데이터 수집 플랫폼 연동은 실제 HTTP 요청을 보내는 방식으로 구현했습니다.

외부 서비스 대신 로컬 Mock API를 사용하여 사용자 ID, 메뉴 ID, 결제금액이 전달되는지 확인했습니다. 주기적인 일괄 전송이 아니라 주문·결제 커밋 직후 전송을 시도합니다.

---

## 🔄 핵심 처리 흐름

### 주문·결제 및 후속 처리

```text
POST /api/orders
        ↓
세션에서 회원 식별
        ↓
포인트 계정·메뉴 조회
        ↓
포인트 차감
주문·결제 PAID 처리
포인트 사용 이력 저장
        ↓
DB 커밋
        ├─ Kafka에 결제 완료 이벤트 발행
        │       ↓
        │   Consumer 수신
        │       ↓
        │   payment_histories 저장
        │
        └─ Mock 데이터 수집 API로 HTTP 전송
                ↓
            수신 로그 기록
```

Kafka 결제 이력 저장과 Mock API 전송은 별도 후속 작업입니다.

### 인기 메뉴 조회

```text
GET /api/menus/popular
        ↓
Redis 캐시 조회
        ├─ 유효한 캐시 있음 → 캐시 결과 반환
        │
        └─ 캐시 없음·만료·조회 실패
                ↓
            MySQL 최근 168시간 집계
                ↓
            Redis 저장 시도
                ↓
            집계 결과 반환
```

---

## 📨 Kafka 구성

| 항목 | 구성 |
| --- | --- |
| 클러스터 | 로컬 개발용 1개 |
| Broker | 1대 |
| 실행 컨테이너 | `coffee-kafka` |
| 연결 주소 | `localhost:9092` |
| Topic | `payment-completed` |
| Partition | 1개 |
| Replication factor | 1 |
| Consumer Group | `payment-history-group` |
| 메시지 Key | 결제 ID |
| 메시지 Value | 결제 완료 이벤트 JSON |

Consumer는 별도 서버가 아닌 Spring Boot 애플리케이션 내부에서 실행합니다.

### 이벤트 예시

```json
{
  "paymentId": 2,
  "orderId": 2,
  "memberId": 1,
  "menuId": 1,
  "paymentAmount": 3000,
  "paidAt": "2026-10-06T08:11:23.357622Z"
}
```

실제 결제 원본은 `payments`에 저장되며, Consumer가 이벤트를 처리한 후 `payment_histories`에 기록합니다.

---

## 🖥️ 다중 인스턴스 설계 방향

여러 애플리케이션 인스턴스가 동일한 MySQL, Redis, Kafka 클러스터를 공유하는 방향으로 설계했습니다.

```text
애플리케이션 인스턴스 A ─┐
                        ├─ MySQL
애플리케이션 인스턴스 B ─┼─ Redis
                        └─ Kafka 클러스터
```

- 메뉴·주문·결제의 원본은 공유 MySQL에 저장합니다.
- 인기 메뉴 캐시는 공유 Redis에 저장합니다.
- Kafka Consumer는 동일한 Consumer Group을 사용합니다.
- 인스턴스 간 로그인 상태를 유지하려면 공유 세션 구성이 필요합니다.

현재 실행 및 수동 확인은 단일 애플리케이션 인스턴스에서 진행했습니다. 공유 세션과 다중 인스턴스 배포는 이번 구현에 포함하지 않았습니다.

---

## ▶️ 로컬 실행

### 1. MySQL 준비

- `coffee_order` 데이터베이스를 생성합니다.
- 프로젝트 DDL을 적용합니다.
- 초기 회원, 해당 회원의 포인트 계정, 메뉴 데이터를 등록합니다.

### 2. Kafka 실행

최초 생성:

```powershell
docker run -d --name coffee-kafka -p 127.0.0.1:9092:9092 apache/kafka:4.1.2
```

기존 컨테이너 재시작:

```powershell
docker start coffee-kafka
```

### 3. Redis 실행

`redis-server` 컨테이너가 없다면 최초 생성합니다.

```powershell
docker run -d --name redis-server -p 127.0.0.1:6379:6379 redis:7
```

기존 컨테이너 재시작:

```powershell
docker start redis-server
```

같은 6379 포트를 사용하는 다른 Redis 컨테이너와 동시에 실행하지 않습니다.

### 4. 환경 설정

아래 설정을 기존 `application.yml`에 병합합니다. 기존 JPA 등 다른 설정은 유지합니다.

```yaml
spring:
  profiles:
    active: local

  datasource:
    url: jdbc:mysql://localhost:3306/coffee_order
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: localhost
      port: 6379
      connect-timeout: 1s
      timeout: 1s

  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      properties:
        enable.idempotence: true
        max.block.ms: 3000

    consumer:
      group-id: payment-history-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false
      auto-offset-reset: earliest

    listener:
      ack-mode: record

data-platform:
  url: http://localhost:8080/mock/order-events

ranking:
  cache-ttl: 30s
```

IntelliJ 실행 설정의 환경 변수에 `DB_PASSWORD`를 등록합니다.

### 5. 애플리케이션 실행

IntelliJ에서 `CoffeeOrderSystemApplication`을 실행합니다.

기본 API 주소:

```text
http://localhost:8080
```

### 6. Postman 요청 순서

```text
로그인
  ↓
메뉴 목록 조회
  ↓
포인트 충전
  ↓
주문·결제
  ↓
인기 메뉴 조회
```

---

## ✅ 수동 확인 결과

자동화 테스트는 작성하지 않았으며, Postman·DB 콘솔·애플리케이션 로그·Redis CLI로 아래 흐름을 확인했습니다.

| 항목 | 확인 내용 |
| --- | --- |
| 메뉴 목록 | 등록된 메뉴 조회 |
| 포인트 충전 | 충전 후 포인트 증가 |
| 주문·결제 | 결제 완료 및 `PAID` 이력 확인 |
| Mock 플랫폼 전송 | 사용자 ID·메뉴 ID·결제금액 수신 로그 확인 |
| Kafka | 결제 이벤트 발행 성공 및 결제 이력 DB 저장 |
| 인기 메뉴 API | 최근 7일 집계 구간과 메뉴별 주문 횟수 응답 |
| Redis 저장 | API 응답의 집계 결과와 Redis JSON 일치 |
| 캐시 재사용 | 반복 조회 시 동일한 `periodEnd` 반환 |
| 캐시 갱신 | 유효기간 경과 후 `periodEnd` 갱신 |

### Redis 확인 명령어

인기 메뉴 API 호출 직후 실행합니다.

```powershell
docker exec redis-server redis-cli GET coffee:menus:popular:7days
```

남은 유효기간 확인:

```powershell
docker exec redis-server redis-cli TTL coffee:menus:popular:7days
```

### 확인 범위

위 결과는 정상 요청 흐름에 대한 수동 확인입니다.

여러 메뉴의 순위·동률 정렬·7일 경계값·Redis 장애 상황을 모두 검증한 것은 아닙니다.

---

## 📌 현재 한계 및 개선 방향

- 인기 메뉴는 캐시 유효기간 동안 최신 주문 반영이 지연될 수 있습니다.
- Kafka는 단일 Broker 구성으로 실행합니다.
- DB 커밋 후 Kafka 발행 또는 Mock API 전송 전에 서버가 종료되면 후속 작업이 누락될 수 있습니다.
- Mock API 전송 실패에 대한 영속적인 자동 재시도는 구현하지 않았습니다.
- 동일한 주문·충전 요청을 반복 호출하면 별도 요청으로 처리될 수 있습니다.
- 로그인 세션은 현재 애플리케이션 인스턴스 내부에서 관리합니다.
- 실패한 주문과 결제를 별도 내역으로 보존하는 기능은 포함하지 않았습니다.****
