[CmdletBinding()]
param(
    [switch]$CheckOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$dockerDirectory = $PSScriptRoot
$environmentDirectory = Join-Path $dockerDirectory 'environment'
$composeFile = Join-Path $dockerDirectory 'docker-compose.yaml'
$forumHome = [System.IO.Path]::GetFullPath((Join-Path (Get-Location).Path 'volumes'))
$env:FORUM_HOME = $forumHome

$requiredEnvFiles = [ordered]@{
    'mysql.env' = @(
        'MYSQL_ROOT_PASSWORD',
        'MYSQL_DATABASE',
        'MYSQL_USER',
        'MYSQL_PASSWORD'
    )
    'redis.env' = @(
        'REDIS_USERNAME',
        'REDIS_PASSWORD'
    )
    'minio.env' = @(
        'MINIO_ROOT_USER',
        'MINIO_ROOT_PASSWORD'
    )
    'rabbitmq.env' = @(
        'RABBITMQ_DEFAULT_USER',
        'RABBITMQ_DEFAULT_PASS',
        'RABBITMQ_DEFAULT_VHOST'
    )
    'elasticsearch.env' = @(
        'ELASTIC_PASSWORD',
        'ELASTICSEARCH_APP_USERNAME',
        'ELASTICSEARCH_APP_PASSWORD',
        'ELASTICSEARCH_APP_ROLE'
    )
}

function Read-EnvFile {
    param([Parameter(Mandatory)][string]$Path)

    $values = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') {
            $values[$matches[1]] = $matches[2]
        }
    }
    return $values
}

function Test-EnvironmentFiles {
    $problems = [System.Collections.Generic.List[string]]::new()

    foreach ($entry in $requiredEnvFiles.GetEnumerator()) {
        $path = Join-Path $environmentDirectory $entry.Key
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            $problems.Add("缺少环境变量文件：$path（请从 $($entry.Key).example 复制）")
            continue
        }

        $values = Read-EnvFile $path
        foreach ($field in $entry.Value) {
            $value = if ($values.ContainsKey($field)) { [string]$values[$field] } else { $null }
            if ([string]::IsNullOrWhiteSpace($value) -or $value.TrimStart().StartsWith('CHANGE_ME')) {
                $problems.Add("$($entry.Key) 缺少或未配置字段：$field")
            }
        }
    }

    if ($problems.Count -gt 0) {
        $details = $problems | ForEach-Object { "- $_" }
        throw "环境变量检查失败：`n$($details -join "`n")"
    }
}

Test-EnvironmentFiles
Write-Host '环境变量检查通过；未修改任何 env 文件。'

if ($CheckOnly) {
    exit 0
}

New-Item -ItemType Directory -Force -Path $forumHome | Out-Null
Write-Host "Docker 持久化数据目录：$forumHome"

& docker compose -f $composeFile up -d mysql redis minio rabbitmq elasticsearch elasticsearch-init
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host 'MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 已启动。'
