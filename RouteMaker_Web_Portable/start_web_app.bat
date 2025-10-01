@echo off
echo ============================================================
echo    Orientation Route Maker - Portable Web Version
echo ============================================================
echo.
echo Starting portable web server...
echo The application will open in your default browser.
echo.
echo To stop the server, close this window or press Ctrl+C
echo.

:: Use embedded Python if available, otherwise try system Python
if exist "python\python.exe" (
    echo Using embedded Python...
    "python\python.exe" "web\server.py"
) else (
    echo Trying system Python...
    python "web\server.py"
    if errorlevel 1 (
        echo.
        echo ERROR: Python not found!
        echo Please install Python 3.6+ from https://www.python.org/downloads/
        echo Or use the standalone HTML version instead.
        echo.
        pause
    )
)
