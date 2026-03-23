# logger-service

`logger-service` provides centralized log ingestion, file persistence, and query APIs for the platform.

## Purpose

- receive logs from all services
- validate log levels
- persist logs into file-based storage
- query logs by id, service, trace id, and hash id

## Port And Storage

- HTTP: `8085`
- Storage: file-based
- Default log directory: `logger-service/logs`

Config:
- `LOGGER_FILE_DIRECTORY=/absolute/path`

## Accepted Log Levels

- `TRACE`
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`

## Files

Each accepted log entry is written to:
- `logs/main.log`
- `logs/<source-service>.log`

## Endpoints

- `POST /api/logs`
- `GET /api/logs`
- `GET /api/logs/{id}`
- `GET /api/logs/by-service?serviceName=...`
- `GET /api/logs/by-trace?traceId=...`
- `GET /api/logs/by-hash?hashId=...`

## Example Requests

### Create log

```bash
curl -i -X POST http://localhost:8085/api/logs \
  -H 'Content-Type: application/json' \
  -d '{
    "sourceService": "wallet-service",
    "level": "DEBUG",
    "message": "Wallet lookup",
    "traceId": "trace-demo-001",
    "hashId": "abcdef1234567890",
    "details": "accountNumber=855011234567890"
  }'
```

### Query by trace id

```bash
curl "http://localhost:8085/api/logs/by-trace?traceId=trace-demo-001"
```

## Behavior Notes

- services can keep running even if `logger-service` is down
- if `logger-service` is unavailable, services fall back to local console logging only
- the shared logger client now supports `TRACE`, `DEBUG`, `INFO`, `WARN`, and `ERROR`

## Local Run

```bash
set -a
source ../.env
set +a

cd <project-root>/logger-service
./mvnw spring-boot:run
```

Run notes:
- `logger-service` can run fully by itself
- it requires `LOGGER_SERVICE_PORT` and `LOGGER_FILE_DIRECTORY`, so load the shared root `.env` first
- other services can run without `logger-service`; they fall back to console logging when it is unavailable

## Key Source Files

- `src/main/java/com/kd/wallet/logger/controller/LogController.java`
- `src/main/java/com/kd/wallet/logger/service/impl/LogServiceImpl.java`
- `src/main/java/com/kd/wallet/logger/repository/LogEntryRepository.java`
- `src/main/java/com/kd/wallet/logger/util/LogLevelUtils.java`
