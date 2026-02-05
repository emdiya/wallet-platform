# 💳 Wallet Digital Microservice Platform

A **banking-style digital wallet system** built with **Spring Boot microservices**, **Apache Kafka**, and **PostgreSQL**.
This project demonstrates **real-world backend patterns** such as **Saga orchestration**, **event-driven architecture**, **idempotency**, and **consumer-based ledger auditing**.

---

## 🧩 Architecture Overview

The system is designed as **independent microservices**, each owning its own database and responsibility.

---
## 🧩 System Architecture

```text
Client
  |
  v
REST APIs
  |
  v
┌──────────────────┐
│ auth-service     │  User authentication
└──────────────────┘
        |
┌──────────────────┐        ┌────────────────────┐
│ wallet-service   │ <––>   │ transfer-service   │
│ Balance / Holds  │        │ Saga Orchestrator  │
└──────────────────┘        └────────────────────┘
        |
        | Kafka Events
        v
┌──────────────────┐
│ ledger-service   │  Kafka Consumer (Audit Ledger)
└──────────────────┘
```

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 4**
- **Gradle**
- **PostgreSQL**
- **Apache Kafka**
- **Flyway** (DB migration)
- **Docker & Docker Compose**
- **Spring Data JPA**
- **Spring for Apache Kafka**

---

## 📦 Services

### 1️⃣ auth-service
**Purpose**
- User registration & authentication
- Owns `users` table

**Port**
- `8081`

**Database**
- `authdb`

---

### 2️⃣ wallet-service
**Purpose**
- Wallet balance management
- Reserve (hold), commit, release funds
- Publish wallet-related Kafka events

**Port**
- `8082`

**Database**
- `walletdb`

**Key Concepts**
- Optimistic locking (`@Version`)
- Idempotent operations
- BigDecimal for money

---

### 3️⃣ transfer-service
**Purpose**
- Money transfer orchestration (Saga pattern)
- Coordinates between wallets
- Publishes transfer events to Kafka

**Port**
- `8083`

**Database**
- `transferdb`

**Saga Flow**
1. Place hold on sender wallet
2. Credit receiver wallet
3. Commit sender hold
4. Publish `TransferCompleted`
5. On failure → release hold + publish `TransferFailed`

---

### 4️⃣ ledger-service (Kafka Consumer)
**Purpose**
- Consume Kafka events
- Store immutable ledger records
- Prevent duplicate processing

**Port**
- `8084`

**Database**
- `ledgerdb`

**Key Concepts**
- Event deduplication
- At-least-once delivery safety
- Immutable ledger entries

---

## 📡 Kafka Topics

| Topic Name | Description |
|-----------|------------|
| `wallet.events` | Wallet credit/hold events |
| `transfer.events` | Transfer success/failure events |

---

## 🗄️ Databases

Each service owns **its own database** (true microservice isolation):

| Service | Database | Port |
|------|---------|------|
| auth-service | authdb | 5433 |
| wallet-service | walletdb | 5434 |
| transfer-service | transferdb | 5435 |
| ledger-service | ledgerdb | 5436 |

---

## 🐳 Run Infrastructure (Kafka + Postgres)

```bash
docker compose up -d
```

---

## 🐳 Run All Services in One Container

```bash
docker build -t wallet-platform-all .
docker run --rm \
  -p 8081:8081 \
  -p 8082:8082 \
  -p 8083:8083 \
  -p 8084:8084 \
  wallet-platform-all
```

---

## 🐳 Run Services Individually

```bash
# auth-service
docker build -t auth-service auth-service
docker run --rm -p 8081:8081 auth-service

# wallet-service
docker build -t wallet-service wallet-service
docker run --rm -p 8082:8082 wallet-service

# transfer-service
docker build -t transfer-service transfer-service
docker run --rm -p 8083:8083 transfer-service

# ledger-service
docker build -t ledger-service ledger-service
docker run --rm -p 8084:8084 ledger-service
```
