#!/usr/bin/env bash
set -euo pipefail

APP_KEY="your-app-key"
APP_SECRET="your-app-secret"
BASE_URL="http://localhost:8088"
BODY='{"queryType":"medical_all","queryParams":{"name":"张三","idCard":"430102199001011234"}}'
TIMESTAMP="$(date +%s)000"
NONCE="$(openssl rand -hex 16)"
PAYLOAD="${TIMESTAMP}
${NONCE}
${BODY}"
SIGN="$(printf '%s' "${PAYLOAD}" | openssl dgst -sha256 -hmac "${APP_SECRET}" -binary | xxd -p -c 256)"

curl -sS -X POST "${BASE_URL}/api/v1/medical/query" \
  -H "Content-Type: application/json;charset=utf-8" \
  -H "X-App-Key: ${APP_KEY}" \
  -H "X-Timestamp: ${TIMESTAMP}" \
  -H "X-Nonce: ${NONCE}" \
  -H "X-Sign: ${SIGN}" \
  -d "${BODY}"

