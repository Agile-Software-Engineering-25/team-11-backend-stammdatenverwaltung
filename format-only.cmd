@echo off
REM Format code only with Spotless (Google Java Format)

echo Formatting Java code with Google Java Format style...
call mvnw.cmd spotless:apply

if %ERRORLEVEL% EQU 0 (
    echo ✅ Code formatting completed successfully!
) else (
    echo ❌ Code formatting failed. Please check the error messages above.
)

pause
