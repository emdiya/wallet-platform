# ledger-service

`ledger-service` is the immutable audit consumer for `wallet.events` and `transfer.events`.

## Current Status

What exists now:
- Spring Boot application bootstrap
- shared request logging filter
- logger client integration
- Kafka consumers for wallet and transfer events
- immutable ledger entry persistence in `ledgerdb`
- event deduplication by `eventId`
- ledger query APIs

## Purpose

- consume wallet and transfer events
- store immutable financial audit entries
- support at-least-once Kafka delivery safely
- provide audit trail for banking-style wallet operations

## Port And Database

- HTTP: `8084`
- Database: `ledgerdb`
- DB port: `5436`

## Kafka Inputs

- `wallet.events`
- `transfer.events`

## API

- `GET /api/ledger/{id}`
- `GET /api/ledger?requestId=...`
- `GET /api/ledger?accountNumber=...`
- `GET /api/ledger?eventType=...`
- `GET /api/ledger?sourceTopic=wallet.events`
- `GET /api/ledger?holdId=...`

Example:

```bash
curl "http://localhost:8084/api/ledger?requestId=transfer-001"
```

## Local Run

```bash
set -a
source ../.env
set +a

docker compose up -d ledger-db kafka
cd <project-root>/ledger-service
./mvnw spring-boot:run
```

Run notes:
- `ledger-service` now requires env vars to be loaded before startup
- load `../.env` first as shown above so local Spring and Docker Compose use the same values
- `ledger-db` and `kafka` should be available
- actual audit ingestion only happens when `wallet-service` or `transfer-service` publish events

## Key Source Files

- `src/main/java/com/kd/wallet/ledger/LedgerServiceApplication.java`
- `src/main/java/com/kd/wallet/ledger/controller/LedgerController.java`
- `src/main/java/com/kd/wallet/ledger/service/impl/LedgerKafkaConsumer.java`
- `src/main/java/com/kd/wallet/ledger/service/impl/LedgerEventIngestionService.java`
- `src/main/java/com/kd/wallet/ledger/entity/LedgerEntry.java`
- `src/main/resources/application.yaml`
- `src/main/resources/db/migration/V1__init.sql`
