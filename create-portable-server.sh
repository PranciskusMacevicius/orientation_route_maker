#!/bin/bash

# Create Portable Server - Downloads a single executable HTTP server
# No dependencies, no installations, just one file

echo "📦 Creating truly portable server..."
echo ""

# Create server directory
mkdir -p portable-server

# Download a lightweight HTTP server (Caddy is a good choice)
echo "⬇️  Downloading portable HTTP server..."

# Detect OS and download appropriate binary
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    curl -L -o portable-server/caddy "https://caddyserver.com/api/download?os=darwin&arch=amd64&idempotency=123456789"
    chmod +x portable-server/caddy
    SERVER_CMD="./portable-server/caddy"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    curl -L -o portable-server/caddy "https://caddyserver.com/api/download?os=linux&arch=amd64&idempotency=123456789"
    chmod +x portable-server/caddy
    SERVER_CMD="./portable-server/caddy"
else
    echo "❌ Unsupported OS for automatic download"
    echo "Please download Caddy manually from https://caddyserver.com"
    exit 1
fi

# Create Caddyfile
cat > portable-server/Caddyfile << 'EOF'
:3000 {
    file_server browse
    log {
        output file portable-server/access.log
    }
}
EOF

# Create start script
cat > start-portable.sh << EOF
#!/bin/bash

# Orientation Route Maker - Portable Server
# Single executable, no dependencies

echo "🌐 Starting Portable Orientation Route Maker Server..."
echo ""

# Get the directory where this script is located
DIR="\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Start the portable server
cd "\$DIR"
echo "✅ Using portable HTTP server..."
echo "🚀 Server: http://127.0.0.1:3000"
echo "📱 Mobile: Use your computer's IP address"
echo "⏹️  Press Ctrl+C to stop"
echo ""

$SERVER_CMD run --config portable-server/Caddyfile
EOF

chmod +x start-portable.sh
echo "✅ Created start-portable.sh"

echo ""
echo "🎉 Portable server created!"
echo ""
echo "Files included:"
echo "  - portable-server/caddy (HTTP server executable)"
echo "  - portable-server/Caddyfile (server configuration)"
echo "  - start-portable.sh (start script)"
echo ""
echo "Users can now run:"
echo "  ./start-portable.sh"
echo ""
echo "No system dependencies required!"
