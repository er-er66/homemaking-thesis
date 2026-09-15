# Recover: install dsh plugins into the DSH "web" profile again.
#
# WHY THIS FILE EXISTS
# Two independent problems blocked the plugin install from the market UI:
#   1. (FIXED ALREADY) profiles/web/node_modules/.modules.yaml pointed at a
#      temporary pnpm store (D:\homemaking\front\.dsh-market-tmp\pnpm-store\v10)
#      that no longer exists -> "ERR_PNPM_UNEXPECTED_STORE: Unexpected store location".
#      The profile is now relinked to pnpm's normal store.
#   2. (THIS SCRIPT) The running `dsh web` host watches the live profile
#      (package.json has "patchReload": "live") and holds handles inside
#      node_modules, so pnpm cannot rename any package directory:
#      "ERR_PNPM_EPERM ... rename '..._tmp_..._1' -> '...'".
#      => Every plugin install MUST run while dsh is stopped.
#
# ORDER MATTERS: dsh must be stopped BEFORE pnpm runs.

$ErrorActionPreference = 'Continue'
$profileDir = Join-Path $env:DSH_HOME 'profiles\web'

Write-Host '=== 1. stop the running dsh web host ===' -ForegroundColor Cyan
$hosts = Get-Process node -ErrorAction SilentlyContinue | Where-Object {
  $_.StartTime -lt (Get-Date).AddMinutes(-1)
}
if (-not $hosts) {
  Write-Host 'no node process found - is dsh web already stopped?' -ForegroundColor Yellow
} else {
  $hosts | Select-Object Id, StartTime | Format-Table -AutoSize
  $hosts | Stop-Process -Force
  Write-Host 'stopped. waiting for file handles to be released...' -ForegroundColor Green
  Start-Sleep -Seconds 3
}

Write-Host '=== 2. rebuild the profile cleanly (also drops leftover *_tmp_* dirs) ===' -ForegroundColor Cyan
Push-Location $profileDir
pnpm install --force
$installExit = $LASTEXITCODE
Pop-Location
Write-Host "pnpm install exit=$installExit" -ForegroundColor Cyan

Write-Host '=== 3. remove any leftover pnpm temp directories ===' -ForegroundColor Cyan
Get-ChildItem (Join-Path $profileDir 'node_modules') -Directory -Force -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like '*_tmp_*' } |
  ForEach-Object {
    Write-Host "  removing $($_.Name)"
    Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
  }

Write-Host '=== 4. install the plugin ===' -ForegroundColor Cyan
$dshBin = Get-ChildItem -Path (Join-Path $env:LOCALAPPDATA 'npm-cache\_npx') -Directory -ErrorAction SilentlyContinue |
  ForEach-Object { Join-Path $_.FullName 'node_modules\@deepseek-ai\dsh\lib\bin.js' } |
  Where-Object { Test-Path $_ } | Select-Object -First 1
if ($dshBin) {
  node $dshBin plugin --profile web add deepseek-harness-background
} else {
  Write-Host 'dsh CLI not found; run instead: dsh plugin --profile web add deepseek-harness-background' -ForegroundColor Yellow
}

Write-Host '=== 5. result ===' -ForegroundColor Cyan
Get-Content (Join-Path $profileDir 'package.json') -Raw
$mods = Get-ChildItem (Join-Path $profileDir 'node_modules') -Directory -Force -Name
Write-Host "leftover temp dirs: $((($mods | Where-Object { $_ -like '*_tmp_*' }) -join ', '))"

Write-Host ''
Write-Host 'Now start dsh again:  dsh web   (then open Settings -> Plugin Market)' -ForegroundColor Green
