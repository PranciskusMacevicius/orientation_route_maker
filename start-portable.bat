@echo off
REM Orientation Route Maker - Portable Server (Windows)
REM No system dependencies required

echo 🌐 Starting Portable Orientation Route Maker Server...
echo.

REM Get the directory where this script is located
set "DIR=%~dp0"

REM Use the included simple server
if exist "%DIR%portable-server\simple-server.js" (
    echo ✅ Using portable HTTP server...
    echo 🚀 Server: http://127.0.0.1:3000
    echo 📱 Mobile: Use your computer's IP address
    echo ⏹️  Press Ctrl+C to stop
    echo.
    node "%DIR%portable-server\simple-server.js"
) else (
    echo ❌ Portable server not found!
    echo Please ensure "portable-server\simple-server.js" exists
    pause
    exit /b 1
)

pause
