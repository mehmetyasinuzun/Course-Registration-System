@echo off
cd /d "%~dp0"

REM Copy resources to out directory
if not exist "out\view" mkdir "out\view"
if exist "src\view\university_bg.jpg" copy /Y "src\view\university_bg.jpg" "out\view\university_bg.jpg" >nul

REM Run the application
java --module-path lib\javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml -cp "out;lib\sqlite-jdbc-3.51.1.0.jar" Main

if errorlevel 1 (
    echo.
    echo Error occurred while running the application!
    pause
) else (
    echo.
    echo Application closed successfully.
    pause
)
