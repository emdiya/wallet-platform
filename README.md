# Wallet Digital Microservice Platform

A banking-style digital wallet platform built with Spring Boot microservices, PostgreSQL, Kafka, and Flyway.
This workspace now includes:
- banking-style customer/account identity in `auth-service`
- wallet account management and hold lifecycle in `wallet-service`
- transfer saga orchestration in `transfer-service`
- immutable audit ingestion in `ledger-service`
- centralized log ingestion and file persistence in `logger-service`

## System Architecture

```text
Client
  |
  v
┌──────────────────┐
│ auth-service     │  Registration / login / TPIN / account identity
└──────────────────┘
  | JWT + TPIN verify
  |-----------------------------------\
  v                                    \
┌──────────────────┐
│ wallet-service   │  Wallet account / self top-up / reserve / commit / release
└──────────────────┘
  |                            \
  | REST                        \ Kafka
  v                              v
┌──────────────────┐        ┌──────────────────┐
│ transfer-service │ -----> │ ledger-service   │
│ Saga orchestrator│        │ Audit consumer   │
└──────────────────┘        └──────────────────┘

Customer money actions:
- client authenticates with `auth-service`
- client sets/verifies TPIN through `auth-service`
- `wallet-service` and `transfer-service` call back to `auth-service` to verify TPIN on money movement

All services can send centralized app/request logs to:

┌──────────────────┐
│ logger-service   │  Stores logs in files and exposes query APIs
└──────────────────┘
```

## Modules

- `common-lib`: shared request metadata, logger client, startup logger, request logging filter, API response wrapper
- `auth-service`
- `wallet-service`
- `transfer-service`
- `ledger-service`
- `logger-service`

## Ports And Databases

| Service | HTTP Port | Database | DB Port |
|---|---:|---|---:|
| auth-service | 8081 | authdb | 5433 |
| wallet-service | 8082 | walletdb | 5434 |
| transfer-service | 8083 | transferdb | 5435 |
| ledger-service | 8084 | ledgerdb | 5436 |
| logger-service | 8085 | file-based | - |

## Kafka Topics

| Topic | Purpose |
|---|---|
| `wallet.events` | wallet creation, top-up, hold reserve, hold commit, hold release |
| `transfer.events` | transfer started, completed, failed |

## Check Kafka Events

List topics:

```bash
cd <project-root>
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

Read wallet events:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic wallet.events \
  --from-beginning
```

Read transfer events:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic transfer.events \
  --from-beginning
```

## Customer Journey

Recommended customer API order:
1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. `POST /api/auth/tpin/setup`
4. `GET /api/wallets/me`
5. Optional: `POST /api/wallets` to create extra wallet such as `KHR`
6. `POST /api/wallets/me/top-ups`
7. `POST /api/transfers`

## Full Flow

### 1. Customer registration

`auth-service` registers the user and returns banking-style identity fields:
- `customerId`
- `accountName`
- `accountNumber`

Registration now also auto-provisions one default `USD` wallet in `wallet-service`.

### 2. Login and TPIN setup

`auth-service` login returns the bearer token plus `hasTpin`.

Recommended customer flow:
1. Login with phone and password
2. If `hasTpin` is `false`, call `POST /api/auth/tpin/setup`
3. Use that TPIN for customer money actions

Banking-style split:
- password = account access
- TPIN = money authorization

### 3. Wallet provisioning and wallet view

`wallet-service` supports multiple wallets per user, one wallet per currency.

Default behavior:
- `auth-service` auto-creates the first `USD` wallet during registration

Additional behavior:
- the customer can request an extra wallet later, such as `KHR`

Current customer self-service behavior:
- the customer can fetch all of their wallets with `GET /api/wallets/me`
- the customer can create an extra wallet with `POST /api/wallets`
- for authenticated customer requests, `wallet-service` derives identity from the bearer token
- only `currency` is required in the request body for self-service wallet creation

The wallet starts with `0.00` balance and publishes a `WalletCreated` event.

### 4. Top-up flow

Customer self-service top-up uses `POST /api/wallets/me/top-ups`.

Flow:
1. Authenticate with bearer token
2. Verify TPIN through `auth-service`
3. Ensure the wallet belongs to the authenticated user
4. Check `operationId` for idempotency
5. Increase wallet balance
6. Store wallet operation
7. Publish `WalletTopUpCompleted`

### 5. Transfer flow

`transfer-service` orchestrates a wallet-to-wallet transfer by account number.

Flow:
1. Authenticate with bearer token
2. Verify TPIN through `auth-service`
3. Ensure the source wallet belongs to the authenticated user
4. Create transfer record with status `PROCESSING`
5. Call `wallet-service` to reserve hold on source account
6. Call `wallet-service` to top up destination account
7. Call `wallet-service` to commit the source hold
8. Mark transfer `COMPLETED`
9. Publish `TransferCompleted`

Failure path:
1. If any step after hold reserve fails, `transfer-service` attempts hold release
2. Transfer is marked `FAILED`
3. `TransferFailed` event is published

### 6. Ledger flow

`ledger-service` consumes `wallet.events` and `transfer.events` and stores immutable audit entries in `ledgerdb`.

Current behavior:
1. `wallet-service` publishes wallet domain events
2. `transfer-service` publishes transfer domain events
3. `ledger-service` consumes both topics
4. events are deduplicated by `eventId`
5. raw payload plus searchable fields are stored in `ledger_entries`
6. ledger entries can be queried by id, request id, account number, event type, topic, or hold id

## Request Metadata And Banking Headers

All HTTP services return these technical headers:
- `X-Trace-Id`
- `X-Hash-Id`
- `X-Request-Id`

Recommended client usage:
- `X-Trace-Id`: distributed tracing across services
- `X-Request-Id`: client/support correlation
- `X-Hash-Id`: internal log lookup
- `Idempotency-Key`: recommended for external money-moving APIs

Behavior:
- if client sends `X-Trace-Id`, service reuses it
- if client sends `X-Request-Id`, service reuses it
- if missing, service generates them
- `X-Hash-Id` is generated per request

## Service Details

### auth-service

Purpose:
- user registration
- login and JWT issuance
- banking-style customer/account identity generation

Database:
- `users`

Register response includes:
- `id`
- `customerId`
- `fullName`
- `accountName`
- `accountNumber`
- `phone`
- `hasTpin`
- `createdAt`

Endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/tpin/setup`
- `POST /api/auth/tpin/verify`
- `GET /api/auth/users/{id}`
- `GET /api/auth/users/by-phone?phone=...`

Example register:

```bash
curl -i -X POST http://localhost:8081/api/auth/register \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: trace-auth-001' \
  -d '{
    "fullName": "Dii Dev",
    "phone": "+85512345678",
    "password": "Password1"
  }'
```

### wallet-service

Purpose:
- wallet balance management
- account lookup
- top-up
- reserve hold
- commit hold
- release hold
- publish wallet events

Database:
- `wallets`
- `wallet_holds`
- `wallet_operations`

Key concepts:
- optimistic locking via `@Version`
- idempotency using `operationId`
- money stored as `numeric(19,2)` / `BigDecimal`

`operationId` rule:
- reuse the same `operationId` only when retrying the exact same request
- use a new `operationId` for every new top-up, hold, or money action
- examples:
  - `topup-usd-001`
  - `topup-khr-001`
  - `hold-transfer-001`

Endpoints:
- `POST /api/wallets`
- `GET /api/wallets/me`
- `GET /api/wallets/{id}`
- `GET /api/wallets?userId=...`
- `GET /api/wallets/by-customer?customerId=...`
- `GET /api/wallets/by-account?accountNumber=...`
- `POST /api/wallets/me/top-ups`
- `POST /api/wallets/top-ups`
- `POST /api/wallets/holds/reserve`
- `POST /api/wallets/holds/commit`
- `POST /api/wallets/holds/release`

Example create wallet:

```bash
curl -i -X POST http://localhost:8082/api/wallets \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "currency": "KHR"
  }'
```

Example fetch my wallets:

```bash
curl -i http://localhost:8082/api/wallets/me \
  -H 'Authorization: Bearer <access-token>'
```

Example top-up:

```bash
curl -i -X POST http://localhost:8082/api/wallets/me/top-ups \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "accountNumber": "855011234567890",
    "operationId": "topup-usd-001",
    "amount": 100.00,
    "purpose": "cash in",
    "tpin": "1234"
  }'
```

### transfer-service

Purpose:
- wallet-to-wallet transfer orchestration
- hold / credit / commit saga
- rollback via hold release on failure
- publish transfer events

Database:
- `transfers`

Stored transfer data includes:
- `requestId`
- `referenceNo`
- `fromAccountNumber`
- `toAccountNumber`
- `amount`
- `status`
- `holdId`
- `purpose`
- `errorCode`
- `errorMessage`

Endpoints:
- `POST /api/transfers`
- `GET /api/transfers/{id}`
- `GET /api/transfers?requestId=...`

Example transfer:

```bash
curl -i -X POST http://localhost:8083/api/transfers \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "requestId": "transfer-001",
    "fromAccountNumber": "855011234567890",
    "toAccountNumber": "855019876543210",
    "amount": 25.00,
    "purpose": "wallet transfer",
    "tpin": "1234"
  }'
```

### ledger-service

Purpose:
- consume Kafka events
- maintain immutable audit/ledger entries
- deduplicate processing

Endpoints:
- `GET /api/ledger/{id}`
- `GET /api/ledger?requestId=...`
- `GET /api/ledger?accountNumber=...`
- `GET /api/ledger?eventType=...`
- `GET /api/ledger?sourceTopic=wallet.events`
- `GET /api/ledger?holdId=...`

### logger-service

Purpose:
- centralized log ingestion
- query logs by id, service, trace id, hash id
- persist logs to files

Accepted log levels:
- `TRACE`
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`

Files:
- `logger-service/logs/main.log`
- `logger-service/logs/<source-service>.log`

Endpoints:
- `POST /api/logs`
- `GET /api/logs`
- `GET /api/logs/{id}`
- `GET /api/logs/by-service?serviceName=...`
- `GET /api/logs/by-trace?traceId=...`
- `GET /api/logs/by-hash?hashId=...`

## Run Modes

### Local Spring Boot + Docker infrastructure

This is the current recommended development mode:
- run databases and Kafka with Docker Compose
- run each Spring Boot service locally with `./mvnw spring-boot:run`

### Full Docker app stack

This repository currently provides Docker Compose services for infrastructure only:
- `kafka`
- `auth-db`
- `wallet-db`
- `transfer-db`
- `ledger-db`

Application containers for `auth-service`, `wallet-service`, `transfer-service`, `ledger-service`, and `logger-service` are not wired into `docker-compose.yml` yet.

## End-To-End Local Demo

### Prerequisites

- Java 25
- Docker Desktop or Docker Engine with Compose

### Shared configuration

Manage local credentials and service URLs from one file:

```bash
cp .env.example .env
```

Docker Compose reads `.env` automatically.

For local `spring-boot:run`, load the same variables into your shell first:

```bash
set -a
source .env
set +a
```

Notes:
- service configs no longer keep fallback values; load `.env` before local Spring runs
- using `.env` is recommended so Docker and local Spring runs use the same values
- starting one service does not guarantee every feature works; some flows depend on other services

### Start infrastructure with Docker

```bash
docker compose up -d auth-db wallet-db transfer-db ledger-db kafka
```

### Install shared module once

From the repository root:

```bash
./mvnw -pl common-lib -am install
```

### Start services locally from the repository root

Recommended order:
1. `logger-service`
2. `wallet-service`
3. `auth-service`
4. `transfer-service`
5. `ledger-service`

Commands:

```bash
set -a
source .env
set +a

cd logger-service && ./mvnw spring-boot:run
cd wallet-service && ./mvnw spring-boot:run
cd auth-service && ./mvnw spring-boot:run
cd transfer-service && ./mvnw spring-boot:run
cd ledger-service && ./mvnw spring-boot:run
```

This root workflow is the intended local run path today.

Single-service runtime notes:
- `logger-service` can run alone
- `ledger-service` can run alone if `ledger-db` and `kafka` are available
- `wallet-service` can start alone with `wallet-db` and `kafka`, but JWT and TPIN flows depend on `auth-service`
- `auth-service` can start alone with `auth-db`, but registration wallet provisioning depends on `wallet-service`
- `transfer-service` can start alone with `transfer-db` and `kafka`, but real transfer execution depends on `wallet-service` and `auth-service`

### Demo sequence

1. Register user A in `auth-service`
2. Register user B in `auth-service`
3. Login user A and set TPIN
4. Optional: create KHR wallet for user A in `wallet-service`
5. Top up user A wallet
6. Transfer from user A to user B
7. Verify audit ingestion in `ledger-service`

Ledger check example:

```bash
curl "http://localhost:8084/api/ledger?requestId=transfer-001"
```
6. Create transfer from A account to B account
7. Query logs in `logger-service` by trace id

### Example sequence

Register user A:

```bash
curl -s -X POST http://localhost:8081/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Alice Wallet","phone":"+85511111111","password":"Password1"}'
```

Register user B:

```bash
curl -s -X POST http://localhost:8081/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Bob Wallet","phone":"+85522222222","password":"Password1"}'
```

Each registration now auto-creates one default `USD` wallet.

Login to get a bearer token:

```bash
curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"+85511111111","password":"Password1"}'
```

The login response now includes `hasTpin` so the client knows whether TPIN setup is still required.

Set TPIN once after login:

```bash
curl -s -X POST http://localhost:8081/api/auth/tpin/setup \
  -H 'Authorization: Bearer <alice-access-token>' \
  -H 'Content-Type: application/json' \
  -d '{"tpin":"1234","confirmTpin":"1234"}'
```

If you want an extra `KHR` wallet, create it with the token:

```bash
curl -s -X POST http://localhost:8082/api/wallets \
  -H 'Authorization: Bearer <alice-access-token>' \
  -H 'Content-Type: application/json' \
  -d '{"currency":"KHR"}'
```

You can then verify both `USD` and `KHR` wallets:

```bash
curl -s http://localhost:8082/api/wallets/me \
  -H 'Authorization: Bearer <alice-access-token>'
```

After that:

```bash
curl -s -X POST http://localhost:8082/api/wallets/me/top-ups \
  -H 'Authorization: Bearer <alice-access-token>' \
  -H 'Content-Type: application/json' \
  -d '{"accountNumber":"<alice-account>","operationId":"topup-alice-001","amount":100.00,"purpose":"initial funding","tpin":"1234"}'

curl -s -X POST http://localhost:8083/api/transfers \
  -H 'Authorization: Bearer <alice-access-token>' \
  -H 'Content-Type: application/json' \
  -d '{"requestId":"transfer-alice-bob-001","fromAccountNumber":"<alice-account>","toAccountNumber":"<bob-account>","amount":25.00,"purpose":"peer transfer","tpin":"1234"}'
```

## Logging Notes

- Start `logger-service` first if you want centralized log capture.
- Start `kafka` before `wallet-service` and `transfer-service` if you want event publishing without request delays.
- If `logger-service` is down, services still run, but only local console logs are available.
- `logger-service` persists accepted logs into files.
- Shared logger client now supports `TRACE`, `DEBUG`, `INFO`, `WARN`, and `ERROR`.

## Build Commands

Build all modules:

```bash
./mvnw test
```

Build a single service with shared dependencies:

```bash
./mvnw -pl auth-service -am package
./mvnw -pl wallet-service -am package
./mvnw -pl transfer-service -am package
./mvnw -pl logger-service -am package
```

Install all modules:

```bash
./mvnw install
```
