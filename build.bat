@echo off
echo Building Zoo Tycoon Plugin...
echo Ensuring Maven is available...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build Failed! 
    echo Please ensure Maven and Java 21 are installed and added to your PATH.
    echo You may need to restart your computer or command prompt if you just installed them.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Build Successful!
echo Plugin jar is located in the target/ folder.
pause
