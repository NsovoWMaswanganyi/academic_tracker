@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\Install-Desktop.ps1"
if errorlevel 1 (
    echo Installation did not finish. See the error above.
    pause
    exit /b 1
)
echo Academic Tracker is ready. Open it using the shortcut on your desktop.
pause
