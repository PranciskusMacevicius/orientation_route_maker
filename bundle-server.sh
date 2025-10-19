#!/bin/bash

# Bundle Server Dependencies Script
# This script downloads and bundles a standalone server

echo "📦 Bundling standalone server for Orientation Route Maker..."
echo ""

# Create server directory
mkdir -p server

# Download standalone live-server (if not exists)
if [ ! -f "server/live-server.js" ]; then
    echo "⬇️  Downloading standalone live-server..."
    curl -o server/live-server.js https://raw.githubusercontent.com/tapio/live-server/master/live-server.js
    echo "✅ Downloaded live-server.js"
fi

# Create standalone server script
cat > start-standalone.sh << 'EOF'
#!/bin/bash

# Orientation Route Maker - Standalone Server
# No installations required - everything is bundled

echo "🌐 Starting Orientation Route Maker Standalone Server..."
echo ""

# Check if Node.js is available
if command -v node &> /dev/null; then
    echo "✅ Using bundled live-server..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    node server/live-server.js --port=3000 --open="Orientation Route Maker.html" --no-browser
else
    echo "❌ Node.js not found!"
    echo ""
    echo "Alternative: Use Python server (if Python is installed)"
    echo "Run: python3 -m http.server 3000"
    echo ""
    echo "Or just open 'Orientation Route Maker.html' directly in your browser"
    exit 1
fi
EOF

chmod +x start-standalone.sh
echo "✅ Created start-standalone.sh"

echo ""
echo "🎉 Bundling complete!"
echo ""
echo "Users can now run:"
echo "  ./start-standalone.sh"
echo ""
echo "No additional installations required!"
