$ErrorActionPreference = "Stop"

$baseUrl = if ($args.Count -gt 0) { $args[0] } else { "http://localhost:8080" }

Write-Host "Checking Iris API at $baseUrl"
Invoke-RestMethod "$baseUrl/api/health" | ConvertTo-Json

Write-Host "`nResetting demo state"
Invoke-RestMethod -Method Post "$baseUrl/api/demo/reset" | ConvertTo-Json

Write-Host "`nAuthorized command"
$authorized = @{
    transcript = "Hey Iris, can you turn on the lights?"
    faceId = "mike"
    confidence = 0.94
} | ConvertTo-Json
Invoke-RestMethod -Method Post "$baseUrl/api/interactions" -ContentType "application/json" -Body $authorized |
        Select-Object assistantResponse, authenticated, @{Name="lightStatus";Expression={$_.appliance.status}} |
        ConvertTo-Json

Write-Host "`nBlocked command"
$blocked = @{
    transcript = "Hey Iris, can you turn off the lights?"
    faceId = "jake"
    confidence = 0.91
} | ConvertTo-Json
Invoke-RestMethod -Method Post "$baseUrl/api/interactions" -ContentType "application/json" -Body $blocked |
        Select-Object assistantResponse, authenticated, @{Name="lightStatus";Expression={$_.appliance.status}} |
        ConvertTo-Json
