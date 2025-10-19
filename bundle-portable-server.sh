#!/bin/bash

# Bundle Portable Server - Downloads and bundles everything needed
# No system dependencies required

echo "📦 Creating portable server bundle..."
echo ""

# Create portable directory
mkdir -p portable-server

# Download portable Node.js (if not exists)
if [ ! -f "portable-server/node.exe" ] && [ ! -f "portable-server/node" ]; then
    echo "⬇️  Downloading portable Node.js..."
    
    # Detect OS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        curl -L -o portable-server/node.tar.gz https://nodejs.org/dist/v20.10.0/node-v20.10.0-darwin-x64.tar.gz
        cd portable-server && tar -xzf node.tar.gz && mv node-v20.10.0-darwin-x64/* . && rm -rf node-v20.10.0-darwin-x64 node.tar.gz && cd ..
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        curl -L -o portable-server/node.tar.gz https://nodejs.org/dist/v20.10.0/node-v20.10.0-linux-x64.tar.gz
        cd portable-server && tar -xzf node.tar.gz && mv node-v20.10.0-linux-x64/* . && rm -rf node-v20.10.0-linux-x64 node.tar.gz && cd ..
    else
        echo "❌ Unsupported OS. Please download Node.js manually."
        exit 1
    fi
    echo "✅ Downloaded portable Node.js"
fi

# Download live-server standalone
if [ ! -f "portable-server/live-server.js" ]; then
    echo "⬇️  Downloading live-server..."
    curl -L -o portable-server/live-server.js https://raw.githubusercontent.com/tapio/live-server/master/live-server.js
    echo "✅ Downloaded live-server"
fi

# Create portable server script
cat > start-portable.sh << 'EOF'
#!/bin/bash

# Orientation Route Maker - Portable Server
# No system dependencies required

echo "🌐 Starting Portable Orientation Route Maker Server..."
echo ""

# Get the directory where this script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Try portable Node.js first
if [ -f "$DIR/portable-server/node" ]; then
    echo "✅ Using portable Node.js..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    "$DIR/portable-server/node" "$DIR/portable-server/live-server.js" --port=3000 --open="Orientation Route Maker.html" --no-browser
    exit 0
fi

# Fallback to system Node.js
if command -v node &> /dev/null; then
    echo "✅ Using system Node.js..."
    echo "🚀 Server: http://127.0.0.1:3000"
    echo "📱 Mobile: Use your computer's IP address"
    echo "⏹️  Press Ctrl+C to stop"
    echo ""
    node "$DIR/portable-server/live-server.js" --port=3000 --open="Orientation Route Maker.html" --no-browser
    exit 0
fi

echo "❌ No Node.js found!"
echo "Please install Node.js from https://nodejs.org"
echo "Or just open 'Orientation Route Maker.html' directly"
exit 1
EOF

chmod +x start-portable.sh
echo "✅ Created start-portable.sh"

echo ""
echo "🎉 Portable server bundle complete!"
echo ""
echo "Users can now run:"
echo "  ./start-portable.sh"
echo ""
echo "No system dependencies required!"
