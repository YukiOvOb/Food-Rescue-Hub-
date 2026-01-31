#!/bin/bash

set -e

PROJECT_DIR="/root/Food-Rescue-Hub-/backend"
LOG_DIR="/var/log/food-rescue-hub"
BACKEND_LOG="$LOG_DIR/backend.log"

mkdir -p "$LOG_DIR"

if pgrep -f "spring-boot:run" > /dev/null 2>&1; then
  pkill -f "spring-boot:run" || true
  sleep 2
fi

cd "$PROJECT_DIR"
nohup ./mvnw spring-boot:run > "$BACKEND_LOG" 2>&1 &

echo "Backend started. Log: $BACKEND_LOG"
