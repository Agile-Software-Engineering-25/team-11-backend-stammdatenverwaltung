#!/bin/bash
# Format code only with Spotless (Google Java Format)

echo "Formatting Java code with Google Java Format style..."
./mvnw spotless:apply

if [ $? -eq 0 ]; then
    echo "✅ Code formatting completed successfully!"
else
    echo "❌ Code formatting failed. Please check the error messages above."
    exit 1
fi
