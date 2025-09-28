@echo off
echo Creating RouteMaker portable app with jpackage...
echo.

:: Check if Java is available and show version
echo Checking Java version...
java -version
if errorlevel 1 (
    echo ERROR: Java not found! Please install Java 17+ or use the portable JRE.
    echo Trying to use portable JRE from RouteMaker directory...
    if not exist "RouteMaker\jre\bin\java.exe" (
        echo ERROR: No Java found! Please install Java 17+ or ensure RouteMaker\jre exists.
        pause
        exit /b 1
    )
    set JAVA_CMD=RouteMaker\jre\bin\java.exe
    set JPACKAGE_CMD=RouteMaker\jre\bin\jpackage.exe
) else (
    set JAVA_CMD=java
    set JPACKAGE_CMD=jpackage
)

:: Check if jpackage is available
echo Checking jpackage availability...
%JPACKAGE_CMD% --help >nul 2>&1
if errorlevel 1 (
    echo ERROR: jpackage not found! Please use Java 17+ or ensure jpackage is available.
    pause
    exit /b 1
)

:: Compile the application
echo Compiling application...
if exist "RouteMaker\jre\bin\javac.exe" (
    RouteMaker\jre\bin\javac.exe --module-path "RouteMaker\javafx\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "pdfbox.jar" *.java
) else (
    javac --module-path "RouteMaker\javafx\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "pdfbox.jar" *.java
)
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

:: Create Fat JAR file (includes PDFBox)
echo Creating Fat JAR file with PDFBox...
if exist "RouteMaker\jre\bin\jar.exe" (
    RouteMaker\jre\bin\jar.exe cfe RouteMaker.jar RouteMaker *.class
    RouteMaker\jre\bin\jar.exe uf RouteMaker.jar -C . pdfbox.jar
) else (
    jar cfe RouteMaker.jar RouteMaker *.class
    jar uf RouteMaker.jar -C . pdfbox.jar
)
if errorlevel 1 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)

:: Clean up before jpackage
echo Cleaning up for jpackage...
if exist "dist" rmdir /s /q "dist"
if exist "*.class" del "*.class"

:: Create portable app with jpackage
echo Creating portable application...
%JPACKAGE_CMD% ^
    --input . ^
    --name "RouteMaker" ^
    --main-jar RouteMaker.jar ^
    --main-class RouteMaker ^
    --module-path "RouteMaker\javafx\lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing ^
    --type app-image ^
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
echo ✅ Portable app created successfully!
echo    Check the 'dist\RouteMaker' folder
echo    Run RouteMaker.exe to start the application
echo.
pause
