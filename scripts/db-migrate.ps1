<#
.SYNOPSIS
  QBounce database migration helper script.

.DESCRIPTION
  This script manages the developer database migration workflow.

  diff   → Auto-detect entity changes and generate a .mysql.sql migration file.
  apply  → Apply all pending Liquibase changesets to the local DB,
           then regenerate jOOQ type-safe models.
  status → Show which changesets have not yet been applied.

.EXAMPLE
  .\scripts\db-migrate.ps1 diff
  .\scripts\db-migrate.ps1 apply
  .\scripts\db-migrate.ps1 status

.NOTES
  MySQL must be running on localhost:3306 before using either command.
#>

param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("diff", "apply", "status")]
    [string]$Command
)

$ErrorActionPreference = "Stop"

# Navigate to the project root (one level up from /scripts)
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $ProjectRoot

# Use .\mvnw (Maven wrapper) so no global Maven install is required
$Mvn = ".\mvnw.cmd"
if (-not (Test-Path $Mvn)) {
    # Fallback to system mvn if wrapper not found
    $Mvn = "mvn"
}

function Invoke-Maven {
    param([string[]]$Goals)
    $goalStr = $Goals -join " "
    Write-Host ""
    Write-Host ">> $Mvn $goalStr" -ForegroundColor Cyan
    Write-Host ""
    & $Mvn @Goals
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] $Mvn $goalStr failed (exit code $LASTEXITCODE)" -ForegroundColor Red
        Pop-Location
        exit $LASTEXITCODE
    }
}

switch ($Command) {

    "diff" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "  QBounce DB Migrate - Detecting Entity Changes" -ForegroundColor Cyan
        Write-Host "============================================================" -ForegroundColor Cyan

        Invoke-Maven @("process-classes")
        
        Write-Host ""
        Write-Host "Check src/main/resources/db/changelog/changes/ for new files." -ForegroundColor Cyan
    }

    "apply" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Green
        Write-Host "  QBounce DB Migrate - Applying pending changesets" -ForegroundColor Green
        Write-Host "============================================================" -ForegroundColor Green

        Write-Host ""
        Write-Host "Step 1/2  Applying Liquibase changesets..." -ForegroundColor Yellow
        Invoke-Maven @("liquibase:update")

        Write-Host ""
        Write-Host "Step 2/2  Regenerating jOOQ models from updated schema..." -ForegroundColor Yellow
        Invoke-Maven @("jooq-codegen:generate")

        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Green
        Write-Host "  Done! DB updated + jOOQ models regenerated." -ForegroundColor Green
        Write-Host "  Commit your entity, migration SQL file, and jOOQ models." -ForegroundColor Green
        Write-Host "============================================================" -ForegroundColor Green
    }

    "status" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Yellow
        Write-Host "  QBounce DB Migrate - Pending changesets" -ForegroundColor Yellow
        Write-Host "============================================================" -ForegroundColor Yellow

        Invoke-Maven @("liquibase:status")
    }
}

Pop-Location
