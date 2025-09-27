@echo off
echo Compiling RouteMaker with JavaFX...

:: Clean the bin directory first
if exist "bin\*.class" (
    echo Cleaning previous compilation...
    del /q bin\*.class
)

:: Compile all Java files with JavaFX modules
echo Compiling Java source files...
"%JAVA_HOME%\bin\javac.exe" --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "pdfbox.jar" -d bin *.java

if errorlevel 1 (
    echo.
    echo ❌ Compilation failed!
    echo Check the error messages above.
    pause
    exit /b 1
) else (
    echo.
    echo ✅ Compilation successful!
    echo Generated class files in bin\ directory.
    echo.
    echo You can now:
    echo   - Use "Run Java" in Cursor (if it works)
    echo   - Run compile_and_run.bat to compile and run
    echo   - Run the application manually
)

pause
