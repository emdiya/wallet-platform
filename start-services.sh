#!/usr/bin/env bash
set -euo pipefail

pids=()

SERVER_PORT=8081 java -jar /app/auth-service.jar &
pids+=("$!")

SERVER_PORT=8084 java -jar /app/ledger-service.jar &
pids+=("$!")

SERVER_PORT=8083 java -jar /app/transfer-service.jar &
pids+=("$!")

SERVER_PORT=8082 java -jar /app/wallet-service.jar &
pids+=("$!")

trap 'kill "${pids[@]}" 2>/dev/null || true; wait' TERM INT

wait -n
