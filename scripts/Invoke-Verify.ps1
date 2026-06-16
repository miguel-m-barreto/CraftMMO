Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock] $Command
    )
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code $LASTEXITCODE"
    }
}

Push-Location $Root
try {
    Invoke-Checked { & .\mvnw.cmd -B -ntp clean verify }
    Invoke-Checked { docker compose config --services }
    Invoke-Checked { docker compose config --profiles }
    Invoke-Checked { git status --short --branch }
} finally {
    Pop-Location
}
