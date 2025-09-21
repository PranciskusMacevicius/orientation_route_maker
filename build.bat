@echo off
echo Building RouteMaker portable application...
echo.

:: Check if any Java source files exist
set JAVA_FILES_FOUND=0
for %%f in (*.java) do set JAVA_FILES_FOUND=1
if %JAVA_FILES_FOUND%==0 (
    echo ERROR: No .java files found in current directory!
    pause
    exit /b 1
)

:: Check if RouteMaker directory exists
if not exist "RouteMaker\" (
    echo ERROR: RouteMaker directory not found!
    echo Please ensure the RouteMaker portable directory exists.
    pause
    exit /b 1
)

:: Use JRE from RouteMaker directory to compile
set JRE_PATH=RouteMaker\jre

if not exist "%JRE_PATH%\bin\javac.exe" (
    echo ERROR: Java compiler not found at %JRE_PATH%\bin\javac.exe
    pause
    exit /b 1
)

:: Compile all Java source files
echo Compiling Java files...
if exist "RouteMaker\javafx\lib" (
    echo Using JavaFX for compilation...
    "%JRE_PATH%\bin\javac.exe" --module-path RouteMaker\javafx\lib --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "pdfbox.jar" *.java
) else (
    echo Compiling without JavaFX...
    "%JRE_PATH%\bin\javac.exe" -cp "pdfbox.jar" *.java
)
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

:: Create JAR file with all compiled classes
echo Creating RouteMaker.jar...
"%JRE_PATH%\bin\jar.exe" -cfe RouteMaker.jar RouteMaker *.class
if errorlevel 1 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)

:: Copy JAR to RouteMaker directory
echo Deploying to RouteMaker directory...
copy "RouteMaker.jar" "RouteMaker\"
copy "pdfbox.jar" "RouteMaker\"
if errorlevel 1 (
    echo ERROR: Failed to copy JAR to RouteMaker directory!
    pause
    exit /b 1
)

:: Clean up temporary files
del *.class >nul 2>&1
del RouteMaker.jar >nul 2>&1

echo.
echo ✅ Build successful!
echo    RouteMaker.jar has been deployed to the RouteMaker directory.
echo    You can now distribute the RouteMaker folder or test with RouteMaker\run.bat
echo.
pause
