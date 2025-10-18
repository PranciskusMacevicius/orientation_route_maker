// Import mgrs library for MGRS conversion
const mgrs = require('mgrs');

// Global types
interface Waypoint {
    position: google.maps.LatLng;
    number: string;
    letter: string;
    marker: google.maps.Marker;
    nextPoint?: Waypoint;
}

interface WaypointData {
    position: google.maps.LatLng;
    number: string;
    letter: string;
}

interface PDFWaypointData {
    number: string;
    coordinates: string;
    letter: string;
    nextCoordinates: string | null;
    isFinish: boolean;
}

// Global variables
let map: google.maps.Map;
let waypoints: Waypoint[] = [];
let polyline: google.maps.Polyline | null = null;
let distanceLabels: google.maps.Marker[] = [];
let isMapReady: boolean = false;
let redoHistory: WaypointData[] = [];
let undoCount: number = 0;
const MAX_HISTORY_SIZE: number = 20;
let showDistances: boolean = true;
let userLocation: google.maps.LatLng | null = null;
let userLocationMarker: google.maps.Marker | null = null;
let isFirstLocation: boolean = true;
let locationRetryInterval: number | null = null;
let retryCount: number = 0;

// Default coordinates for Rukla, Lithuania
const DEFAULT_LATITUDE: number = 55.030180;
const DEFAULT_LONGITUDE: number = 24.370464;

// Initialize the map with infinite retry logic
let mapInitAttempts: number = 0;

function initMap(): void {
    try {
        mapInitAttempts++;
        console.log('Initializing Google Maps... (attempt ' + mapInitAttempts + ')');

        // Check if Google Maps is available
        if (typeof google === 'undefined' || !google.maps) {
            throw new Error('Google Maps API not loaded');
        }

        map = new google.maps.Map(document.getElementById('map') as HTMLElement, {
            center: { lat: DEFAULT_LATITUDE, lng: DEFAULT_LONGITUDE },
            zoom: 13,
            mapTypeId: google.maps.MapTypeId.HYBRID,
            draggable: false,
            zoomControl: false,
            scrollwheel: false,
            disableDoubleClickZoom: true,
            clickableIcons: false
        });

        // Add click listener for waypoints
        map.addListener('click', function (event: google.maps.MapMouseEvent) {
            if (!isMapReady) {
                console.log('Map not ready - click ignored');
                return;
            }
            if (event.latLng) {
                addWaypoint(event.latLng);
            }
        });

        // Add mouse move listener for coordinate display
        map.addListener('mousemove', function (event: google.maps.MapMouseEvent) {
            if (event.latLng) {
                const lat = event.latLng.lat();
                const lng = event.latLng.lng();
                const utmCoords = convertToUTM(lat, lng);
                const coordinateDisplay = document.getElementById('coordinateDisplay');
                if (coordinateDisplay) {
                    coordinateDisplay.textContent = utmCoords;
                }
            }
        });

        // Add idle listener to ensure map is fully loaded
        map.addListener('idle', function () {
            console.log('Google Maps fully loaded and ready');
            isMapReady = true;
            onMapReady();
        });

        console.log('Google Maps initialized successfully');

    } catch (error) {
        console.error('Error initializing Google Maps:', error);
        console.log('Retrying map initialization in 2 seconds...');
        showStatus('Google Maps failed to load. Retrying... (attempt ' + mapInitAttempts + ')', 'warning');
        setTimeout(initMap, 2000);
    }
}

// Add timeout for map loading with infinite retry
window.addEventListener('load', function () {
    setTimeout(function () {
        if (!isMapReady) {
            console.log('Map loading timeout - retrying...');
            showStatus('Google Maps is taking longer than expected. Retrying...', 'warning');
            initMap();
        }
    }, 6000); // 6 second timeout
});

// Fallback initialization if Google Maps doesn't load
setTimeout(function () {
    if (!isMapReady && mapInitAttempts === 0) {
        console.log('Google Maps callback not called - attempting manual initialization');
        if (typeof google !== 'undefined' && google.maps) {
            initMap();
        } else {
            showStatus('Google Maps API not available. Please check your internet connection.', 'error');
            const loading = document.getElementById('loading');
            if (loading) {
                loading.textContent = 'Google Maps API not available - Check Internet';
            }
        }
    }
}, 3000); // 3 second fallback

function onMapReady(): void {
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
    const loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = 'none';
    }

    // Enable all buttons
    enableButtons(true);

    // Start automatic location tracking
    startLocationTracking();
}

function enableButtons(enabled: boolean): void {
    const buttons = ['zoomInBtn', 'zoomOutBtn', 'resetBtn', 'invertBtn', 'undoBtn', 'redoBtn', 'toggleDistancesBtn', 'pdfBtn', 'centerUserBtn', 'centerDefaultBtn'];
    buttons.forEach(btnId => {
        const btn = document.getElementById(btnId);
        if (btn) {
            (btn as HTMLButtonElement).disabled = !enabled;
        }
    });
}

function addWaypoint(latLng: google.maps.LatLng): void {
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

function generateRandomLetter(): string {
    const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    return letters.charAt(Math.floor(Math.random() * letters.length));
}

function calculateDistance(point1: google.maps.LatLng, point2: google.maps.LatLng): number {
    return google.maps.geometry.spherical.computeDistanceBetween(point1, point2);
}

function addDistanceLabels(): void {
    for (let i = 0; i < waypoints.length - 1; i++) {
        const point1 = waypoints[i].position;
        const point2 = waypoints[i + 1].position;
        const distance = calculateDistance(point1, point2);

        // Calculate midpoint for label position
        const midLat = (point1.lat() + point2.lat()) / 2;
        const midLng = (point1.lng() + point2.lng()) / 2;

        // Format distance (meters to appropriate unit)
        let distanceText: string;
        if (distance < 1000) {
            distanceText = Math.round(distance) + 'm';
        } else {
            distanceText = (distance / 1000).toFixed(1) + 'km';
        }

        // Create distance label
        const label = new google.maps.Marker({
            position: { lat: midLat, lng: midLng },
            map: map,
            icon: {
                url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                    <svg width="60" height="30" xmlns="http://www.w3.org/2000/svg">
                        <rect width="60" height="30" fill="white" stroke="red" stroke-width="2" rx="5"/>
                        <text x="30" y="20" text-anchor="middle" font-family="Arial" font-size="12" font-weight="bold" fill="red">${distanceText}</text>
                    </svg>
                `),
                scaledSize: new google.maps.Size(60, 30),
                anchor: new google.maps.Point(30, 15)
            },
            zIndex: 1000
        });

        distanceLabels.push(label);
    }
}

function toggleDistances(): void {
    showDistances = !showDistances;
    updateConnections();

    const btn = document.getElementById('toggleDistancesBtn');
    if (btn) {
        btn.textContent = showDistances ? 'Hide Distances' : 'Show Distances';
        btn.style.background = showDistances ? 'rgba(255, 255, 255, 0.9)' : 'rgba(255, 255, 255, 0.5)';
    }
}

function updateConnections(): void {
    if (polyline) {
        polyline.setMap(null);
    }

    // Clear existing distance labels
    distanceLabels.forEach(label => label.setMap(null));
    distanceLabels = [];

    if (waypoints.length > 1) {
        const path = waypoints.map(function (wp) { return wp.position; });
        polyline = new google.maps.Polyline({
            path: path,
            geodesic: true,
            strokeColor: '#FF0000',
            strokeOpacity: 1.0,
            strokeWeight: 2
        });
        polyline.setMap(map);

        // Add distance labels if enabled
        if (showDistances) {
            addDistanceLabels();
        }
    }
}

function clearWaypoints(): void {
    waypoints.forEach(function (wp) {
        wp.marker.setMap(null);
    });
    if (polyline) polyline.setMap(null);

    // Clear distance labels
    distanceLabels.forEach(label => label.setMap(null));
    distanceLabels = [];

    waypoints = [];
    clearHistory();
    console.log('All waypoints cleared');
}

function invertRoute(): void {
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

function undo(): void {
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

function redo(): void {
    if (redoHistory.length > 0) {
        const waypointData = redoHistory.pop();
        if (waypointData) {
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

            // Update waypoint numbering after adding back
            updateWaypointNumbering();
            updateConnections();
            undoCount--;
            console.log('Redo: restored waypoint', waypointData.number, 'undo count:', undoCount);
        }
    } else {
        console.log('Nothing to redo');
    }
}

function updateWaypointNumbering(): void {
    if (waypoints.length === 0) return;

    for (let i = 0; i < waypoints.length; i++) {
        const wp = waypoints[i];
        if (i === 0) {
            wp.number = 'S';
        } else if (i === waypoints.length - 1) {
            wp.number = 'F';
        } else {
            wp.number = i.toString();
        }
        wp.marker.setLabel(wp.number);
    }
}

function clearHistory(): void {
    redoHistory = [];
    undoCount = 0;
    console.log('History cleared, undo count reset to 0');
}

function generatePDF(): void {
    if (waypoints.length === 0) {
        showStatus('No waypoints to generate PDF. Please add some points to the map first.', 'warning');
        return;
    }

    try {
        // Check if pdfmake is available, fallback to jsPDF
        if (typeof (window as any).pdfMake === 'undefined') {
            console.log('pdfmake not available, falling back to jsPDF');
            generatePDFWithJsPDF();
            return;
        }

        // Update next point coordinates for each waypoint
        for (let i = 0; i < waypoints.length; i++) {
            if (i < waypoints.length - 1) {
                waypoints[i].nextPoint = waypoints[i + 1];
            }
        }

        // Create waypoint data for PDF
        const waypointData: PDFWaypointData[] = waypoints.map((wp, index) => {
            const isFinish = wp.number === 'F';
            const nextPointCoords = index < waypoints.length - 1 ?
                convertToUTM(waypoints[index + 1].position.lat(), waypoints[index + 1].position.lng()) :
                'N/A';

            return {
                number: wp.number === 'S' ? 'Startas' : wp.number === 'F' ? 'Finišas' : wp.number,
                coordinates: convertToUTM(wp.position.lat(), wp.position.lng()),
                letter: wp.letter,
                nextCoordinates: isFinish ? null : nextPointCoords,
                isFinish: isFinish
            };
        });

        // Create PDF document definition
        const docDefinition = {
            pageSize: 'A4',
            pageMargins: [0, 0, 0, 0], // No margins like original
            content: [] as any[],
            styles: {
                cellHeader: {
                    fontSize: 14,
                    bold: true,
                    alignment: 'center' as const,
                    margin: [0, 5, 0, 5]
                },
                cellText: {
                    fontSize: 16,
                    alignment: 'center' as const,
                    margin: [0, 5, 0, 5]
                },
                cellBorder: {
                    border: [true, true, true, true],
                    borderColor: '#000000'
                }
            }
        };

        // Create grid layout (2x4 = 8 waypoints per page with larger cells)
        const POINTS_PER_PAGE = 12;
        const totalPages = Math.ceil(waypointData.length / POINTS_PER_PAGE);

        for (let pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            const startIndex = pageIndex * POINTS_PER_PAGE;
            const endIndex = Math.min(startIndex + POINTS_PER_PAGE, waypointData.length);
            const pageWaypoints = waypointData.slice(startIndex, endIndex);

            // Create table for this page
            const tableBody: any[] = [];

            // Create rows only for existing waypoints (2 per row)
            for (let i = 0; i < pageWaypoints.length; i += 2) {
                const tableRow: any[] = [];

                // Add first waypoint in row
                const wp1 = pageWaypoints[i];
                if (wp1) {
                    const cellContent1: any[] = [];

                    cellContent1.push({
                        text: `Taškas: ${wp1.number}`,
                        style: 'cellText',
                        alignment: 'center'
                    });

                    cellContent1.push({
                        text: `Koordinatės: ${wp1.coordinates}`,
                        style: 'cellText',
                        alignment: 'center'
                    });

                    cellContent1.push({
                        text: `Raidė: ${wp1.letter}`,
                        style: 'cellText',
                        alignment: 'center'
                    });

                    if (!wp1.isFinish) {
                        cellContent1.push({
                            text: `Sekančio taško koordinatės:\n${wp1.nextCoordinates}`,
                            style: 'cellText',
                            alignment: 'center'
                        });
                    } else {
                        // Add the same line structure for finish point to match height
                        cellContent1.push({
                            text: `Sekančio taško koordinatės:\n N/A`,
                            style: 'cellText',
                            alignment: 'center',
                            color: 'white'
                        });
                    }

                    tableRow.push({
                        stack: cellContent1,
                        border: [true, true, true, true],
                        fillColor: '#ffffff'
                    });
                }

                // Add second waypoint in row (if exists)
                const wp2 = pageWaypoints[i + 1];
                if (wp2) {
                    const cellContent2: any[] = [];

                    cellContent2.push({
                        text: `Taškas: ${wp2.number}`,
                        style: 'cellText',
                        alignment: 'center'
                    });

                    cellContent2.push({
                        text: `Koordinatės: ${wp2.coordinates}`,
                        style: 'cellText',
                        alignment: 'center'
                    });

                    cellContent2.push({
                        text: `Raidė: ${wp2.letter}`,
                        style: 'cellText',
                        alignment: 'center'
                    });

                    if (!wp2.isFinish) {
                        cellContent2.push({
                            text: `Sekančio taško koordinatės:\n ${wp2.nextCoordinates}`,
                            style: 'cellText',
                            alignment: 'center'
                        });
                    } else {
                        // Add the same line structure for finish point to match height
                        cellContent2.push({
                            text: `Sekančio taško koordinatės:\n N/A`,
                            style: 'cellText',
                            alignment: 'center',
                            color: 'white'
                        });
                    }

                    tableRow.push({
                        stack: cellContent2,
                        border: [true, true, true, true],
                        fillColor: '#ffffff'
                    });
                } else {
                    // Add empty cell if no second waypoint to maintain table structure
                    tableRow.push({
                        text: '',
                        border: [false, false, false, false],
                        fillColor: '#ffffff'
                    });
                }

                // Always add row to maintain consistent table structure
                tableBody.push(tableRow);
            }

            // Add table to document
            if (pageIndex > 0) {
                docDefinition.content.push({ text: '', pageBreak: 'before' });
            }

            docDefinition.content.push({
                table: {
                    headerRows: 0,
                    widths: ['50%', '50%'],
                    body: tableBody
                },
                layout: {
                    hLineWidth: function () {
                        return 2; // Thick borders
                    },
                    vLineWidth: function () {
                        return 2; // Thick borders
                    },
                    hLineColor: function () {
                        return '#000000';
                    },
                    vLineColor: function () {
                        return '#000000';
                    }
                }
            });
        }

        // Generate filename with timestamp (local time)
        const now = new Date();
        const timestamp = now.getFullYear().toString() +
            (now.getMonth() + 1).toString().padStart(2, '0') +
            now.getDate().toString().padStart(2, '0') + '_' +
            now.getHours().toString().padStart(2, '0') +
            now.getMinutes().toString().padStart(2, '0') +
            now.getSeconds().toString().padStart(2, '0');
        const fileName = `orientation_${timestamp}.pdf`;

        // Generate and download PDF
        (window as any).pdfMake.createPdf(docDefinition).download(fileName);

        showStatus(`PDF saved as ${fileName}`, 'success');
        console.log('PDF generated successfully with pdfmake');

    } catch (error) {
        console.error('Error generating PDF:', error);
        showStatus('Error generating PDF: ' + (error as Error).message, 'error');
    }
}

// Fallback PDF generation with jsPDF
function generatePDFWithJsPDF(): void {
    try {
        if (typeof (window as any).jspdf === 'undefined') {
            showStatus('PDF libraries not loaded. Please refresh the page and try again.', 'error');
            return;
        }

        const { jsPDF } = (window as any).jspdf;
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
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();
            const cellWidth = pageWidth / cols;
            const cellHeight = pageHeight / 2; // Double the height - use only 2 rows worth of space

            // Debug output
            console.log('PDF Debug - Page:', pageWidth, 'x', pageHeight);
            console.log('PDF Debug - Cell:', cellWidth, 'x', cellHeight);
            console.log('PDF Debug - Grid: 2x4, but using 2 physical rows');

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
                const tableRowHeight = cellHeight / 4;

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

        // Generate filename with timestamp (local time)
        const now = new Date();
        const timestamp = now.getFullYear().toString() +
            (now.getMonth() + 1).toString().padStart(2, '0') +
            now.getDate().toString().padStart(2, '0') + '_' +
            now.getHours().toString().padStart(2, '0') +
            now.getMinutes().toString().padStart(2, '0') +
            now.getSeconds().toString().padStart(2, '0');
        const fileName = `orientation_${timestamp}.pdf`;

        // Save the PDF
        doc.save(fileName);

        showStatus(`PDF saved as ${fileName} (using jsPDF)`, 'success');
        console.log('PDF generated successfully with jsPDF fallback');

    } catch (error) {
        console.error('Error generating PDF with jsPDF:', error);
        showStatus('Error generating PDF: ' + (error as Error).message, 'error');
    }
}

// MGRS coordinate conversion using mgrs library
function convertToUTM(latitude: number, longitude: number): string {
    try {
        // Convert lat/lng to MGRS
        const mgrsString = mgrs.forward([longitude, latitude]);

        // Format to shortened MGRS: LB 3106 9903 (100km square + first 4 digits)
        // The mgrs library returns: 35ULA3106599038
        // We need: LB 3106 9903 (square + first 4 digits of each coordinate)

        if (mgrsString.length >= 15) {
            // Extract 100km square (chars 3-5)
            const square = mgrsString.substring(3, 5);
            // Extract first 4 digits of easting (chars 5-9)
            const easting = mgrsString.substring(5, 9);
            // Extract first 4 digits of northing (chars 10-14)
            const northing = mgrsString.substring(10, 14);

            return `${square} ${easting} ${northing}`;
        }

        return mgrsString;
    } catch (error) {
        console.error('MGRS conversion error:', error);
        // Fallback to simple format if library fails
        return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
    }
}


function showStatus(message: string, type: 'info' | 'error' | 'warning' | 'success' = 'info'): void {
    const statusEl = document.getElementById('statusMessage');
    if (statusEl) {
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
}

function showLocationStatus(message: string, type: 'info' | 'error' | 'warning' | 'success' = 'info'): void {
    const statusEl = document.getElementById('status');
    if (statusEl) {
        statusEl.textContent = message;
        statusEl.className = `status-panel status-${type}`;
        statusEl.style.display = 'flex';
        statusEl.style.visibility = 'visible';
        statusEl.style.opacity = '1';
    }
}

function startLocationRetry(): void {
    if (locationRetryInterval !== null) {
        return; // Already retrying
    }

    retryCount = 0;
    showLocationStatus('Retrying To Get Location...', 'warning');

    locationRetryInterval = window.setInterval(() => {
        retryCount++;
        showLocationStatus(`Retrying To Get Location... (Attempt: ${retryCount})`, 'warning');

        navigator.geolocation.getCurrentPosition(
            (position) => {
                // Success! Stop retrying and start normal tracking
                clearLocationRetry();
                updateUserLocation(position);
                const accuracy = position.coords.accuracy;
                showLocationStatus(`Location Tracking Active (Accuracy: ${Math.round(accuracy)}m)`, 'success');
                startWatchPosition();
            },
            (error) => {
                console.log('Retry attempt failed:', error);
                // Continue retrying
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0 // Force fresh location
            }
        );
    }, 5000); // Retry every 5 seconds
}

function clearLocationRetry(): void {
    if (locationRetryInterval !== null) {
        clearInterval(locationRetryInterval);
        locationRetryInterval = null;
        retryCount = 0;
    }
}

function startWatchPosition(): void {
    navigator.geolocation.watchPosition(
        (position) => updateUserLocation(position),
        (error) => {
            console.log('Location watch error:', error);
            showLocationStatus('Location updates unavailable', 'warning');
            // Start retrying when watchPosition fails
            startLocationRetry();
        },
        {
            enableHighAccuracy: true,
            timeout: 30000,
            maximumAge: 60000
        }
    );
}

// GPS Location functions
function startLocationTracking(): void {
    if (!navigator.geolocation) {
        showStatus('Geolocation is not supported by this browser.', 'error');
        return;
    }

    // Start location tracking with status updates
    showLocationStatus('Getting Location...', 'info');

    // Get initial location with more lenient settings
    navigator.geolocation.getCurrentPosition(
        (position) => {
            updateUserLocation(position);
            showLocationStatus('Location Tracking Active', 'success');
            // Start watching location changes only after successful initial location
            startWatchPosition();
        },
        (error) => {
            // Show user that initial location failed and start retrying
            console.log('Initial location request failed:', error);
            showLocationStatus('Unable To Get Location', 'error');
            startLocationRetry();
        },
        {
            enableHighAccuracy: true,
            timeout: 30000, // Longer timeout
            maximumAge: 60000 // Update every minute
        }
    );
}

function updateUserLocation(position: GeolocationPosition): void {
    const lat = position.coords.latitude;
    const lng = position.coords.longitude;
    userLocation = new google.maps.LatLng(lat, lng);

    // Create or update user location marker
    if (userLocationMarker) {
        userLocationMarker.setMap(null);
    }

    userLocationMarker = new google.maps.Marker({
        position: userLocation,
        map: map,
        title: 'Your Location',
        icon: {
            url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                <svg width="24" height="24" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" fill="#4285F4" stroke="#FFFFFF" stroke-width="2"/>
                    <circle cx="12" cy="12" r="4" fill="#FFFFFF"/>
                </svg>
            `),
            scaledSize: new google.maps.Size(24, 24),
            anchor: new google.maps.Point(12, 12)
        }
    });

    // Show location found message but don't center the map
    if (isFirstLocation) {
        showStatus(`Location found: ${lat.toFixed(6)}, ${lng.toFixed(6)}`, 'success');
        isFirstLocation = false;
    } else {
        console.log('Location updated:', lat, lng);
        // Show that location tracking is working with accuracy
        const accuracy = position.coords.accuracy;
        showLocationStatus(`Location tracking active (accuracy: ${Math.round(accuracy)}m)`, 'success');
    }

    console.log('User location:', lat, lng);
}

function handleLocationError(error: GeolocationPositionError): void {
    let errorMessage = '';
    switch (error.code) {
        case error.PERMISSION_DENIED:
            errorMessage = 'Location permission denied. Please allow location access in your browser settings and try again.';
            break;
        case error.POSITION_UNAVAILABLE:
            errorMessage = 'Location information unavailable. Please check your GPS/network connection.';
            break;
        case error.TIMEOUT:
            errorMessage = 'Location request timed out. Please try again.';
            break;
        default:
            errorMessage = 'Unable to get location. Please try again.';
            break;
    }
    showStatus(errorMessage, 'error');
    console.error('Geolocation error:', error);
}

function centerOnUserLocation(): void {
    if (userLocation) {
        map.setCenter(userLocation);
        map.setZoom(15);
        showStatus('Centered on your location', 'info');
    } else {
        showStatus('No location available. Requesting location...', 'info');
        showLocationStatus('Getting Location...', 'info');
        // Retry getting location
        navigator.geolocation.getCurrentPosition(
            (position) => {
                updateUserLocation(position);
                map.setCenter(userLocation!);
                map.setZoom(15);
                showStatus('Centered on your location', 'success');
                const accuracy = position.coords.accuracy;
                showLocationStatus(`Location tracking active (accuracy: ${Math.round(accuracy)}m)`, 'success');
                startWatchPosition();
            },
            (error) => {
                handleLocationError(error);
                showLocationStatus('Unable To Get Location', 'error');
                startLocationRetry();
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    }
}

function centerOnDefaultLocation(): void {
    const defaultLocation = new google.maps.LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    map.setCenter(defaultLocation);
    map.setZoom(13);
    showStatus('Centered on default location', 'info');
}

// Initialize buttons as disabled
document.addEventListener('DOMContentLoaded', function () {
    enableButtons(false);
});

// Make functions available globally
(window as any).initMap = initMap;
(window as any).clearWaypoints = clearWaypoints;
(window as any).invertRoute = invertRoute;
(window as any).centerOnUserLocation = centerOnUserLocation;
(window as any).centerOnDefaultLocation = centerOnDefaultLocation;
(window as any).undo = undo;
(window as any).redo = redo;
(window as any).generatePDF = generatePDF;
(window as any).toggleDistances = toggleDistances;
