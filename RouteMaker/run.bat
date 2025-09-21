@echo off
setlocal enabledelayedexpansion

:: Change to the directory where this script is located
cd /d "%~dp0"

echo RouteMaker Portable
echo ===================

:: Check for command line argument to force architecture
if "%1"=="32" goto force32
if "%1"=="64" goto force64
if "%1"=="-32" goto force32
if "%1"=="-64" goto force64
if "%1"=="x86" goto force32
if "%1"=="x64" goto force64

:: Auto-detect system architecture
set ARCH=%PROCESSOR_ARCHITECTURE%
if "%ARCH%"=="AMD64" (
    set JRE_DIR=jre
    echo Detected 64-bit system
    goto checkjre
) else if "%ARCH%"=="x86" (
    set JRE_DIR=jre-x86
    echo Detected 32-bit system
    goto checkjre
) else (
    echo Unknown architecture: %ARCH%
    goto showmenu
)

:force32
set JRE_DIR=jre-x86
echo Using 32-bit JRE
goto checkjre

:force64
set JRE_DIR=jre
echo Using 64-bit JRE
goto checkjre

:showmenu
echo.
echo Could not auto-detect system architecture.
echo Please choose:
echo   1. Use 64-bit JRE
echo   2. Use 32-bit JRE
echo   3. Exit
echo.
set /p CHOICE="Enter choice (1-3): "

if "%CHOICE%"=="1" (
    set JRE_DIR=jre
) else if "%CHOICE%"=="2" (
    set JRE_DIR=jre-x86
) else if "%CHOICE%"=="3" (
    exit /b 0
) else (
    echo Invalid choice. Using 64-bit JRE.
    set JRE_DIR=jre
)

:checkjre
:: Check if JRE directory exists
if not exist "%JRE_DIR%" (
    echo.
    echo ERROR: JRE directory "%JRE_DIR%" not found!
    if "%JRE_DIR%"=="jre-x86" (
        echo This portable app currently only includes 64-bit JRE.
        echo To run on 32-bit systems, you need to add a 32-bit JRE to the "jre-x86" folder.
        echo.
        echo Would you like to try 64-bit JRE instead? (y/n^)
        set /p SWITCH="Enter choice: "
        if /i "!SWITCH!"=="y" (
            set JRE_DIR=jre
            goto checkjre
        )
    )
    pause
    exit /b 1
)

:: Check if Java executable exists
if not exist "%JRE_DIR%\bin\java.exe" (
    echo ERROR: Java executable not found in "%JRE_DIR%\bin\java.exe"
    pause
    exit /b 1
)

:: Check if JAR file exists
if not exist "RouteMaker.jar" (
    echo ERROR: RouteMaker.jar not found!
    pause
    exit /b 1
)

:: Run the application
echo.
echo Starting RouteMaker...
echo.
if exist "javafx\lib" (
    echo Using JavaFX for Google Maps integration...
    "%JRE_DIR%\bin\java.exe" --module-path javafx\lib --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "RouteMaker.jar;pdfbox.jar" RouteMaker
) else (
    echo Warning: JavaFX not found. Google Maps will not work.
    "%JRE_DIR%\bin\java.exe" -cp "RouteMaker.jar;pdfbox.jar" RouteMaker
)

echo.
echo Application finished.
pause
