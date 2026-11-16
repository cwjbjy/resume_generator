$ErrorActionPreference = 'Stop'
$envFile = Join-Path $PSScriptRoot '.env'
if (Test-Path -LiteralPath $envFile) { Write-Host '.env already exists; nothing changed.'; exit 0 }
function New-LocalSecret {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return [Convert]::ToBase64String($bytes)
}
$lines = @(
    'APP_PROFILE=dev'
    'DB_PORT=3306'
    'REDIS_PORT=6379'
    "DB_PASSWORD=$(New-LocalSecret)"
    "MYSQL_ROOT_PASSWORD=$(New-LocalSecret)"
    "REDIS_PASSWORD=$(New-LocalSecret)"
    "JWT_SECRET=$(New-LocalSecret)"
    "PAYMENT_SECRET=$(New-LocalSecret)"
    'DEEPSEEK_API_KEY='
    'DEEPSEEK_MODEL=deepseek-flash'
)
$lines | Set-Content -LiteralPath $envFile -Encoding ascii
Write-Host 'Created .env with random local secrets.'
Write-Host 'Start dev databases: docker compose -f compose.yml -f compose.dev.yml up -d --wait mysql redis'
