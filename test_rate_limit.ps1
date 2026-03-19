#!/usr/bin/env pwsh

# Test script to verify rate limiting is working
$baseUrl = "http://localhost:8080/v1/flowguard"
$results = @()

Write-Host "Starting rate limit test..."
Write-Host "Limit configured: 10 requests per minute"
Write-Host "Sending 15 requests in rapid succession..."
Write-Host ""

# Make 15 requests in rapid succession
for ($i = 1; $i -le 15; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $baseUrl -UseBasicParsing -TimeoutSec 2
        $statusCode = $response.StatusCode
        $results += @{ 
            Request = $i
            Status = $statusCode
            Success = $true
        }
        Write-Host "Request $i : HTTP $statusCode ✓"
    } catch {
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.Value__
            $results += @{ 
                Request = $i
                Status = $statusCode
                Success = $false
            }
            Write-Host "Request $i : HTTP $statusCode ✗"
        } else {
            Write-Host "Request $i : Error - $($_.Exception.Message)"
        }
    }
    Start-Sleep -Milliseconds 100
}

Write-Host ""
Write-Host "=== TEST RESULTS ==="
$successCount = ($results | Where-Object { $_.Status -eq 200 }).Count
$rateLimitedCount = ($results | Where-Object { $_.Status -eq 429 }).Count
$otherCount = ($results | Where-Object { $_.Status -ne 200 -and $_.Status -ne 429 }).Count

Write-Host "Successful (200):     $successCount"
Write-Host "Rate Limited (429):   $rateLimitedCount"
Write-Host "Other Errors:         $otherCount"
Write-Host ""

if ($rateLimitedCount -gt 0) {
    Write-Host "✓ RATE LIMITING IS WORKING - Requests were blocked after exceeding limit"
} else {
    Write-Host "✗ RATE LIMITING NOT WORKING - No 429 responses received"
}
