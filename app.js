// Global variables
let map;
let waypoints = [];
let markers = [];
let polyline;
let isMapReady = false;
let undoHistory = [];
let redoHistory = [];
let undoCount = 0;
const MAX_HISTORY_SIZE = 20;

// Default coordinates for Rukla, Lithuania
const DEFAULT_LATITUDE = 55.030180;
const DEFAULT_LONGITUDE = 24.370464;

// Initialize the map
function initMap() {
    try {
        map = new google.maps.Map(document.getElementById('map'), {
            center: { lat: DEFAULT_LATITUDE, lng: DEFAULT_LONGITUDE },
            zoom: 13,
            mapTypeId: google.maps.MapTypeId.SATELLITE,
            draggable: false,
            zoomControl: false,
            scrollwheel: false,
            disableDoubleClickZoom: true,
            clickableIcons: false
        });

        // Add click listener for waypoints
        map.addListener('click', function(event) {
            if (!isMapReady) {
                console.log('Map not ready - click ignored');
                return;
            }
            addWaypoint(event.latLng);
        });

        // Add mouse move listener for coordinate display
        map.addListener('mousemove', function(event) {
            const lat = event.latLng.lat();
            const lng = event.latLng.lng();
            const utmCoords = convertToUTM(lat, lng);
            document.getElementById('coordinateDisplay').textContent = utmCoords;
        });

        console.log('Google Maps initialized');
        isMapReady = true;
        onMapReady();
        
    } catch (error) {
        console.error('Error initializing Google Maps:', error);
        showStatus('Error loading Google Maps. Please check your internet connection.', 'error');
    }
}

function onMapReady() {
    console.log('Map is ready for interaction!');
    
    // Enable map interactions
    map.setOptions({
        draggable: true,
        zoomControl: true,
        scrollwheel: true,
        disableDoubleClickZoom: false,
        clickableIcons: true
    });
    
    // Hide loading message
    document.getElementById('loading').style.display = 'none';
    
    // Enable all buttons
    enableButtons(true);
}

function enableButtons(enabled) {
    const buttons = ['zoomInBtn', 'zoomOutBtn', 'resetBtn', 'invertBtn', 'undoBtn', 'redoBtn', 'pdfBtn'];
    buttons.forEach(btnId => {
        document.getElementById(btnId).disabled = !enabled;
    });
}

function addWaypoint(latLng) {
    const pointNumber = waypoints.length === 0 ? 'S' : 'F';
    
    // Update previous waypoint number if it's not the first
    if (waypoints.length > 1) {
        waypoints[waypoints.length - 1].number = (waypoints.length - 1).toString();
        waypoints[waypoints.length - 1].marker.setLabel(waypoints[waypoints.length - 1].number);
    }

    const marker = new google.maps.Marker({
        position: latLng,
        map: map,
        label: pointNumber
    });

    const randomLetter = generateRandomLetter();

    waypoints.push({
        position: latLng,
        number: pointNumber,
        letter: randomLetter,
        marker: marker
    });

    updateConnections();
    console.log(`Added waypoint ${pointNumber} at ${latLng.lat()}, ${latLng.lng()}`);
}

function generateRandomLetter() {
    const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    return letters.charAt(Math.floor(Math.random() * letters.length));
}

function updateConnections() {
    if (polyline) {
        polyline.setMap(null);
    }
    
    if (waypoints.length > 1) {
        const path = waypoints.map(function(wp) { return wp.position; });
        polyline = new google.maps.Polyline({
            path: path,
            geodesic: true,
            strokeColor: '#FF0000',
            strokeOpacity: 1.0,
            strokeWeight: 2
        });
        polyline.setMap(map);
    }
}

function clearWaypoints() {
    waypoints.forEach(function(wp) {
        wp.marker.setMap(null);
    });
    if (polyline) polyline.setMap(null);
    waypoints = [];
    clearHistory();
    console.log('All waypoints cleared');
}

function invertRoute() {
    if (waypoints.length < 2) return;
    
    waypoints.reverse();
    for (let i = 0; i < waypoints.length; i++) {
        if (i === 0) {
            waypoints[i].number = 'S';
            waypoints[i].marker.setLabel('S');
        } else if (i === waypoints.length - 1) {
            waypoints[i].number = 'F';
            waypoints[i].marker.setLabel('F');
        } else {
            waypoints[i].number = i.toString();
            waypoints[i].marker.setLabel(i.toString());
        }
    }
    updateConnections();
    console.log('Route inverted');
}

function zoomIn() {
    if (map) {
        map.setZoom(map.getZoom() + 1);
    }
}

function zoomOut() {
    if (map) {
        map.setZoom(map.getZoom() - 1);
    }
}

function undo() {
    console.log('Undo method called, undoCount:', undoCount);
    
    if (undoCount >= MAX_HISTORY_SIZE) {
        console.log('Undo limit reached (20 operations)');
        showStatus('Undo limit reached (20 operations)', 'warning');
        return;
    }
    
    if (waypoints.length > 0) {
        const lastWaypoint = waypoints[waypoints.length - 1];
        console.log('Removing waypoint:', lastWaypoint.number);
        
        // Store for redo
        redoHistory.push({
            position: lastWaypoint.position,
            number: lastWaypoint.number,
            letter: lastWaypoint.letter
        });
        
        lastWaypoint.marker.setMap(null);
        waypoints.pop();
        updateConnections();
        
        // Update waypoint numbers
        for (let i = 0; i < waypoints.length; i++) {
            if (i === 0) {
                waypoints[i].number = 'S';
                waypoints[i].marker.setLabel('S');
            } else if (i === waypoints.length - 1) {
                waypoints[i].number = 'F';
                waypoints[i].marker.setLabel('F');
            } else {
                waypoints[i].number = i.toString();
                waypoints[i].marker.setLabel(i.toString());
            }
        }
        
        undoCount++;
        console.log('Waypoint removed, new length:', waypoints.length, 'undo count:', undoCount);
    } else {
        console.log('No waypoints to remove');
    }
}

function redo() {
    if (redoHistory.length > 0) {
        const waypointData = redoHistory.pop();
        
        const marker = new google.maps.Marker({
            position: waypointData.position,
            map: map,
            label: waypointData.number
        });

        waypoints.push({
            position: waypointData.position,
            number: waypointData.number,
            letter: waypointData.letter,
            marker: marker
        });
        
        updateConnections();
        undoCount--;
        console.log('Redo: restored waypoint', waypointData.number, 'undo count:', undoCount);
    } else {
        console.log('Nothing to redo');
    }
}

function clearHistory() {
    undoHistory = [];
    redoHistory = [];
    undoCount = 0;
    console.log('History cleared, undo count reset to 0');
}

function generatePDF() {
    if (waypoints.length === 0) {
        showStatus('No waypoints to generate PDF. Please add some points to the map first.', 'warning');
        return;
    }
    
    try {
        // Create new PDF document
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        // Update next point coordinates for each waypoint
        for (let i = 0; i < waypoints.length; i++) {
            if (i < waypoints.length - 1) {
                waypoints[i].nextPoint = waypoints[i + 1];
            }
        }
        
        const POINTS_PER_PAGE = 8; // 2x4 grid
        const totalPages = Math.ceil(waypoints.length / POINTS_PER_PAGE);
        
        for (let pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            if (pageIndex > 0) {
                doc.addPage();
            }
            
            const startIndex = pageIndex * POINTS_PER_PAGE;
            const endIndex = Math.min(startIndex + POINTS_PER_PAGE, waypoints.length);
            const actualWaypoints = endIndex - startIndex;
            
            // Grid dimensions
            const cols = 2;
            const rows = 4;
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();
            const cellWidth = pageWidth / cols;
            const cellHeight = pageHeight / 2; // Double the height - use only 2 rows worth of space
            
            // Debug: log the dimensions
            console.log('Page dimensions:', pageWidth, 'x', pageHeight);
            console.log('Cell dimensions:', cellWidth, 'x', cellHeight);
            console.log('Grid:', cols, 'x', rows);
            
            // Draw borders
            doc.setLineWidth(2);
            for (let i = 0; i < actualWaypoints; i++) {
                const logicalRow = Math.floor(i / cols);
                const col = i % cols;
                
                // Map 4 logical rows to 2 physical rows
                const physicalRow = Math.floor(logicalRow / 2);
                
                const cellX = col * cellWidth;
                const cellY = physicalRow * cellHeight;
                
                // Draw cell border
                doc.rect(cellX, cellY, cellWidth, cellHeight);
            }
            
            // Draw internal lines and add text
            doc.setLineWidth(0.5);
            for (let i = 0; i < actualWaypoints; i++) {
                const wp = waypoints[startIndex + i];
                const logicalRow = Math.floor(i / cols);
                const col = i % cols;
                
                // Map 4 logical rows to 2 physical rows
                const physicalRow = Math.floor(logicalRow / 2);
                
                const cellX = col * cellWidth;
                const cellY = physicalRow * cellHeight;
                const tableRowHeight = cellHeight / 4; // 4 internal rows per cell
                
                // Draw internal lines
                for (let tableRow = 1; tableRow < 4; tableRow++) {
                    const y = cellY + tableRow * tableRowHeight;
                    doc.line(cellX, y, cellX + cellWidth, y);
                }
                
                // Add text content
                const isFinish = wp.number === 'F';
                const texts = isFinish ? [
                    `Taškas: ${wp.number === 'S' ? 'Startas' : wp.number === 'F' ? 'Finišas' : wp.number}`,
                    `Koordinatės: ${convertToUTM(wp.position.lat(), wp.position.lng())}`,
                    `Raidė: ${wp.letter}`
                ] : [
                    `Taškas: ${wp.number === 'S' ? 'Startas' : wp.number === 'F' ? 'Finišas' : wp.number}`,
                    `Koordinatės: ${convertToUTM(wp.position.lat(), wp.position.lng())}`,
                    `Raidė: ${wp.letter}`,
                    `Sekančio taško koordinatės: ${i < waypoints.length - 1 ? convertToUTM(waypoints[startIndex + i + 1].position.lat(), waypoints[startIndex + i + 1].position.lng()) : 'N/A'}`
                ];
                
                // Draw text
                doc.setFontSize(12);
                for (let textRow = 0; textRow < texts.length; textRow++) {
                    const text = texts[textRow];
                    const textX = cellX + 5;
                    const textY = cellY + (textRow + 1) * tableRowHeight - 5;
                    doc.text(text, textX, textY);
                }
            }
        }
        
        // Generate filename with timestamp
        const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\..+/, '').replace('T', '_');
        const fileName = `route_points_${timestamp}.pdf`;
        
        // Save the PDF
        doc.save(fileName);
        
        showStatus(`PDF saved as ${fileName}`, 'success');
        console.log('PDF generated successfully');
        
    } catch (error) {
        console.error('Error generating PDF:', error);
        showStatus('Error generating PDF: ' + error.message, 'error');
    }
}

// UTM coordinate conversion (simplified implementation)
function convertToUTM(latitude, longitude) {
    // Calculate the correct UTM zone
    const zone = Math.floor((longitude + 180) / 6) + 1;
    
    const utm = convertWGS84ToUTM(latitude, longitude, zone);
    const easting = utm[0];
    const northing = utm[1];

    // Get digits 2-5 from the right for easting
    const eastingDigits = Math.abs(Math.floor(easting)) % 100000;
    const eastingLast4 = Math.floor(eastingDigits / 10) % 10000;
    
    // Get digits 2-5 from the right for northing
    const northingDigits = Math.abs(Math.floor(northing)) % 100000;
    const northingLast4 = Math.floor(northingDigits / 10) % 10000;

    return `${eastingLast4.toString().padStart(4, '0')} ${northingLast4.toString().padStart(4, '0')}`;
}

function convertWGS84ToUTM(latitude, longitude, zone) {
    // WGS84 ellipsoid parameters
    const a = 6378137.0;              // Semi-major axis (meters)
    const e = 0.0818191908;           // First eccentricity
    const e1sq = 0.006739497;         // e1 squared
    
    // UTM parameters
    const k0 = 0.9996;                // Scale factor
    const E0 = 500000.0;              // False easting
    const N0 = 0.0;                   // False northing (Northern hemisphere)
    
    // Convert to radians
    const lat = latitude * Math.PI / 180;
    const lon = longitude * Math.PI / 180;
    const lon0 = ((zone - 1) * 6 - 180 + 3) * Math.PI / 180; // Central meridian
    
    const dLon = lon - lon0;
    
    // Calculate M (meridional arc)
    const M = a * ((1 - Math.pow(e, 2) / 4 - 3 * Math.pow(e, 4) / 64 - 5 * Math.pow(e, 6) / 256) * lat
            - (3 * Math.pow(e, 2) / 8 + 3 * Math.pow(e, 4) / 32 + 45 * Math.pow(e, 6) / 1024) * Math.sin(2 * lat)
            + (15 * Math.pow(e, 4) / 256 + 45 * Math.pow(e, 6) / 1024) * Math.sin(4 * lat)
            - (35 * Math.pow(e, 6) / 3072) * Math.sin(6 * lat));
    
    // Calculate other parameters
    const nu = a / Math.sqrt(1 - Math.pow(e * Math.sin(lat), 2));
    const rho = nu * (1 - Math.pow(e, 2)) / (1 - Math.pow(e * Math.sin(lat), 2));
    const T = Math.pow(Math.tan(lat), 2);
    const C = e1sq * Math.pow(Math.cos(lat), 2);
    const A_coeff = dLon * Math.cos(lat);
    
    // Calculate easting
    const easting = k0 * nu * (A_coeff + (1 - T + C) * Math.pow(A_coeff, 3) / 6
            + (5 - 18 * T + Math.pow(T, 2) + 72 * C - 58 * e1sq) * Math.pow(A_coeff, 5) / 120) + E0;
    
    // Calculate northing
    const northing = k0 * (M + nu * Math.tan(lat) * (Math.pow(A_coeff, 2) / 2
            + (5 - T + 9 * C + 4 * Math.pow(C, 2)) * Math.pow(A_coeff, 4) / 24
            + (61 - 58 * T + Math.pow(T, 2) + 600 * C - 330 * e1sq) * Math.pow(A_coeff, 6) / 720)) + N0;
    
    return [easting, northing];
}

function showStatus(message, type = 'info') {
    const statusEl = document.getElementById('statusMessage');
    statusEl.textContent = message;
    statusEl.style.display = 'block';
    statusEl.style.background = type === 'error' ? 'rgba(220, 53, 69, 0.9)' : 
                               type === 'warning' ? 'rgba(255, 193, 7, 0.9)' : 
                               type === 'success' ? 'rgba(40, 167, 69, 0.9)' : 
                               'rgba(0, 0, 0, 0.8)';
    
    setTimeout(() => {
        statusEl.style.display = 'none';
    }, 3000);
}

// Initialize buttons as disabled
document.addEventListener('DOMContentLoaded', function() {
    enableButtons(false);
});
