$ErrorActionPreference = 'Stop'

$composeFile = Join-Path $PSScriptRoot 'docker-compose.yaml'
$composeText = Get-Content -Raw $composeFile

docker compose -f $composeFile config --quiet
if ($LASTEXITCODE -ne 0) {
    throw 'docker compose config failed'
}

if ($composeText -match '(?im)^\s*image:\s+\S*:(latest|management)\s*$') {
    throw 'floating infrastructure image tag found'
}

if ($composeText -match '(?m)^\s*-\s*"(?!127\.0\.0\.1:)[^"]+"\s*$') {
    throw 'published port is not loopback-bound'
}

if ($composeText -match '(?m)^\s*-\s*"[^"]*:9300:[^"]+"\s*$') {
    throw 'Elasticsearch transport port is published'
}

foreach ($required in @('credentials-preflight:', 'kibana-credentials-preflight:', 'condition: service_healthy')) {
    if ($composeText.IndexOf($required, [StringComparison]::Ordinal) -lt 0) {
        throw "required Compose security contract missing: $required"
    }
}

Write-Output 'compose-security-static-check=pass'
