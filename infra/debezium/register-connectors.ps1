$ErrorActionPreference = "Stop"

$debeziumUrl = "http://localhost:8083"
$connectorsDir = Join-Path $PSScriptRoot "connectors"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Debezium Connectors Registration Script (PowerShell)     " -ForegroundColor Cyan
Write-Host " Target URL: $debeziumUrl                                 " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Resolve POSTGRES_PASSWORD from env or infra/.env
$pgPass = $env:POSTGRES_PASSWORD
if (-not $pgPass) {
    $envPath = Join-Path $PSScriptRoot "..\.env"
    if (Test-Path $envPath) {
        $envLines = Get-Content $envPath
        foreach ($line in $envLines) {
            if ($line -match "^\s*POSTGRES_PASSWORD\s*=\s*(.*)$") {
                $pgPass = $matches[1].Trim()
                break
            }
        }
    }
}

if (-not $pgPass) {
    Write-Host "UYARI: POSTGRES_PASSWORD bulunamadi, varsayilan 'postgres' kullanilacak." -ForegroundColor Yellow
    $pgPass = "postgres"
}

# Wait for Debezium to be healthy
Write-Host "Debezium Connect REST API bekleniyor..." -ForegroundColor Yellow
$maxRetries = 20
$retryCount = 0

while ($retryCount -lt $maxRetries) {
    try {
        $res = Invoke-RestMethod -Uri "$debeziumUrl/connectors" -Method Get -TimeoutSec 3 -ErrorAction SilentlyContinue
        Write-Host "Debezium Connect hazır!" -ForegroundColor Green
        break
    }
    catch {
        $retryCount++
        Write-Host "Debezium henüz hazır değil ($retryCount/$maxRetries)... 3 sn sonra tekrar deneniyor." -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}

if ($retryCount -ge $maxRetries) {
    Write-Error "Debezium Connect $debeziumUrl adresinde yanıt vermedi. Lütfen container'ın çalıştığından emin olun."
    exit 1
}

Get-ChildItem -Path $connectorsDir -Filter "*.json" | ForEach-Object {
    $filePath = $_.FullName
    $rawContent = Get-Content -Path $filePath -Raw
    $substituted = $rawContent.Replace('${POSTGRES_PASSWORD}', $pgPass)
    $connectorObj = $substituted | ConvertFrom-Json
    $connectorName = $connectorObj.name

    Write-Host "`nConnector işleniyor: $connectorName" -ForegroundColor Cyan

    try {
        # Check if already exists
        $existing = Invoke-RestMethod -Uri "$debeziumUrl/connectors/$connectorName" -Method Get -ErrorAction SilentlyContinue
        if ($existing) {
            Write-Host "Connector '$connectorName' zaten kayıtlı, konfigürasyon güncelleniyor..." -ForegroundColor Yellow
            $configJson = $connectorObj.config | ConvertTo-Json -Depth 10
            Invoke-RestMethod -Uri "$debeziumUrl/connectors/$connectorName/config" -Method Put -ContentType "application/json" -Body $configJson | Out-Null
            Write-Host "Connector '$connectorName' başarıyla güncellendi!" -ForegroundColor Green
        }
    }
    catch {
        # Does not exist, create it
        Write-Host "Yeni connector '$connectorName' oluşturuluyor..." -ForegroundColor Yellow
        try {
            Invoke-RestMethod -Uri "$debeziumUrl/connectors" -Method Post -ContentType "application/json" -Body $substituted | Out-Null
            Write-Host "Connector '$connectorName' başarıyla oluşturuldu!" -ForegroundColor Green
        }
        catch {
            Write-Host "Hata oluştu ($connectorName): $_" -ForegroundColor Red
        }
    }
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " Tüm connector'lar başarıyla tamamlandı!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
