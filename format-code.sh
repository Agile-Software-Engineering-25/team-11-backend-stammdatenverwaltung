#!/bin/bash
# Complete code quality script (Spotless + Checkstyle)
# 1. Spotless: Automatic formatting (like Prettier for Java)
# 2. Checkstyle: Logic and complexity checks

echo "================================"
echo "   Code Quality Check Suite"
echo "================================"
echo

echo "[1/2] Formatting code with Spotless (Google Java Format)..."
./mvnw spotless:apply

if [ $? -eq 0 ]; then
    echo "✅ Code formatting completed successfully!"
    echo
    
    echo "[2/2] Running Checkstyle logic and complexity checks..."
    ./mvnw checkstyle:check
    
    if [ $? -eq 0 ]; then
        echo "✅ All code quality checks passed!"
        echo
        echo "🎉 Your code is properly formatted and follows best practices!"
    else
        echo "⚠️  Checkstyle found some logic/complexity issues."
        echo "    Check the output above for details."
        echo "    Note: These are warnings, not formatting errors."
    fi
else
    echo "❌ Code formatting failed. Please check the error messages above."
    exit 1
fi

echo
echo "================================"
