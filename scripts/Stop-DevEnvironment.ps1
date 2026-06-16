Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

Push-Location $Root
try {
    docker compose down
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose down failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}
