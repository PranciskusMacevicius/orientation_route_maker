#!/bin/bash

# Orientation Route Maker - Portable Server
# No system dependencies required

echo "🌐 Starting Portable Orientation Route Maker Server..."
echo ""

# Get the directory where this script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Use the included simple server
if [ -f "$DIR/portable-server/simple-server.js" ]; then
    echo "✅ Using portable HTTP server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    node "$DIR/portable-server/simple-server.js"
else
    echo "❌ Portable server not found!"
    echo "Please ensure 'portable-server/simple-server.js' exists"
    exit 1
fi
