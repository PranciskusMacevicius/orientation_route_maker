@echo off
echo ============================================================
echo    Orientation Route Maker - Web Application Launcher
echo ============================================================
echo.

:: Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH!
    echo Please install Python 3.6+ and try again.
    echo Download from: https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

:: Check if required files exist
if not exist "index.html" (
    echo ERROR: index.html not found!
    echo Please ensure all web application files are in the same directory.
    pause
    exit /b 1
)

if not exist "app.js" (
    echo ERROR: app.js not found!
    echo Please ensure all web application files are in the same directory.
    pause
    exit /b 1
)

echo Starting web server...
echo The application will open in your default browser.
echo.
echo To stop the server, close this window or press Ctrl+C
echo.

:: Start the Python server
python server.py

pause
