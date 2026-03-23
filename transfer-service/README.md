# transfer-service

`transfer-service` orchestrates wallet-to-wallet transfers using a saga-style flow against `wallet-service`.

## Purpose

- create transfer records
- reserve funds from source wallet
- credit destination wallet
- commit reserved hold
- release reserved hold on failure
- publish transfer events

## Port And Database

- HTTP: `8083`
- Database: `transferdb`
- DB port: `5435`

## Table

- `transfers`

Stored fields include:
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

Request meaning:
- `requestId` = idempotency key for the transfer request
- `fromAccountNumber` = source wallet to be debited
- `toAccountNumber` = destination wallet to be credited
- `tpin` = customer transaction PIN required to authorize the transfer

## Endpoints

- `POST /api/transfers`
- `GET /api/transfers/{id}`
- `GET /api/transfers?requestId=...`

## Saga Flow

### Happy path

1. Require authenticated user
2. Verify TPIN through `auth-service`
3. Ensure the source wallet belongs to the authenticated user
4. Create transfer row with status `PROCESSING`
5. Call `wallet-service` hold reserve on source account
6. Call `wallet-service` top-up on destination account
7. Call `wallet-service` hold commit on source account
8. Mark transfer `COMPLETED`
9. Publish `TransferCompleted`

### Failure path

1. If any step fails after reserve
2. Attempt hold release
3. Mark transfer `FAILED`
4. Store `errorCode` and `errorMessage`
5. Publish `TransferFailed`

## Example Request

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

## Dependencies

Runtime dependencies:
- `transfer-db`
- `kafka`
- `wallet-service` running on `http://localhost:8082`
- `auth-service` running on `http://localhost:8081` for TPIN verification

## Kafka

Topic:
- `transfer.events`

Published event types:
- `TransferStarted`
- `TransferCompleted`
- `TransferFailed`

Check emitted events:

```bash
cd <project-root>
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic transfer.events \
  --from-beginning
```

## Local Run

```bash
set -a
source ../.env
set +a

docker compose up -d transfer-db wallet-db kafka
cd <project-root>/wallet-service
./mvnw spring-boot:run

cd <project-root>/transfer-service
./mvnw spring-boot:run
```

Run notes:
- `transfer-service` now requires env vars to be loaded before startup
- load `../.env` first as shown above so local Spring and Docker Compose use the same values
- `transfer-db` and `kafka` should be available for normal startup and event publishing
- real transfer execution depends on both `wallet-service` and `auth-service`

## Key Source Files

- `src/main/java/com/kd/wallet/transfer/controller/TransferController.java`
- `src/main/java/com/kd/wallet/transfer/service/impl/TransferServiceImpl.java`
- `src/main/java/com/kd/wallet/transfer/service/impl/HttpWalletClient.java`
- `src/main/java/com/kd/wallet/transfer/service/impl/KafkaTransferEventPublisher.java`
- `src/main/resources/db/migration/V2__add_banking_transfer_columns.sql`
