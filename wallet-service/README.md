# wallet-service

`wallet-service` manages wallet accounts, balances, hold operations, and wallet event publishing.

## Purpose

- create wallet accounts
- fetch wallet accounts
- top up wallet balances
- reserve funds using holds
- commit reserved holds
- release reserved holds
- publish wallet-related Kafka events

## Port And Database

- HTTP: `8082`
- Database: `walletdb`
- DB port: `5434`

## Tables

- `wallets`
- `wallet_holds`
- `wallet_operations`

## Key Concepts

- wallet identity uses:
  - `customerId`
  - `accountName`
  - `accountNumber`
- one wallet per currency per user
- default `USD` wallet is auto-created by `auth-service` registration
- extra wallets such as `KHR` can be created later
- balance uses `BigDecimal`
- wallet updates use optimistic locking via `@Version`
- `operationId` is used for idempotent top-up / hold actions

`operationId` rule:
- reuse the same `operationId` only when retrying the same request after timeout or network failure
- generate a new `operationId` for each new top-up or hold action
- examples:
  - `topup-usd-001`
  - `topup-khr-001`
  - `hold-reserve-001`

## Endpoints

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

## Wallet Flow

### Create wallet

1. For authenticated customer requests, resolve the wallet owner from the bearer token
2. Validate uniqueness for `(userId, currency)`
3. Generate `accountNumber` if client does not provide one
4. Create wallet with starting balance `0.00`
5. Publish `WalletCreated`

### Top-up

Customer self-service top-up:
1. Authenticate with bearer token
2. Verify TPIN with `auth-service`
3. Ensure the wallet belongs to the authenticated user
4. Increase balance
5. Store wallet operation
6. Publish `WalletTopUpCompleted`

Internal top-up:
1. Find wallet by `accountNumber`
2. Check `operationId` for idempotency
3. Increase balance
4. Store wallet operation
5. Publish `WalletTopUpCompleted`

### Hold reserve

1. Find wallet by `accountNumber`
2. Check `operationId`
3. Ensure sufficient available balance
4. Decrease available wallet balance immediately
5. Create hold with `RESERVED`
6. Publish `WalletHoldReserved`

### Hold commit

1. Find hold by `holdId`
2. Check `operationId`
3. Confirm the reserved amount should now become a final debit
4. Change status to `COMMITTED`
4. Publish `WalletHoldCommitted`

### Hold release

1. Find hold by `holdId`
2. Check `operationId`
3. Restore held amount to wallet balance
4. Change status to `RELEASED`
5. Publish `WalletHoldReleased`

Hold lifecycle meaning:
- `RESERVED` = money is locked temporarily
- `COMMITTED` = money is finally debited
- `RELEASED` = money is unlocked and returned

## Example Requests

### Create wallet

```bash
curl -i -X POST http://localhost:8082/api/wallets \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "currency": "KHR"
  }'
```

This is the expected way to create an additional wallet such as `KHR` after the default `USD` wallet already exists.

### Get my wallets

```bash
curl -i http://localhost:8082/api/wallets/me \
  -H 'Authorization: Bearer <access-token>'
```

### Top-up

```bash
curl -i -X POST http://localhost:8082/api/wallets/me/top-ups \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "accountNumber": "855011234567890",
    "operationId": "topup-khr-001",
    "amount": 100.00,
    "purpose": "cash in",
    "tpin": "1234"
  }'
```

### Reserve hold

```bash
curl -i -X POST http://localhost:8082/api/wallets/holds/reserve \
  -H 'Content-Type: application/json' \
  -d '{
    "accountNumber": "855011234567890",
    "holdId": "hold-001",
    "operationId": "hold-reserve-001",
    "amount": 25.00,
    "purpose": "transfer reserve"
  }'
```

### Commit hold

```bash
curl -i -X POST http://localhost:8082/api/wallets/holds/commit \
  -H 'X-Internal-Api-Key: wallet-platform-internal-dev-key' \
  -H 'Content-Type: application/json' \
  -d '{
    "holdId": "hold-001",
    "operationId": "hold-commit-001",
    "purpose": "transfer commit"
  }'
```

### Release hold

```bash
curl -i -X POST http://localhost:8082/api/wallets/holds/release \
  -H 'X-Internal-Api-Key: wallet-platform-internal-dev-key' \
  -H 'Content-Type: application/json' \
  -d '{
    "holdId": "hold-001",
    "operationId": "hold-release-001",
    "purpose": "transfer rollback"
  }'
```

## Kafka

Topic:
- `wallet.events`

Behavior:
- Kafka must be running on `localhost:9092`
- `wallet.events` is created automatically by Spring on startup
- if Kafka is unavailable, wallet creation may still succeed but event delivery will fail

Published event types:
- `WalletCreated`
- `WalletTopUpCompleted`
- `WalletHoldReserved`
- `WalletHoldCommitted`
- `WalletHoldReleased`

Check emitted events:

```bash
cd <project-root>
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic wallet.events \
  --from-beginning
```

## Local Run

```bash
set -a
source ../.env
set +a

docker compose up -d wallet-db kafka
cd <project-root>/wallet-service
./mvnw spring-boot:run
```

Run notes:
- `wallet-service` now requires env vars to be loaded before startup
- load `../.env` first as shown above so local Spring and Docker Compose use the same values
- `wallet-db` and `kafka` should be available for normal wallet behavior
- customer JWT and TPIN-protected endpoints depend on `auth-service`
- internal wallet write endpoints can still run without `auth-service` if they do not require TPIN verification

## Key Source Files

- `src/main/java/com/kd/wallet/wallet/controller/WalletController.java`
- `src/main/java/com/kd/wallet/wallet/service/impl/WalletServiceImpl.java`
- `src/main/java/com/kd/wallet/wallet/service/impl/KafkaWalletEventPublisher.java`
- `src/main/resources/db/migration/V2__add_banking_wallet_columns.sql`
- `src/main/resources/db/migration/V3__allow_multiple_wallets_per_user.sql`
