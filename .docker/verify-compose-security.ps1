$ErrorActionPreference = 'Stop'

$composeFile = Join-Path $PSScriptRoot 'docker-compose.yaml'
$composeText = Get-Content -Raw $composeFile

$originalForumHome = $env:FORUM_HOME
try {
    if ([string]::IsNullOrWhiteSpace($env:FORUM_HOME)) {
        $env:FORUM_HOME = '/tmp/nine-forum-compose-check'
    }
    docker compose -f $composeFile config --quiet
    if ($LASTEXITCODE -ne 0) {
        throw 'docker compose config failed'
    }
} finally {
    if ($null -eq $originalForumHome) {
        Remove-Item Env:FORUM_HOME -ErrorAction SilentlyContinue
    } else {
        $env:FORUM_HOME = $originalForumHome
    }
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

if ($composeText.Contains('/docker_volumes/nine_forum')) {
    throw 'hard-coded forum data root found'
}

foreach ($requiredVolume in @(
    '/mysql',
    '/redis',
    '/minio/data',
    '/minio/config',
    '/rabbitmq',
    '/elastic/data',
    '/elastic/plugins'
)) {
    $source = '${FORUM_HOME:?Set FORUM_HOME in .docker/.env}' + $requiredVolume
    if ($composeText.IndexOf($source, [StringComparison]::Ordinal) -lt 0) {
        throw "FORUM_HOME volume missing: $requiredVolume"
    }
}

$bashStartText = Get-Content -Raw (Join-Path $PSScriptRoot 'start_bash.sh')
$powerShellStartText = Get-Content -Raw (Join-Path $PSScriptRoot 'start_pwsh.ps1')
$servicePattern = '(?m)^[^\r\n]*docker compose[^\r\n]*\sup -d (?<services>[a-z0-9 -]+)\s*$'
$bashServices = [regex]::Match($bashStartText, $servicePattern).Groups['services'].Value.Trim()
$powerShellServices = [regex]::Match($powerShellStartText, $servicePattern).Groups['services'].Value.Trim()
if ([string]::IsNullOrWhiteSpace($bashServices) -or
    [string]::IsNullOrWhiteSpace($powerShellServices) -or
    $bashServices -ne $powerShellServices) {
    throw "start script service lists differ: sh=[$bashServices], ps1=[$powerShellServices]"
}

Write-Output 'compose-security-static-check=pass'
