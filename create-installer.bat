@echo off
echo Creating RouteMaker installer with jpackage...
echo.

:: Check if Java 17+ is available
java -version 2>&1 | findstr "17\|18\|19\|20\|21" >nul
if errorlevel 1 (
    echo ERROR: Java 17+ required for jpackage!
    echo Current Java version:
    java -version
    pause
    exit /b 1
)

:: Compile the application
echo Compiling application...
javac --module-path "RouteMaker\javafx\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "pdfbox.jar" *.java
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

:: Create JAR file
echo Creating JAR file...
jar cfe RouteMaker.jar RouteMaker *.class
if errorlevel 1 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)

:: Create installer with jpackage
echo Creating Windows installer...
jpackage ^
    --input . ^
    --name "RouteMaker" ^
    --main-jar RouteMaker.jar ^
    --main-class RouteMaker ^
    --module-path "RouteMaker\javafx\lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing ^
    --runtime-image "RouteMaker\jre" ^
    --type msi ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --icon "icon.ico" ^
    --app-version "1.0.0" ^
    --description "Orientation Route Maker Application" ^
    --vendor "Your Name" ^
    --dest "dist"

if errorlevel 1 (
    echo ERROR: jpackage failed!
    pause
    exit /b 1
)

echo.
echo ✅ Installer created successfully!
echo    Check the 'dist' folder for RouteMaker.msi
echo.
pause

