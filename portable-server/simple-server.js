#!/usr/bin/env node

// Simple HTTP Server - No dependencies required
// This is a standalone Node.js HTTP server

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const HOST = '127.0.0.1';

// MIME types
const mimeTypes = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

function getMimeType(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    return mimeTypes[ext] || 'application/octet-stream';
}

function serveFile(filePath, res) {
    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('File not found');
            return;
        }
        
        const mimeType = getMimeType(filePath);
        res.writeHead(200, { 'Content-Type': mimeType });
        res.end(data);
    });
}

const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url);
    let pathname = parsedUrl.pathname;
    
    // Default to index.html if root
    if (pathname === '/') {
        pathname = '/Orientation Route Maker.html';
    }
    
    // Security: prevent directory traversal
    const safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
    const filePath = path.join(__dirname, '..', safePath);
    
    // Check if file exists
    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            res.writeHead(404, { 'Content-Type': 'text/html' });
            res.end(`
                <html>
                    <body>
                        <h1>404 - File Not Found</h1>
                        <p>The requested file was not found on this server.</p>
                        <a href="/">Go to Orientation Route Maker</a>
                    </body>
                </html>
            `);
            return;
        }
        
        serveFile(filePath, res);
    });
});

server.listen(PORT, HOST, () => {
    console.log(`🌐 Orientation Route Maker Server running at http://${HOST}:${PORT}/`);
    console.log(`📱 Mobile access: Use your computer's IP address instead of localhost`);
    console.log(`⏹️  Press Ctrl+C to stop the server`);
});

// Handle server errors
server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
        console.log(`❌ Port ${PORT} is already in use. Please stop other servers or use a different port.`);
    } else {
        console.log(`❌ Server error: ${err.message}`);
    }
    process.exit(1);
});
