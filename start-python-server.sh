#!/bin/bash

# Orientation Route Maker - Python Server (No installations needed)
# Most computers already have Python installed

echo "🌐 Starting Orientation Route Maker Server..."
echo ""

# Try Python 3 first (most modern systems)
if command -v python3 &> /dev/null; then
    echo "🐍 Using Python 3 built-in server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    python3 -m http.server 3000
elif command -v python &> /dev/null; then
    echo "🐍 Using Python 2 built-in server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    python -m SimpleHTTPServer 3000
else
    echo "❌ Python not found on this system."
    echo ""
    echo "Alternative options:"
    echo "1. Install Python from https://python.org"
    echo "2. Use Node.js version (requires npm install)"
    echo "3. Use PHP server (if PHP is installed)"
    exit 1
fi
