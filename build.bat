@echo off
echo Building Course Registration System...
cd /d "%~dp0"

REM Clean out directory
if exist "out" rmdir /S /Q "out"
mkdir "out"

REM Copy resources
echo Copying resources...
mkdir "out\view"
copy /Y "src\view\university_bg.jpg" "out\view\university_bg.jpg" >nul

REM Compile
echo Compiling...
javac --module-path lib\javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml -cp lib\sqlite-jdbc-3.51.1.0.jar -d out -sourcepath src src\Main.java

if errorlevel 1 (
    echo.
    echo Build FAILED!
    pause
    exit /b 1
) else (
    echo.
    echo Build SUCCESS!
    echo You can now run the application using run.bat
    pause
)
