#!/bin/bash

# Orientation Route Maker - Universal Server Script
# Tries Python, PHP, or Node.js in order of preference

echo "🌐 Starting Orientation Route Maker Server..."
echo "🔍 Looking for available server options..."
echo ""

# Try Python 3 first
if command -v python3 &> /dev/null; then
    echo "✅ Found Python 3"
    echo "🐍 Using Python 3 server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    python3 -m http.server 3000
    exit 0
fi

# Try Python 2
if command -v python &> /dev/null; then
    echo "✅ Found Python 2"
    echo "🐍 Using Python 2 server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    python -m SimpleHTTPServer 3000
    exit 0
fi

# Try PHP
if command -v php &> /dev/null; then
    echo "✅ Found PHP"
    echo "🐘 Using PHP server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    php -S 127.0.0.1:3000
    exit 0
fi

# Try Node.js (requires npm install)
if command -v node &> /dev/null && [ -d "node_modules" ]; then
    echo "✅ Found Node.js with dependencies"
    echo "📦 Using Node.js server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    npx live-server --port=3000 --open="Orientation Route Maker.html" --no-browser
    exit 0
fi

# No server found
echo "❌ No server found on this system!"
echo ""
echo "Available options:"
echo "1. Install Python from https://python.org"
echo "2. Install PHP from https://php.net"
echo "3. Install Node.js from https://nodejs.org and run: npm install"
echo ""
echo "Or just open 'Orientation Route Maker.html' directly in your browser"
exit 1
