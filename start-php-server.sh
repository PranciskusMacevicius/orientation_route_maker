#!/bin/bash

# Orientation Route Maker - PHP Server (No installations needed)
# If PHP is installed on the system

echo "🌐 Starting Orientation Route Maker Server..."
echo ""

if command -v php &> /dev/null; then
    echo "🐘 Using PHP built-in server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    php -S 127.0.0.1:3000
else
    echo "❌ PHP not found on this system."
    echo ""
    echo "Alternative options:"
    echo "1. Use Python server: ./start-python-server.sh"
    echo "2. Use Node.js version (requires npm install)"
    exit 1
fi
