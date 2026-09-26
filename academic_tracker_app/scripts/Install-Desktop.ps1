param([string]$ImagePath)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
if (-not $ImagePath) {
    $latest = Join-Path $root 'target\windows\latest-path.txt'
    if (-not (Test-Path -LiteralPath $latest)) { throw 'Run scripts\Build-Windows.ps1 first.' }
    $ImagePath = (Get-Content -LiteralPath $latest -Raw).Trim()
}
$sourceExe = Join-Path $ImagePath 'Academic Tracker.exe'
if (-not (Test-Path -LiteralPath $sourceExe)) { throw "App image is missing: $sourceExe" }
# Versioned program files keep previous installs intact; grades live outside this directory.
$installRoot = Join-Path $env:LOCALAPPDATA 'Programs\Academic Tracker'
$destination = Join-Path $installRoot (Get-Date -Format 'yyyyMMdd-HHmmss')
New-Item -ItemType Directory -Force $destination | Out-Null
Copy-Item -Path (Join-Path $ImagePath '*') -Destination $destination -Recurse
$exe = Join-Path $destination 'Academic Tracker.exe'
$legacy = Join-Path $root 'grades.db'
$initialize = Start-Process -FilePath $exe -ArgumentList @('--initialize-data', ('"' + $legacy + '"')) `
    -WorkingDirectory $destination -WindowStyle Hidden -PassThru -Wait
if ($initialize.ExitCode -ne 0) { throw 'Grades could not be initialized. The original database has been preserved.' }
$desktop = [Environment]::GetFolderPath('Desktop')
$startMenu = [Environment]::GetFolderPath('Programs')
$shell = New-Object -ComObject WScript.Shell
foreach ($folder in @($desktop, $startMenu)) {
    $shortcut = $shell.CreateShortcut((Join-Path $folder 'Academic Tracker.lnk'))
    $shortcut.TargetPath = $exe
    $shortcut.WorkingDirectory = $destination
    $shortcut.IconLocation = "$exe,0"
    $shortcut.Description = 'Track grades and weighted averages'
    $shortcut.Save()
}
Write-Host "Installed: $exe"
Write-Host "Desktop shortcut: $(Join-Path $desktop 'Academic Tracker.lnk')"
Write-Host "Grades: $(Join-Path $env:LOCALAPPDATA 'Academic Tracker\grades.db')"
