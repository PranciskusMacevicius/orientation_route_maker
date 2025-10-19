#!/bin/bash

# Orientation Route Maker - Development Server Script
# This script sets up a local development environment

echo "🚀 Starting Orientation Route Maker Development Server..."
echo ""

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo ""
fi

# Check if dist directory exists
if [ ! -d "dist" ]; then
    echo "🔨 Building project for first time..."
    npm run build
    echo ""
fi

echo "🌐 Starting development server..."
echo "📍 Server will be available at: http://127.0.0.1:3000"
echo "📱 Mobile testing: Use your computer's IP address (e.g., http://192.168.1.100:3000)"
echo "🔄 Auto-reload enabled - browser will refresh when you edit files"
echo "⏹️  Press Ctrl+C to stop the server"
echo ""

# Start the development server
npm run dev
