# 🌐 Orientation Route Maker - Server Setup

## Quick Start (No Installations Required)

### Option 1: Use Built-in Server Scripts

**Windows:**
```bash
start-server.bat
```

**Mac/Linux:**
```bash
./start-any-server.sh
```

### Option 2: Manual Server Commands

**Python 3 (Most Common):**
```bash
python3 -m http.server 3000
```

**Python 2:**
```bash
python -m SimpleHTTPServer 3000
```

**PHP:**
```bash
php -S 127.0.0.1:3000
```

## 📱 Mobile Testing

1. **Find your computer's IP address:**
   - Windows: `ipconfig`
   - Mac/Linux: `ifconfig` or `ip addr`

2. **Use your IP instead of localhost:**
   - Instead of: `http://127.0.0.1:3000`
   - Use: `http://192.168.1.100:3000` (your actual IP)

## 🚀 What Happens

1. **Server starts** on port 3000
2. **Browser opens** automatically to the app
3. **Mobile devices** can access via your IP address
4. **Press Ctrl+C** to stop the server

## ❓ Troubleshooting

### "Command not found" errors:
- **Python not installed:** Download from https://python.org
- **PHP not installed:** Download from https://php.net
- **Node.js not installed:** Download from https://nodejs.org

### Alternative: Direct File Access
If no server works, you can still use the app by:
1. Opening `Orientation Route Maker.html` directly in your browser
2. Note: Some features may not work without a server

## 📁 Files Included

- `start-server.bat` - Windows server script
- `start-any-server.sh` - Mac/Linux server script  
- `serve.html` - Server information page
- `Orientation Route Maker.html` - Main application

## 🎯 Success!

Once the server is running, you'll see:
- **Desktop:** http://127.0.0.1:3000
- **Mobile:** http://[your-ip]:3000
- **App opens automatically** in your browser
