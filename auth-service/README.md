# auth-service

`auth-service` is responsible for user registration, login, and banking-style account identity generation.

## Purpose

- register users
- authenticate users and issue JWT access tokens
- manage and verify TPIN for money movement
- expose user lookup APIs
- generate banking identity fields used by downstream wallet flows

## Port And Database

- HTTP: `8081`
- Database: `authdb`
- DB port: `5433`

## Data Model

The `users` table stores:
- `id`
- `customer_id`
- `full_name`
- `account_name`
- `account_number`
- `phone`
- `password_hash`
- `tpin_hash`
- `created_at`

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/tpin/setup`
- `POST /api/auth/tpin/verify`
- `GET /api/auth/users/{id}`
- `GET /api/auth/users/by-phone?phone=...`

## Register Flow

1. Validate full name, phone, and password
2. Normalize phone
3. Reject duplicate phone
4. Hash password
5. Generate `customerId`
6. Generate `accountName`
7. Generate `accountNumber`
8. Persist user
9. Auto-create default `USD` wallet in `wallet-service`
10. Return banking-style identity data

## TPIN Flow

1. User logs in and receives JWT access token
2. User calls `POST /api/auth/tpin/setup` once to configure a 4-6 digit TPIN
3. `auth-service` stores only `tpin_hash`, never plain TPIN
4. `wallet-service` and `transfer-service` call `POST /api/auth/tpin/verify` during customer money actions
5. TPIN is used for top-up and transfer authorization, separate from login password

## Example Requests

### Register

```bash
curl -i -X POST http://localhost:8081/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName": "Dii Dev",
    "phone": "+85512345678",
    "password": "Password1"
  }'
```

### Login

```bash
curl -i -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "phone": "+85512345678",
    "password": "Password1"
  }'
```

### Setup TPIN

```bash
curl -i -X POST http://localhost:8081/api/auth/tpin/setup \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "tpin": "1234",
    "confirmTpin": "1234"
  }'
```

## Response Notes

Register and login responses include:
- `customerId`
- `accountName`
- `accountNumber`
- `hasTpin`

These values are intended for wallet lookup and later top-up / transfer flows.

Registration behavior:
- automatically provisions one default `USD` wallet
- additional wallets such as `KHR` can be created later through `wallet-service`

## Local Run

```bash
set -a
source ../.env
set +a

docker compose up -d auth-db wallet-db kafka
cd <project-root>/wallet-service
./mvnw spring-boot:run

cd <project-root>/auth-service
./mvnw spring-boot:run
```

Run notes:
- `auth-service` now requires env vars to be loaded before startup
- load `../.env` first as shown above so local Spring and Docker Compose use the same values
- registration auto-provisions the default wallet, so `POST /api/auth/register` depends on `wallet-service` being available
- login and user lookup can still work without `wallet-service`

## Key Source Files

- `src/main/java/com/kd/wallet/auth/controller/AuthController.java`
- `src/main/java/com/kd/wallet/auth/service/impl/AuthServiceImpl.java`
- `src/main/java/com/kd/wallet/auth/util/BankingIdentityUtils.java`
- `src/main/resources/db/migration/V2__add_banking_identity_columns.sql`
