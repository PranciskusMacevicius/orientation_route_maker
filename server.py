#!/usr/bin/env python3
"""
Simple HTTP server for the Orientation Route Maker web application.
Run this script to serve the web application locally.
"""

import http.server
import socketserver
import webbrowser
import os
import sys
from pathlib import Path

# Configuration
PORT = 8000
HOST = 'localhost'

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Custom handler to serve files with proper MIME types."""
    
    def end_headers(self):
        # Add CORS headers to allow cross-origin requests
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()
    
    def guess_type(self, path):
        """Override to set correct MIME types for web files."""
        mimetype, encoding = super().guess_type(path)
        
        # Ensure JavaScript files are served with correct MIME type
        if path.endswith('.js'):
            return 'application/javascript'
        elif path.endswith('.css'):
            return 'text/css'
        elif path.endswith('.html'):
            return 'text/html'
        
        return mimetype

def main():
    """Start the HTTP server and open the application in browser."""
    
    # Change to the directory containing this script
    script_dir = Path(__file__).parent
    os.chdir(script_dir)
    
    # Check if required files exist
    required_files = ['index.html', 'app.js']
    missing_files = [f for f in required_files if not Path(f).exists()]
    
    if missing_files:
        print(f"Error: Missing required files: {', '.join(missing_files)}")
        print("Please ensure all web application files are in the same directory as this server script.")
        sys.exit(1)
    
    # Create server
    with socketserver.TCPServer((HOST, PORT), CustomHTTPRequestHandler) as httpd:
        print("=" * 60)
        print("🚀 Orientation Route Maker - Web Server")
        print("=" * 60)
        print(f"Server running at: http://{HOST}:{PORT}")
        print(f"Opening application in your default browser...")
        print("\nTo stop the server, press Ctrl+C")
        print("=" * 60)
        
        # Open browser
        try:
            webbrowser.open(f'http://{HOST}:{PORT}')
        except Exception as e:
            print(f"Could not open browser automatically: {e}")
            print(f"Please manually open: http://{HOST}:{PORT}")
        
        # Start serving
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n\nServer stopped by user.")
            print("Thank you for using Orientation Route Maker!")

if __name__ == '__main__':
    main()
