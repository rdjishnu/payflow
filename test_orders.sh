#!/bin/bash
TOKEN="PASTE_YOUR_TOKEN_HERE"

for i in {1..8}; do
  curl -s -X POST localhost:8080/api/orders \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"customerId\":\"$i\",\"amount\":100,\"idempotencyKey\":\"test-key-$i\"}"
  echo ""
  sleep 1
done
