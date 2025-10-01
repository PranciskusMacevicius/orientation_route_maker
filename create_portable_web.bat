@echo off
echo ============================================================
echo    Creating Portable Web Version of Route Maker
echo ============================================================
echo.

:: Create portable directory structure
if not exist "RouteMaker_Web_Portable" mkdir "RouteMaker_Web_Portable"
if not exist "RouteMaker_Web_Portable\python" mkdir "RouteMaker_Web_Portable\python"
if not exist "RouteMaker_Web_Portable\web" mkdir "RouteMaker_Web_Portable\web"

echo Creating portable web application...

:: Copy web files
copy "index.html" "RouteMaker_Web_Portable\web\"
copy "app.js" "RouteMaker_Web_Portable\web\"
copy "icon.png" "RouteMaker_Web_Portable\web\"
copy "server.py" "RouteMaker_Web_Portable\web\"

:: Create portable Python launcher
echo Creating portable Python launcher...
(
echo @echo off
echo echo ============================================================
echo echo    Orientation Route Maker - Portable Web Version
echo echo ============================================================
echo echo.
echo echo Starting portable web server...
echo echo The application will open in your default browser.
echo echo.
echo echo To stop the server, close this window or press Ctrl+C
echo echo.
echo.
echo :: Use embedded Python if available, otherwise try system Python
echo if exist "python\python.exe" ^(
echo     echo Using embedded Python...
echo     "python\python.exe" "web\server.py"
echo ^) else ^(
echo     echo Trying system Python...
echo     python "web\server.py"
echo     if errorlevel 1 ^(
echo         echo.
echo         echo ERROR: Python not found!
echo         echo Please install Python 3.6+ from https://www.python.org/downloads/
echo         echo Or use the standalone HTML version instead.
echo         echo.
echo         pause
echo     ^)
echo ^)
) > "RouteMaker_Web_Portable\start_web_app.bat"

:: Create standalone HTML version as backup
echo Creating standalone HTML version...
(
echo ^<!DOCTYPE html^>
echo ^<html lang="en"^>
echo ^<head^>
echo     ^<meta charset="UTF-8"^>
echo     ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^>
echo     ^<title^>Orientation Route Maker - Standalone^</title^>
echo     ^<link rel="icon" type="image/png" href="icon.png"^>
echo     ^<style^>
echo         * { margin: 0; padding: 0; box-sizing: border-box; }
echo         body { font-family: Arial, sans-serif; height: 100vh; overflow: hidden; background-color: #f0f0f0; }
echo         .container { display: flex; height: 100vh; }
echo         .map-container { flex: 1; position: relative; }
echo         #map { width: 100%%; height: 100%%; }
echo         .control-panel { width: 220px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; display: flex; flex-direction: column; gap: 15px; box-shadow: -2px 0 10px rgba(0,0,0,0.1); }
echo         .control-panel h2 { color: white; text-align: center; margin-bottom: 20px; font-size: 18px; text-shadow: 0 2px 4px rgba(0,0,0,0.3); }
echo         .btn { width: 100%%; padding: 12px 16px; border: none; border-radius: 8px; background: rgba(255, 255, 255, 0.9); color: #333; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 2px 8px rgba(0,0,0,0.1); text-align: center; }
echo         .btn:hover { background: white; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.2); }
echo         .btn:active { transform: translateY(0); }
echo         .btn:disabled { background: rgba(255, 255, 255, 0.5); color: #999; cursor: not-allowed; transform: none; box-shadow: none; }
echo         .btn.two-line { padding: 16px 12px; line-height: 1.2; }
echo         .coordinate-display { position: absolute; top: 10px; left: 10px; background: rgba(0, 0, 0, 0.8); color: white; padding: 8px 12px; border-radius: 4px; font-family: monospace; font-size: 14px; z-index: 1000; pointer-events: none; min-width: 200px; }
echo         .status-message { position: absolute; top: 50%%; left: 50%%; transform: translate(-50%%, -50%%); background: rgba(0, 0, 0, 0.8); color: white; padding: 20px 30px; border-radius: 8px; font-size: 16px; z-index: 1001; display: none; }
echo         .loading { position: absolute; top: 50%%; left: 50%%; transform: translate(-50%%, -50%%); color: white; font-size: 18px; z-index: 1001; }
echo         @media (max-width: 768px) { .container { flex-direction: column; } .control-panel { width: 100%%; height: auto; flex-direction: row; flex-wrap: wrap; padding: 10px; } .btn { flex: 1; min-width: 120px; margin: 2px; } }
echo     ^</style^>
echo ^</head^>
echo ^<body^>
echo     ^<div class="container"^>
echo         ^<div class="map-container"^>
echo             ^<div id="map"^>^</div^>
echo             ^<div class="coordinate-display" id="coordinateDisplay"^>-- --^</div^>
echo             ^<div class="loading" id="loading"^>Loading Google Maps...^</div^>
echo             ^<div class="status-message" id="statusMessage"^>^</div^>
echo         ^</div^>
echo         ^<div class="control-panel"^>
echo             ^<h2^>Route Controls^</h2^>
echo             ^<button class="btn" id="zoomInBtn" onclick="zoomIn()"^>Zoom In^</button^>
echo             ^<button class="btn" id="zoomOutBtn" onclick="zoomOut()"^>Zoom Out^</button^>
echo             ^<button class="btn" id="resetBtn" onclick="clearWaypoints()"^>Remove Route^</button^>
echo             ^<button class="btn" id="invertBtn" onclick="invertRoute()"^>Invert Route^</button^>
echo             ^<button class="btn two-line" id="undoBtn" onclick="undo()"^>Remove Last^<br^>Point^</button^>
echo             ^<button class="btn two-line" id="redoBtn" onclick="redo()"^>Add Last^<br^>Removed Point^</button^>
echo             ^<button class="btn" id="pdfBtn" onclick="generatePDF()"^>Generate PDF^</button^>
echo         ^</div^>
echo     ^</div^>
echo     ^<script async defer src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBxUx71FnVZj95ze2B7gs99NSzdIl-957k&callback=initMap"^>^</script^>
echo     ^<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"^>^</script^>
echo     ^<script^>
echo         // Global variables
echo         let map;
echo         let waypoints = [];
echo         let markers = [];
echo         let polyline;
echo         let isMapReady = false;
echo         let undoHistory = [];
echo         let redoHistory = [];
echo         let undoCount = 0;
echo         const MAX_HISTORY_SIZE = 20;
echo         const DEFAULT_LATITUDE = 55.030180;
echo         const DEFAULT_LONGITUDE = 24.370464;
echo         function initMap() {
echo             try {
echo                 map = new google.maps.Map(document.getElementById('map'), {
echo                     center: { lat: DEFAULT_LATITUDE, lng: DEFAULT_LONGITUDE },
echo                     zoom: 13,
echo                     mapTypeId: google.maps.MapTypeId.SATELLITE,
echo                     draggable: false,
echo                     zoomControl: false,
echo                     scrollwheel: false,
echo                     disableDoubleClickZoom: true,
echo                     clickableIcons: false
echo                 });
echo                 map.addListener('click', function(event) {
echo                     if (!isMapReady) {
echo                         console.log('Map not ready - click ignored');
echo                         return;
echo                     }
echo                     addWaypoint(event.latLng);
echo                 });
echo                 map.addListener('mousemove', function(event) {
echo                     const lat = event.latLng.lat();
echo                     const lng = event.latLng.lng();
echo                     const utmCoords = convertToUTM(lat, lng);
echo                     document.getElementById('coordinateDisplay').textContent = utmCoords;
echo                 });
echo                 console.log('Google Maps initialized');
echo                 isMapReady = true;
echo                 onMapReady();
echo             } catch (error) {
echo                 console.error('Error initializing Google Maps:', error);
echo                 showStatus('Error loading Google Maps. Please check your internet connection.', 'error');
echo             }
echo         }
echo         function onMapReady() {
echo             console.log('Map is ready for interaction!');
echo             map.setOptions({
echo                 draggable: true,
echo                 zoomControl: true,
echo                 scrollwheel: true,
echo                 disableDoubleClickZoom: false,
echo                 clickableIcons: true
echo             });
echo             document.getElementById('loading').style.display = 'none';
echo             enableButtons(true);
echo         }
echo         function enableButtons(enabled) {
echo             const buttons = ['zoomInBtn', 'zoomOutBtn', 'resetBtn', 'invertBtn', 'undoBtn', 'redoBtn', 'pdfBtn'];
echo             buttons.forEach(btnId => {
echo                 document.getElementById(btnId).disabled = !enabled;
echo             });
echo         }
echo         function addWaypoint(latLng) {
echo             const pointNumber = waypoints.length === 0 ? 'S' : 'F';
echo             if (waypoints.length > 1) {
echo                 waypoints[waypoints.length - 1].number = (waypoints.length - 1).toString();
echo                 waypoints[waypoints.length - 1].marker.setLabel(waypoints[waypoints.length - 1].number);
echo             }
echo             const marker = new google.maps.Marker({
echo                 position: latLng,
echo                 map: map,
echo                 label: pointNumber
echo             });
echo             const randomLetter = generateRandomLetter();
echo             waypoints.push({
echo                 position: latLng,
echo                 number: pointNumber,
echo                 letter: randomLetter,
echo                 marker: marker
echo             });
echo             updateConnections();
echo             console.log(`Added waypoint ${pointNumber} at ${latLng.lat()}, ${latLng.lng()}`);
echo         }
echo         function generateRandomLetter() {
echo             const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
echo             return letters.charAt(Math.floor(Math.random() * letters.length));
echo         }
echo         function updateConnections() {
echo             if (polyline) {
echo                 polyline.setMap(null);
echo             }
echo             if (waypoints.length > 1) {
echo                 const path = waypoints.map(function(wp) { return wp.position; });
echo                 polyline = new google.maps.Polyline({
echo                     path: path,
echo                     geodesic: true,
echo                     strokeColor: '#FF0000',
echo                     strokeOpacity: 1.0,
echo                     strokeWeight: 2
echo                 });
echo                 polyline.setMap(map);
echo             }
echo         }
echo         function clearWaypoints() {
echo             waypoints.forEach(function(wp) {
echo                 wp.marker.setMap(null);
echo             });
echo             if (polyline) polyline.setMap(null);
echo             waypoints = [];
echo             clearHistory();
echo             console.log('All waypoints cleared');
echo         }
echo         function invertRoute() {
echo             if (waypoints.length < 2) return;
echo             waypoints.reverse();
echo             for (let i = 0; i < waypoints.length; i++) {
echo                 if (i === 0) {
echo                     waypoints[i].number = 'S';
echo                     waypoints[i].marker.setLabel('S');
echo                 } else if (i === waypoints.length - 1) {
echo                     waypoints[i].number = 'F';
echo                     waypoints[i].marker.setLabel('F');
echo                 } else {
echo                     waypoints[i].number = i.toString();
echo                     waypoints[i].marker.setLabel(i.toString());
echo                 }
echo             }
echo             updateConnections();
echo             console.log('Route inverted');
echo         }
echo         function zoomIn() {
echo             if (map) {
echo                 map.setZoom(map.getZoom() + 1);
echo             }
echo         }
echo         function zoomOut() {
echo             if (map) {
echo                 map.setZoom(map.getZoom() - 1);
echo             }
echo         }
echo         function undo() {
echo             console.log('Undo method called, undoCount:', undoCount);
echo             if (undoCount >= MAX_HISTORY_SIZE) {
echo                 console.log('Undo limit reached (20 operations)');
echo                 showStatus('Undo limit reached (20 operations)', 'warning');
echo                 return;
echo             }
echo             if (waypoints.length > 0) {
echo                 const lastWaypoint = waypoints[waypoints.length - 1];
echo                 console.log('Removing waypoint:', lastWaypoint.number);
echo                 redoHistory.push({
echo                     position: lastWaypoint.position,
echo                     number: lastWaypoint.number,
echo                     letter: lastWaypoint.letter
echo                 });
echo                 lastWaypoint.marker.setMap(null);
echo                 waypoints.pop();
echo                 updateConnections();
echo                 for (let i = 0; i < waypoints.length; i++) {
echo                     if (i === 0) {
echo                         waypoints[i].number = 'S';
echo                         waypoints[i].marker.setLabel('S');
echo                     } else if (i === waypoints.length - 1) {
echo                         waypoints[i].number = 'F';
echo                         waypoints[i].marker.setLabel('F');
echo                     } else {
echo                         waypoints[i].number = i.toString();
echo                         waypoints[i].marker.setLabel(i.toString());
echo                     }
echo                 }
echo                 undoCount++;
echo                 console.log('Waypoint removed, new length:', waypoints.length, 'undo count:', undoCount);
echo             } else {
echo                 console.log('No waypoints to remove');
echo             }
echo         }
echo         function redo() {
echo             if (redoHistory.length > 0) {
echo                 const waypointData = redoHistory.pop();
echo                 const marker = new google.maps.Marker({
echo                     position: waypointData.position,
echo                     map: map,
echo                     label: waypointData.number
echo                 });
echo                 waypoints.push({
echo                     position: waypointData.position,
echo                     number: waypointData.number,
echo                     letter: waypointData.letter,
echo                     marker: marker
echo                 });
echo                 updateConnections();
echo                 undoCount--;
echo                 console.log('Redo: restored waypoint', waypointData.number, 'undo count:', undoCount);
echo             } else {
echo                 console.log('Nothing to redo');
echo             }
echo         }
echo         function clearHistory() {
echo             undoHistory = [];
echo             redoHistory = [];
echo             undoCount = 0;
echo             console.log('History cleared, undo count reset to 0');
echo         }
echo         function generatePDF() {
echo             if (waypoints.length === 0) {
echo                 showStatus('No waypoints to generate PDF. Please add some points to the map first.', 'warning');
echo                 return;
echo             }
echo             try {
echo                 const { jsPDF } = window.jspdf;
echo                 const doc = new jsPDF();
echo                 for (let i = 0; i < waypoints.length; i++) {
echo                     if (i < waypoints.length - 1) {
echo                         waypoints[i].nextPoint = waypoints[i + 1];
echo                     }
echo                 }
echo                 const POINTS_PER_PAGE = 8;
echo                 const totalPages = Math.ceil(waypoints.length / POINTS_PER_PAGE);
echo                 for (let pageIndex = 0; pageIndex < totalPages; pageIndex++) {
echo                     if (pageIndex > 0) {
echo                         doc.addPage();
echo                     }
echo                     const startIndex = pageIndex * POINTS_PER_PAGE;
echo                     const endIndex = Math.min(startIndex + POINTS_PER_PAGE, waypoints.length);
echo                     const actualWaypoints = endIndex - startIndex;
echo                     const cols = 2;
echo                     const rows = 4;
echo                     const pageWidth = doc.internal.pageSize.getWidth();
echo                     const pageHeight = doc.internal.pageSize.getHeight();
echo                     const cellWidth = pageWidth / cols;
echo                     const cellHeight = pageHeight / rows;
echo                     doc.setLineWidth(2);
echo                     for (let i = 0; i < actualWaypoints; i++) {
echo                         const row = Math.floor(i / cols);
echo                         const col = i % cols;
echo                         const cellX = col * cellWidth;
echo                         const cellY = row * cellHeight;
echo                         doc.rect(cellX, cellY, cellWidth, cellHeight);
echo                     }
echo                     doc.setLineWidth(0.5);
echo                     for (let i = 0; i < actualWaypoints; i++) {
echo                         const wp = waypoints[startIndex + i];
echo                         const row = Math.floor(i / cols);
echo                         const col = i % cols;
echo                         const cellX = col * cellWidth;
echo                         const cellY = row * cellHeight;
echo                         const tableRowHeight = cellHeight / 4;
echo                         for (let tableRow = 1; tableRow < 4; tableRow++) {
echo                             const y = cellY + tableRow * tableRowHeight;
echo                             doc.line(cellX, y, cellX + cellWidth, y);
echo                         }
echo                         const isFinish = wp.number === 'F';
echo                         const texts = isFinish ? [
echo                             `Taškas: ${wp.number === 'S' ? 'Startas' : wp.number === 'F' ? 'Finišas' : wp.number}`,
echo                             `Koordinatės: ${convertToUTM(wp.position.lat(), wp.position.lng())}`,
echo                             `Raidė: ${wp.letter}`
echo                         ] : [
echo                             `Taškas: ${wp.number === 'S' ? 'Startas' : wp.number === 'F' ? 'Finišas' : wp.number}`,
echo                             `Koordinatės: ${convertToUTM(wp.position.lat(), wp.position.lng())}`,
echo                             `Raidė: ${wp.letter}`,
echo                             `Sekančio taško koordinatės: ${i < waypoints.length - 1 ? convertToUTM(waypoints[startIndex + i + 1].position.lat(), waypoints[startIndex + i + 1].position.lng()) : 'N/A'}`
echo                         ];
echo                         doc.setFontSize(12);
echo                         for (let textRow = 0; textRow < texts.length; textRow++) {
echo                             const text = texts[textRow];
echo                             const textX = cellX + 5;
echo                             const textY = cellY + (textRow + 1) * tableRowHeight - 5;
echo                             doc.text(text, textX, textY);
echo                         }
echo                     }
echo                 }
echo                 const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\..+/, '').replace('T', '_');
echo                 const fileName = `route_points_${timestamp}.pdf`;
echo                 doc.save(fileName);
echo                 showStatus(`PDF saved as ${fileName}`, 'success');
echo                 console.log('PDF generated successfully');
echo             } catch (error) {
echo                 console.error('Error generating PDF:', error);
echo                 showStatus('Error generating PDF: ' + error.message, 'error');
echo             }
echo         }
echo         function convertToUTM(latitude, longitude) {
echo             const zone = Math.floor((longitude + 180) / 6) + 1;
echo             const utm = convertWGS84ToUTM(latitude, longitude, zone);
echo             const easting = utm[0];
echo             const northing = utm[1];
echo             const eastingDigits = Math.abs(Math.floor(easting)) % 100000;
echo             const eastingLast4 = Math.floor(eastingDigits / 10) % 10000;
echo             const northingDigits = Math.abs(Math.floor(northing)) % 100000;
echo             const northingLast4 = Math.floor(northingDigits / 10) % 10000;
echo             return `${eastingLast4.toString().padStart(4, '0')} ${northingLast4.toString().padStart(4, '0')}`;
echo         }
echo         function convertWGS84ToUTM(latitude, longitude, zone) {
echo             const a = 6378137.0;
echo             const e = 0.0818191908;
echo             const e1sq = 0.006739497;
echo             const k0 = 0.9996;
echo             const E0 = 500000.0;
echo             const N0 = 0.0;
echo             const lat = latitude * Math.PI / 180;
echo             const lon = longitude * Math.PI / 180;
echo             const lon0 = ((zone - 1) * 6 - 180 + 3) * Math.PI / 180;
echo             const dLon = lon - lon0;
echo             const M = a * ((1 - Math.pow(e, 2) / 4 - 3 * Math.pow(e, 4) / 64 - 5 * Math.pow(e, 6) / 256) * lat
echo                     - (3 * Math.pow(e, 2) / 8 + 3 * Math.pow(e, 4) / 32 + 45 * Math.pow(e, 6) / 1024) * Math.sin(2 * lat)
echo                     + (15 * Math.pow(e, 4) / 256 + 45 * Math.pow(e, 6) / 1024) * Math.sin(4 * lat)
echo                     - (35 * Math.pow(e, 6) / 3072) * Math.sin(6 * lat));
echo             const nu = a / Math.sqrt(1 - Math.pow(e * Math.sin(lat), 2));
echo             const rho = nu * (1 - Math.pow(e, 2)) / (1 - Math.pow(e * Math.sin(lat), 2));
echo             const T = Math.pow(Math.tan(lat), 2);
echo             const C = e1sq * Math.pow(Math.cos(lat), 2);
echo             const A_coeff = dLon * Math.cos(lat);
echo             const easting = k0 * nu * (A_coeff + (1 - T + C) * Math.pow(A_coeff, 3) / 6
echo                     + (5 - 18 * T + Math.pow(T, 2) + 72 * C - 58 * e1sq) * Math.pow(A_coeff, 5) / 120) + E0;
echo             const northing = k0 * (M + nu * Math.tan(lat) * (Math.pow(A_coeff, 2) / 2
echo                     + (5 - T + 9 * C + 4 * Math.pow(C, 2)) * Math.pow(A_coeff, 4) / 24
echo                     + (61 - 58 * T + Math.pow(T, 2) + 600 * C - 330 * e1sq) * Math.pow(A_coeff, 6) / 720)) + N0;
echo             return [easting, northing];
echo         }
echo         function showStatus(message, type = 'info') {
echo             const statusEl = document.getElementById('statusMessage');
echo             statusEl.textContent = message;
echo             statusEl.style.display = 'block';
echo             statusEl.style.background = type === 'error' ? 'rgba(220, 53, 69, 0.9)' : 
echo                                        type === 'warning' ? 'rgba(255, 193, 7, 0.9)' : 
echo                                        type === 'success' ? 'rgba(40, 167, 69, 0.9)' : 
echo                                        'rgba(0, 0, 0, 0.8)';
echo             setTimeout(() => {
echo                 statusEl.style.display = 'none';
echo             }, 3000);
echo         }
echo         document.addEventListener('DOMContentLoaded', function() {
echo             enableButtons(false);
echo         });
echo     ^</script^>
echo ^</body^>
echo ^</html^>
) > "RouteMaker_Web_Portable\route_maker_standalone.html"

:: Create README for portable version
(
echo # Orientation Route Maker - Portable Web Version
echo.
echo This is a portable web version of the Orientation Route Maker application.
echo.
echo ## How to Use
echo.
echo ### Option 1: Standalone HTML (Recommended)
echo 1. Double-click `route_maker_standalone.html`
echo 2. The application will open in your default browser
echo 3. Start creating your route by clicking on the map!
echo.
echo ### Option 2: Web Server (If Python is available)
echo 1. Double-click `start_web_app.bat`
echo 2. The application will open in your default browser
echo.
echo ## Features
echo - Interactive Google Maps
echo - Click to add waypoints (S for Start, numbers for middle, F for Finish)
echo - Zoom controls
echo - Route inversion
echo - Undo/Redo functionality
echo - PDF generation with UTM coordinates
echo - Real-time coordinate display
echo.
echo ## Requirements
echo - Modern web browser (Chrome, Firefox, Safari, Edge)
echo - Internet connection (for Google Maps)
echo.
echo ## Files
echo - `route_maker_standalone.html` - Standalone version (works without server)
echo - `start_web_app.bat` - Web server version (requires Python)
echo - `web/` - Web server files
echo.
echo The standalone HTML version is recommended as it requires no additional software.
) > "RouteMaker_Web_Portable\README.txt"

echo.
echo ============================================================
echo    Portable Web Version Created Successfully!
echo ============================================================
echo.
echo Created folder: RouteMaker_Web_Portable
echo.
echo To use the application:
echo 1. Go to the RouteMaker_Web_Portable folder
echo 2. Double-click route_maker_standalone.html
echo 3. The application will open in your browser
echo.
echo This version works on any Windows computer without requiring
echo Python or any other software to be installed!
echo.
pause
