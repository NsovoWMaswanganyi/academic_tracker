param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to your JDK 25 folder before building.' }
$jpackage = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
if (-not (Test-Path -LiteralPath $jpackage)) { throw 'jpackage.exe was not found in JAVA_HOME.' }
Push-Location $root
try {
    if (-not $SkipBuild) {
        & .\mvnw.cmd clean verify
        if ($LASTEXITCODE -ne 0) { throw 'Maven build or tests failed.' }
    }
    $jar = Join-Path $root 'target\academic_tracker_app-1.0-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw 'Build the project first.' }
    $buildRoot = Join-Path $root ('target\windows\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    $inputFolder = Join-Path $buildRoot 'input'
    New-Item -ItemType Directory -Force $inputFolder | Out-Null
    Copy-Item -LiteralPath $jar -Destination $inputFolder
    & $jpackage --type app-image --name 'Academic Tracker' --app-version 1.1.0 `
        --vendor 'Academic Tracker' --description 'Track module marks and your weighted average' `
        --input $inputFolder --dest $buildRoot `
        --main-jar 'academic_tracker_app-1.0-SNAPSHOT.jar' `
        --main-class 'com.academic_tracker_app.Launcher' `
        --icon (Join-Path $root 'src\main\resources\icons\academic-tracker.ico') `
        --add-modules 'java.base,java.desktop,java.sql,java.sql.rowset,java.scripting,java.logging,java.naming,jdk.unsupported' `
        --java-options '--enable-native-access=ALL-UNNAMED'
    if ($LASTEXITCODE -ne 0) { throw 'Windows packaging failed.' }
    $image = Join-Path $buildRoot 'Academic Tracker'
    Set-Content -LiteralPath (Join-Path $root 'target\windows\latest-path.txt') -Value $image
    Write-Host "Built: $image"
    Write-Host 'Run Install-Desktop.cmd to install this build and create your desktop shortcut.'
} finally { Pop-Location }
