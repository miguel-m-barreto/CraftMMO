# Windows Development

Run commands from the repository root:

```powershell
.\scripts\Start-DevEnvironment.ps1
.\mvnw.cmd -B -ntp clean verify
.\scripts\Stop-DevEnvironment.ps1
```

Resetting local Docker volumes is destructive and requires typing `RESET`.
