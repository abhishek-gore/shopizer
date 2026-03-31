#!/bin/bash
set -e

BASE_URL=${1:-"http://localhost:8080"}
MAX_RETRIES=30
RETRY_INTERVAL=10

echo "🔍 Running smoke tests against: $BASE_URL"

check_endpoint() {
    local endpoint=$1
    local expected_status=${2:-200}
    local description=$3
    
    echo -n "Testing $description... "
    
    status_code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$endpoint" || echo "000")
    
    if [ "$status_code" -eq "$expected_status" ]; then
        echo "✅ PASS (HTTP $status_code)"
        return 0
    else
        echo "❌ FAIL (Expected: $expected_status, Got: $status_code)"
        return 1
    fi
}

echo "⏳ Waiting for application to be ready..."
retry_count=0
while [ $retry_count -lt $MAX_RETRIES ]; do
    if curl -sf "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo "✅ Application is ready!"
        break
    fi
    retry_count=$((retry_count + 1))
    echo "Attempt $retry_count/$MAX_RETRIES - waiting ${RETRY_INTERVAL}s..."
    sleep $RETRY_INTERVAL
done

if [ $retry_count -eq $MAX_RETRIES ]; then
    echo "❌ Application failed to start within timeout"
    exit 1
fi

failed_tests=0

check_endpoint "/actuator/health" 200 "Health Check" || ((failed_tests++))
check_endpoint "/actuator/info" 200 "Application Info" || ((failed_tests++))
check_endpoint "/swagger-ui/index.html" 200 "API Documentation" || ((failed_tests++))
check_endpoint "/api/v1/products?store=DEFAULT&lang=en" 200 "Product API" || ((failed_tests++))
check_endpoint "/api/v1/categories?store=DEFAULT&lang=en" 200 "Category API" || ((failed_tests++))

echo -n "Testing response time... "
response_time=$(curl -o /dev/null -s -w '%{time_total}' "$BASE_URL/actuator/health")
response_time_ms=$(echo "$response_time * 1000" | bc)
if (( $(echo "$response_time < 2.0" | bc -l) )); then
    echo "✅ PASS (${response_time_ms}ms)"
else
    echo "⚠️  SLOW (${response_time_ms}ms - threshold: 2000ms)"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $failed_tests -eq 0 ]; then
    echo "✅ All smoke tests passed!"
    exit 0
else
    echo "❌ $failed_tests test(s) failed"
    exit 1
fi
