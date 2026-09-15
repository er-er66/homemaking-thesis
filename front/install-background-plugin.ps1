# Install HaoyueQin/deepseek-harness-background into the DSH "web" profile.
#
# Launched DETACHED by the agent because step 1 kills the process that hosts the
# agent session. This script must therefore be fully non-interactive and must
# survive its parent's death.
#
# WHY the host must stop: profiles/web/package.json declares "patchReload": "live".
# A running dsh web host watches the profile and holds handles inside node_modules,
# so Windows refuses pnpm's package-directory rename:
#   ERR_PNPM_EPERM rename '..._tmp_<pid>_1' -> '...'
#
# ASCII-only on purpose: Windows PowerShell 5.1 reads .ps1 as ANSI unless it has a
# BOM, which would corrupt any non-ASCII path literal.

$ErrorActionPreference = 'Continue'

$workspace  = 'D:\homemaking\front'
$profileDir = "C:\Users\卓\.dsh\profiles\web"
$pkg        = 'deepseek-harness-background'
$log        = Join-Path $workspace 'dsh-plugin-install.log'

Set-Content -Path $log -Value "=== install $pkg ===" -Encoding UTF8
function Log([string]$m) { Add-Content -Path $log -Value ('[{0:HH:mm:ss}] {1}' -f (Get-Date), $m) -Encoding UTF8 }

Log ("profile: " + $profileDir)

# ---------- 1. stop the dsh host(s) ----------
$pids = @()
try {
  $out = & wmic process where "name='node.exe'" get ProcessId,CommandLine /format:csv 2>$null
  foreach ($l in $out) {
    if ($l -match '_npx' -and $l -match 'dsh') {
      $tail = ($l -split ',')[-1].Trim()
      if ($tail -match '^\d+$') { $pids += [int]$tail }
    }
  }
  Log ("wmic matched dsh hosts: " + $(if ($pids.Count) { $pids -join ', ' } else { 'none' }))
} catch { Log ("wmic failed: " + $_.Exception.Message) }

if ($pids.Count -eq 0) {
  $pids = @(Get-Process node -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Id)
  Log ("FALLBACK: no host matched by command line; stopping all node PIDs: " + ($pids -join ', '))
}

foreach ($p in $pids) { Stop-Process -Id $p -Force -ErrorAction SilentlyContinue }
Log 'host stop requested; waiting 5s for file handles to release'
Start-Sleep -Seconds 5

# ---------- 2. clear leftover pnpm temp dirs from earlier failed attempts ----------
Get-ChildItem (Join-Path $profileDir 'node_modules') -Directory -Force -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like '*_tmp_*' } |
  ForEach-Object { Log ('removing leftover ' + $_.Name); Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue }

# ---------- 3. install ----------
$dshBin = Get-ChildItem -Path (Join-Path $env:LOCALAPPDATA 'npm-cache\_npx') -Directory -ErrorAction SilentlyContinue |
  ForEach-Object { Join-Path $_.FullName 'node_modules\@deepseek-ai\dsh\lib\bin.js' } |
  Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $dshBin) {
  Log 'ERROR: dsh CLI not found'
} else {
  Log ("dsh CLI: " + $dshBin)
  Push-Location $profileDir
  & node $dshBin plugin --profile web add $pkg *>&1 | Tee-Object -FilePath $log -Append
  $exit = $LASTEXITCODE
  Pop-Location
  Log ("dsh plugin exit=$exit")
}

# ---------- 4. verify ----------
$manifest = Get-Content (Join-Path $profileDir 'package.json') -Raw | ConvertFrom-Json
foreach ($d in $manifest.dependencies.PSObject.Properties) { Log ('dependency: ' + $d.Name + ' ' + $d.Value) }
Log ('bundles: ' + ($manifest.dsh.profile.bundles -join ', '))
$patchOk = Test-Path (Join-Path $profileDir ("node_modules\$pkg\cordis.patch.yml"))
$verOk   = Test-Path (Join-Path $profileDir ("node_modules\$pkg\package.json"))
Log ("plugin dir present: $patchOk (package.json: $verOk)")

$left = @(Get-ChildItem (Join-Path $profileDir 'node_modules') -Directory -Force -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like '*_tmp_*' } | Select-Object -ExpandProperty Name)
Log ('leftover tmp dirs: ' + $(if ($left.Count) { $left -join ', ' } else { 'none' }))

Log ('RESULT: ' + $(if ($patchOk) { 'SUCCESS - start dsh web again' } else { 'FAILED - see log above' }))
Log '=== end ==='
