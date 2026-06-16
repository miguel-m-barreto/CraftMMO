Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Confirmation = Read-Host "This will delete local CraftMMO Docker volumes. Type RESET to continue"
if ($Confirmation -ne "RESET") {
    Write-Output "Reset cancelled."
    exit 0
}

function Invoke-Checked {
    param([scriptblock] $Command)
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code $LASTEXITCODE"
    }
}

Push-Location $Root
try {
    Invoke-Checked { docker compose down -v }
    Invoke-Checked { docker compose up -d postgres }
    Invoke-Checked { docker compose ps }
} finally {
    Pop-Location
}
