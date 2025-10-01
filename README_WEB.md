# Orientation Route Maker - Web Version

A web-based version of the Orientation Route Maker application that runs in your browser instead of as a Windows application.

## Features

- **Interactive Google Maps**: Click on the map to add waypoints for your orientation route
- **Waypoint Management**: Add, remove, undo/redo waypoints with full history support
- **Route Operations**: Invert route direction, clear all waypoints
- **PDF Generation**: Generate PDF files with route information and coordinates
- **UTM Coordinates**: Real-time UTM coordinate display and conversion
- **Responsive Design**: Works on desktop and mobile devices

## How to Use

### Quick Start (Windows)

1. Double-click `start_web_app.bat`
2. The application will open in your default browser
3. Start creating your route by clicking on the map!

### Manual Start

1. Make sure you have Python 3.6+ installed
2. Open a terminal/command prompt in this directory
3. Run: `python server.py`
4. Open your browser and go to `http://localhost:8000`

### Using the Application

1. **Adding Waypoints**: Click anywhere on the map to add waypoints
   - First click adds "S" (Start)
   - Middle clicks add numbered waypoints
   - Last waypoint becomes "F" (Finish)

2. **Controls**:
   - **Zoom In/Out**: Adjust map zoom level
   - **Remove Route**: Clear all waypoints
   - **Invert Route**: Reverse the direction of your route
   - **Remove Last Point**: Undo the last waypoint added
   - **Add Last Removed Point**: Redo a removed waypoint
   - **Generate PDF**: Create a PDF with route information

3. **Coordinate Display**: Move your mouse over the map to see UTM coordinates

4. **PDF Output**: The generated PDF includes:
   - Waypoint numbers (Start, numbered points, Finish)
   - UTM coordinates for each waypoint
   - Random letters for each waypoint
   - Next waypoint coordinates

## Technical Details

### Requirements

- Modern web browser (Chrome, Firefox, Safari, Edge)
- Internet connection (for Google Maps API)
- Python 3.6+ (for local server)

### Files

- `index.html` - Main web page
- `app.js` - JavaScript application logic
- `server.py` - Python HTTP server
- `start_web_app.bat` - Windows launcher script
- `icon.png` - Application icon (optional)

### Google Maps API

The application uses the same Google Maps API key as the original Java version. The API key is embedded in the HTML file.

### PDF Generation

PDFs are generated using the jsPDF library and include:
- Lithuanian text labels
- UTM coordinate conversion
- Grid layout (2x4 points per page)
- Professional formatting

## Differences from Java Version

### Advantages
- **Cross-platform**: Runs on any device with a web browser
- **No installation**: Just open in browser
- **Mobile friendly**: Works on tablets and phones
- **Easy sharing**: Send URL to others
- **Always up-to-date**: No need to update software

### Limitations
- **Requires internet**: Needs connection for Google Maps
- **Browser dependent**: Some features may vary by browser
- **No offline mode**: Cannot work without internet connection

## Troubleshooting

### Common Issues

1. **"Loading Google Maps..." never finishes**
   - Check your internet connection
   - Try refreshing the page
   - Check if Google Maps is blocked by firewall

2. **Python server won't start**
   - Make sure Python is installed: `python --version`
   - Try using `python3` instead of `python`
   - Check if port 8000 is already in use

3. **PDF generation fails**
   - Make sure you have waypoints on the map
   - Check browser console for errors
   - Try a different browser

4. **Buttons are disabled**
   - Wait for the map to fully load
   - Refresh the page if needed

### Browser Compatibility

- **Chrome**: Full support, recommended
- **Firefox**: Full support
- **Safari**: Full support
- **Edge**: Full support
- **Internet Explorer**: Not supported

## Development

To modify the application:

1. Edit `app.js` for functionality changes
2. Edit `index.html` for UI changes
3. Restart the server to see changes
4. Use browser developer tools for debugging

## Support

If you encounter issues:

1. Check the browser console for error messages
2. Ensure all files are in the same directory
3. Try a different browser
4. Check your internet connection

The web version maintains all the functionality of the original Java application while providing better accessibility and cross-platform compatibility.
