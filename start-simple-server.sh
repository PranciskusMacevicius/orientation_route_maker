#!/bin/bash

# Orientation Route Maker - Simple Local Server (No Node.js required)
# This script uses Python's built-in server (available on most systems)

echo "🌐 Starting Orientation Route Maker Local Server..."
echo ""

# Check if Python is available
if command -v python3 &> /dev/null; then
    echo "🐍 Using Python 3 server..."
    echo "🚀 Server starting on http://127.0.0.1:3000"
    echo "📱 For mobile testing, use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop the server"
    echo ""
    python3 -m http.server 3000
elif command -v python &> /dev/null; then
    echo "🐍 Using Python 2 server..."
    echo "🚀 Server starting on http://127.0.0.1:3000"
    echo "📱 For mobile testing, use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop the server"
    echo ""
    python -m SimpleHTTPServer 3000
else
    echo "❌ Python not found!"
    echo "Please install Python or use the Node.js version instead."
    echo ""
    echo "To use Node.js version:"
    echo "1. Install Node.js from https://nodejs.org"
    echo "2. Run: npm install"
    echo "3. Run: ./start-server.sh"
    exit 1
fi
