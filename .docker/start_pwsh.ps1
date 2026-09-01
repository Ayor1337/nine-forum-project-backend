[CmdletBinding()]
param(
    [switch]$PrepareOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$dockerDirectory = $PSScriptRoot
$projectDirectory = Split-Path -Parent $dockerDirectory
$environmentDirectory = Join-Path $dockerDirectory 'environment'
$applicationConfig = Join-Path $projectDirectory 'web/web-app/src/main/resources/application.yml'
$composeFile = Join-Path $dockerDirectory 'docker-compose.yaml'
$forumHome = [System.IO.Path]::GetFullPath((Join-Path (Get-Location).Path 'volumes'))
$env:FORUM_HOME = $forumHome

function Read-EnvFile {
    param([Parameter(Mandatory)][string]$Path)

    $values = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $values
    }

    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') {
            $values[$matches[1]] = $matches[2]
        }
    }
    return $values
}

function Get-UsableValue {
    param(
        [hashtable]$Values,
        [Parameter(Mandatory)][string]$Name
    )

    if (-not $Values.ContainsKey($Name)) {
        return $null
    }
    $value = [string]$Values[$Name]
    if ([string]::IsNullOrWhiteSpace($value) -or $value.StartsWith('CHANGE_ME')) {
        return $null
    }
    return $value
}

function Get-YamlScalar {
    param(
        [Parameter(Mandatory)][string]$Path,
        [Parameter(Mandatory)][string[]]$KeyPath
    )

    $parents = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -notmatch '^(\s*)([A-Za-z][A-Za-z0-9.-]*):(?:\s*(.*))?$') {
            continue
        }

        $indent = $matches[1].Length
        if (($indent % 2) -ne 0) {
            continue
        }
        $level = [int]($indent / 2)
        foreach ($existingLevel in @($parents.Keys)) {
            if ([int]$existingLevel -ge $level) {
                $parents.Remove($existingLevel)
            }
        }

        $key = $matches[2]
        $rawValue = $matches[3]
        if ([string]::IsNullOrWhiteSpace($rawValue)) {
            $parents[$level] = $key
            continue
        }

        $actualPath = @()
        for ($index = 0; $index -lt $level; $index++) {
            if (-not $parents.ContainsKey($index)) {
                $actualPath = @()
                break
            }
            $actualPath += [string]$parents[$index]
        }
        $actualPath += $key
        if (($actualPath -join '.') -ne ($KeyPath -join '.')) {
            continue
        }

        $value = $rawValue.Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        return $value
    }

    return $null
}

function New-LocalSecret {
    return [Convert]::ToBase64String(
        [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
    )
}

function Require-Value {
    param(
        [AllowNull()][string]$Value,
        [Parameter(Mandatory)][string]$Description
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "无法从旧配置读取 $Description；请先检查 $applicationConfig"
    }
    return $Value
}

function Write-EnvFile {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][System.Collections.IDictionary]$Values
    )

    $path = Join-Path $environmentDirectory $Name
    $lines = foreach ($entry in $Values.GetEnumerator()) {
        '{0}={1}' -f $entry.Key, $entry.Value
    }
    Set-Content -LiteralPath $path -Value $lines -Encoding utf8
}

if (-not (Test-Path -LiteralPath $applicationConfig)) {
    throw "缺少旧的本地配置：$applicationConfig"
}

$mysqlEnv = Read-EnvFile (Join-Path $environmentDirectory 'mysql.env')
$redisEnv = Read-EnvFile (Join-Path $environmentDirectory 'redis.env')
$elasticsearchEnv = Read-EnvFile (Join-Path $environmentDirectory 'elasticsearch.env')

$oldDatabasePassword = Require-Value `
    (Get-YamlScalar $applicationConfig @('spring', 'datasource', 'password')) `
    'spring.datasource.password'
$mysqlRootPassword = Get-UsableValue $mysqlEnv 'MYSQL_ROOT_PASSWORD'
if ($null -eq $mysqlRootPassword) {
    $mysqlRootPassword = $oldDatabasePassword
}

$mysqlDatabase = Get-UsableValue $mysqlEnv 'MYSQL_DATABASE'
if ($null -eq $mysqlDatabase) {
    $mysqlDatabase = 'nine_forum'
}

Write-EnvFile 'mysql.env' ([ordered]@{
    MYSQL_ROOT_PASSWORD = $mysqlRootPassword
    MYSQL_DATABASE      = $mysqlDatabase
    MYSQL_USER          = 'nine_forum_app'
    MYSQL_PASSWORD      = $oldDatabasePassword
})

$redisUsername = Get-UsableValue $redisEnv 'REDIS_USERNAME'
if ($null -eq $redisUsername) {
    $redisUsername = 'nineforum-app'
}
$redisPassword = Get-UsableValue $redisEnv 'REDIS_PASSWORD'
if ($null -eq $redisPassword) {
    $redisPassword = New-LocalSecret
}
Write-EnvFile 'redis.env' ([ordered]@{
    REDIS_USERNAME = $redisUsername
    REDIS_PASSWORD = $redisPassword
})

Write-EnvFile 'minio.env' ([ordered]@{
    MINIO_ROOT_USER     = Require-Value (Get-YamlScalar $applicationConfig @('spring', 'minio', 'access-key')) 'spring.minio.access-key'
    MINIO_ROOT_PASSWORD = Require-Value (Get-YamlScalar $applicationConfig @('spring', 'minio', 'secret-key')) 'spring.minio.secret-key'
})

Write-EnvFile 'rabbitmq.env' ([ordered]@{
    RABBITMQ_DEFAULT_USER  = Require-Value (Get-YamlScalar $applicationConfig @('spring', 'rabbitmq', 'username')) 'spring.rabbitmq.username'
    RABBITMQ_DEFAULT_PASS  = Require-Value (Get-YamlScalar $applicationConfig @('spring', 'rabbitmq', 'password')) 'spring.rabbitmq.password'
    RABBITMQ_DEFAULT_VHOST = Require-Value (Get-YamlScalar $applicationConfig @('spring', 'rabbitmq', 'virtual-host')) 'spring.rabbitmq.virtual-host'
})

$elasticPassword = Get-UsableValue $elasticsearchEnv 'ELASTIC_PASSWORD'
if ($null -eq $elasticPassword) {
    $elasticPassword = New-LocalSecret
}
$elasticsearchAppPassword = Get-UsableValue $elasticsearchEnv 'ELASTICSEARCH_APP_PASSWORD'
if ($null -eq $elasticsearchAppPassword) {
    $elasticsearchAppPassword = New-LocalSecret
}
Write-EnvFile 'elasticsearch.env' ([ordered]@{
    ELASTIC_PASSWORD              = $elasticPassword
    ELASTICSEARCH_APP_USERNAME    = 'nineforum_app'
    ELASTICSEARCH_APP_PASSWORD    = $elasticsearchAppPassword
    ELASTICSEARCH_APP_ROLE        = 'nineforum_app'
})

Write-Host '本地凭据文件已准备完成；旧 MySQL、MinIO、RabbitMQ 凭据已复用，新增认证密码已随机生成。'
Write-Host '凭据值只保存在被 Git 忽略的 .docker/environment/*.env 中。'

if ($PrepareOnly) {
    exit 0
}

New-Item -ItemType Directory -Force -Path $forumHome | Out-Null
Write-Host "Docker 持久化数据目录：$forumHome"

& docker compose -f $composeFile up -d mysql redis minio rabbitmq elasticsearch elasticsearch-init
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host 'MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 已启动。'
