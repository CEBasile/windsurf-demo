#!/bin/bash

# Security Testing Script for Ticket Application (curl version)
# This script tests the role-based access control with mock JWT tokens

BASE_URL="http://localhost:8080"

echo "=== Ticket Application Security Testing ==="
echo "Make sure the application is running on $BASE_URL"
echo ""

# Test tokens for different user types
ADMIN_TOKEN="mock-admin-admin123"
SUPPORT_TOKEN="mock-support-support456"
USER_TOKEN="mock-user-user789"
USER2_TOKEN="mock-user-user999"

function test_endpoint() {
    local method=$1
    local url=$2
    local token=$3
    local body=$4
    local description=$5
    
    echo "Testing: $description"
    echo "  $method $url"
    echo "  Token: $token"
    
    if [ -n "$body" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$body" \
            "$url" 2>/dev/null)
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method \
            -H "Authorization: Bearer $token" \
            "$url" 2>/dev/null)
    fi
    
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body_response=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_status" -ge 200 ] && [ "$http_status" -lt 300 ]; then
        echo "  ✅ SUCCESS - Status: $http_status"
        if [ -n "$body_response" ]; then
            echo "  Response: $body_response"
        fi
    else
        echo "  ❌ FAILED - Status: $http_status"
        if [ -n "$body_response" ]; then
            echo "  Error: $body_response"
        fi
    fi
    echo ""
}

echo "=== Step 1: Create tickets as different users ==="

# Create ticket as regular user
TICKET1='{"title":"Login Issue","description":"Cannot login to the system","priority":"HIGH","status":"OPEN"}'
test_endpoint "POST" "$BASE_URL/api/tickets" "$USER_TOKEN" "$TICKET1" "Regular user creates ticket"

# Create ticket as support user
TICKET2='{"title":"Server Performance","description":"Server is running slowly","priority":"MEDIUM","status":"OPEN"}'
test_endpoint "POST" "$BASE_URL/api/tickets" "$SUPPORT_TOKEN" "$TICKET2" "Support user creates ticket"

# Create ticket as admin
TICKET3='{"title":"Database Backup","description":"Need to backup database","priority":"LOW","status":"OPEN"}'
test_endpoint "POST" "$BASE_URL/api/tickets" "$ADMIN_TOKEN" "$TICKET3" "Admin creates ticket"

echo "=== Step 2: Test viewing all tickets (ADMIN/SUPPORT only) ==="

test_endpoint "GET" "$BASE_URL/api/tickets" "$ADMIN_TOKEN" "" "Admin views all tickets (should work)"
test_endpoint "GET" "$BASE_URL/api/tickets" "$SUPPORT_TOKEN" "" "Support views all tickets (should work)"
test_endpoint "GET" "$BASE_URL/api/tickets" "$USER_TOKEN" "" "Regular user views all tickets (should fail)"

echo "=== Step 3: Test viewing own tickets ==="

test_endpoint "GET" "$BASE_URL/api/tickets/my" "$USER_TOKEN" "" "Regular user views own tickets"
test_endpoint "GET" "$BASE_URL/api/tickets/my" "$SUPPORT_TOKEN" "" "Support user views own tickets"
test_endpoint "GET" "$BASE_URL/api/tickets/my" "$ADMIN_TOKEN" "" "Admin views own tickets"

echo "=== Step 4: Test viewing specific tickets (ownership check) ==="

test_endpoint "GET" "$BASE_URL/api/tickets/1" "$USER_TOKEN" "" "User views ticket #1 (should work if they own it)"
test_endpoint "GET" "$BASE_URL/api/tickets/1" "$USER2_TOKEN" "" "Different user views ticket #1 (should fail)"
test_endpoint "GET" "$BASE_URL/api/tickets/1" "$SUPPORT_TOKEN" "" "Support views ticket #1 (should work)"
test_endpoint "GET" "$BASE_URL/api/tickets/1" "$ADMIN_TOKEN" "" "Admin views ticket #1 (should work)"

echo "=== Step 5: Test updating tickets (ownership check) ==="

UPDATE_TICKET='{"title":"Login Issue - UPDATED","description":"Cannot login to the system - investigating","priority":"HIGH","status":"IN_PROGRESS"}'
test_endpoint "PUT" "$BASE_URL/api/tickets/1" "$USER_TOKEN" "$UPDATE_TICKET" "User updates own ticket"
test_endpoint "PUT" "$BASE_URL/api/tickets/1" "$USER2_TOKEN" "$UPDATE_TICKET" "Different user updates ticket (should fail)"
test_endpoint "PUT" "$BASE_URL/api/tickets/1" "$SUPPORT_TOKEN" "$UPDATE_TICKET" "Support updates any ticket (should work)"

echo "=== Step 6: Test deleting tickets (ADMIN only) ==="

test_endpoint "DELETE" "$BASE_URL/api/tickets/3" "$USER_TOKEN" "" "Regular user deletes ticket (should fail)"
test_endpoint "DELETE" "$BASE_URL/api/tickets/3" "$SUPPORT_TOKEN" "" "Support deletes ticket (should fail)"
test_endpoint "DELETE" "$BASE_URL/api/tickets/3" "$ADMIN_TOKEN" "" "Admin deletes ticket (should work)"

echo "=== Security Testing Complete ==="
echo ""
echo "Mock Token Format: mock-{userType}-{userId}"
echo "User Types: admin, support, user"
echo "Examples:"
echo "  - mock-admin-admin123 (ADMIN, SUPPORT, USER roles)"
echo "  - mock-support-support456 (SUPPORT, USER roles)"
echo "  - mock-user-user789 (USER role only)"
