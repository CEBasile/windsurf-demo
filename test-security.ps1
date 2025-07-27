# Security Testing Script for Ticket Application
# This script tests the role-based access control with mock JWT tokens

$baseUrl = "http://localhost:8080"

Write-Host "=== Ticket Application Security Testing ===" -ForegroundColor Green
Write-Host "Make sure the application is running on $baseUrl" -ForegroundColor Yellow
Write-Host ""

# Test tokens for different user types
$adminToken = "mock-admin-admin123"
$supportToken = "mock-support-support456" 
$userToken = "mock-user-user789"
$user2Token = "mock-user-user999"

function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Token,
        [string]$Body = $null,
        [string]$Description
    )
    
    Write-Host "Testing: $Description" -ForegroundColor Cyan
    Write-Host "  $Method $Url" -ForegroundColor Gray
    Write-Host "  Token: $Token" -ForegroundColor Gray
    
    try {
        $headers = @{
            "Authorization" = "Bearer $Token"
            "Content-Type" = "application/json"
        }
        
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers
        }
        
        Write-Host "  ✅ SUCCESS" -ForegroundColor Green
        if ($response) {
            Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  ❌ FAILED - Status: $statusCode" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "=== Step 1: Create tickets as different users ===" -ForegroundColor Yellow

# Create ticket as regular user
$ticket1 = @{
    title = "Login Issue"
    description = "Cannot login to the system"
    priority = "HIGH"
    status = "OPEN"
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Url "$baseUrl/api/tickets" -Token $userToken -Body $ticket1 -Description "Regular user creates ticket"

# Create ticket as support user
$ticket2 = @{
    title = "Server Performance"
    description = "Server is running slowly"
    priority = "MEDIUM"
    status = "OPEN"
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Url "$baseUrl/api/tickets" -Token $supportToken -Body $ticket2 -Description "Support user creates ticket"

# Create ticket as admin
$ticket3 = @{
    title = "Database Backup"
    description = "Need to backup database"
    priority = "LOW"
    status = "OPEN"
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Url "$baseUrl/api/tickets" -Token $adminToken -Body $ticket3 -Description "Admin creates ticket"

Write-Host "=== Step 2: Test viewing all tickets (ADMIN/SUPPORT only) ===" -ForegroundColor Yellow

Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets" -Token $adminToken -Description "Admin views all tickets (should work)"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets" -Token $supportToken -Description "Support views all tickets (should work)"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets" -Token $userToken -Description "Regular user views all tickets (should fail)"

Write-Host "=== Step 3: Test viewing own tickets ===" -ForegroundColor Yellow

Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/my" -Token $userToken -Description "Regular user views own tickets"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/my" -Token $supportToken -Description "Support user views own tickets"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/my" -Token $adminToken -Description "Admin views own tickets"

Write-Host "=== Step 4: Test viewing specific tickets (ownership check) ===" -ForegroundColor Yellow

Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/1" -Token $userToken -Description "User views ticket #1 (should work if they own it)"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/1" -Token $user2Token -Description "Different user views ticket #1 (should fail)"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/1" -Token $supportToken -Description "Support views ticket #1 (should work)"
Test-Endpoint -Method "GET" -Url "$baseUrl/api/tickets/1" -Token $adminToken -Description "Admin views ticket #1 (should work)"

Write-Host "=== Step 5: Test updating tickets (ownership check) ===" -ForegroundColor Yellow

$updateTicket = @{
    title = "Login Issue - UPDATED"
    description = "Cannot login to the system - investigating"
    priority = "HIGH"
    status = "IN_PROGRESS"
} | ConvertTo-Json

Test-Endpoint -Method "PUT" -Url "$baseUrl/api/tickets/1" -Token $userToken -Body $updateTicket -Description "User updates own ticket"
Test-Endpoint -Method "PUT" -Url "$baseUrl/api/tickets/1" -Token $user2Token -Body $updateTicket -Description "Different user updates ticket (should fail)"
Test-Endpoint -Method "PUT" -Url "$baseUrl/api/tickets/1" -Token $supportToken -Body $updateTicket -Description "Support updates any ticket (should work)"

Write-Host "=== Step 6: Test deleting tickets (ADMIN only) ===" -ForegroundColor Yellow

Test-Endpoint -Method "DELETE" -Url "$baseUrl/api/tickets/3" -Token $userToken -Description "Regular user deletes ticket (should fail)"
Test-Endpoint -Method "DELETE" -Url "$baseUrl/api/tickets/3" -Token $supportToken -Description "Support deletes ticket (should fail)"
Test-Endpoint -Method "DELETE" -Url "$baseUrl/api/tickets/3" -Token $adminToken -Description "Admin deletes ticket (should work)"

Write-Host "=== Security Testing Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Mock Token Format: mock-{userType}-{userId}" -ForegroundColor Yellow
Write-Host "User Types: admin, support, user" -ForegroundColor Yellow
Write-Host "Examples:" -ForegroundColor Yellow
Write-Host "  - mock-admin-admin123 (ADMIN, SUPPORT, USER roles)" -ForegroundColor Gray
Write-Host "  - mock-support-support456 (SUPPORT, USER roles)" -ForegroundColor Gray
Write-Host "  - mock-user-user789 (USER role only)" -ForegroundColor Gray
