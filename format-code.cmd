@echo off
REM Complete code quality script (Spotless + Checkstyle)
REM 1. Spotless: Automatic formatting (like Prettier for Java)
REM 2. Checkstyle: Logic and complexity checks

echo ================================
echo   Code Quality Check Suite
echo ================================
echo.

echo [1/2] Formatting code with Spotless (Google Java Format)...
call mvnw.cmd spotless:apply

if %ERRORLEVEL% EQU 0 (
    echo ✅ Code formatting completed successfully!
    echo.
    
    echo [2/2] Running Checkstyle logic and complexity checks...
    call mvnw.cmd checkstyle:check
    
    if %ERRORLEVEL% EQU 0 (
        echo ✅ All code quality checks passed!
        echo.
        echo 🎉 Your code is properly formatted and follows best practices!
    ) else (
        echo ⚠️  Checkstyle found some logic/complexity issues.
        echo    Check the output above for details.
        echo    Note: These are warnings, not formatting errors.
    )
) else (
    echo ❌ Code formatting failed. Please check the error messages above.
)

echo.
echo ================================
pause
