@echo off
echo Compiling RouteMaker with JavaFX...

:: Compile all Java files with JavaFX modules
"%JAVA_HOME%\bin\javac.exe" --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "pdfbox.jar" -d bin *.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful! Running RouteMaker...
echo.

:: Run the application with JavaFX modules
"%JAVA_HOME%\bin\java.exe" --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -cp "bin;pdfbox.jar" RouteMaker

echo.
echo Application finished.
pause
