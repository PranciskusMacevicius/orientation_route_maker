import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GoogleMapsPanel extends JPanel {
    private JFXPanel jfxPanel;
    private WebView webView;
    private WebEngine webEngine;
    private ActionManager actionManager;
    private boolean isInitialized = false;
    private boolean isMapReady = false;
    private long lastWaypointTime = 0;
    private Runnable mapReadyCallback;
    
    // History management for undo/redo - track both undo and redo operations
    private java.util.List<WayPoint> undoHistory = new java.util.ArrayList<>(); // Waypoints that can be undone
    private java.util.List<WayPoint> redoHistory = new java.util.ArrayList<>(); // Waypoints that can be redone
    private int undoCount = 0; // Track number of undo operations
    private static final int MAX_HISTORY_SIZE = 20;
    
    // You'll need to get your own Google Maps API key from Google Cloud Console 
    // Replace YOUR_API_KEY_HERE with your actual Google Maps API key
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBxUx71FnVZj95ze2B7gs99NSzdIl-957k"; // Add your API key here
    
    public GoogleMapsPanel(ActionManager actionManager) {
        this.actionManager = actionManager;
        setLayout(new BorderLayout());
        initializeJavaFX();
    }
    
    private void initializeJavaFX() {
        // Initialize JavaFX toolkit
        Platform.setImplicitExit(false);
        
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
        
        Platform.runLater(() -> {
            createWebView();
            loadGoogleMaps();
        });
    }
    
    private void createWebView() {
        webView = new WebView();
        webEngine = webView.getEngine();
        
        // Enable JavaScript
        webEngine.setJavaScriptEnabled(true);
        
        // Set up JavaScript bridge for communication between Java and JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state changed to: " + newState);
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    // Set up the bridge between Java and JavaScript
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaApp", this);
                    window.setMember("isMapReady", false);
                    isInitialized = true;
                    System.out.println("Google Maps loaded successfully!");
                    
                    // Wait 2 seconds then enable interactions
                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
                            Platform.runLater(() -> {
                                isMapReady = true;
                                onMapReady();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } catch (Exception e) {
                    System.err.println("Error setting up JavaScript bridge: " + e.getMessage());
                }
            } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("WebEngine failed to load content");
                Throwable exception = webEngine.getLoadWorker().getException();
                if (exception != null) {
                    System.err.println("Load exception: " + exception.getMessage());
                }
            }
        });
        
        // Add console message listener for JavaScript errors
        webEngine.setOnAlert(event -> {
            System.out.println("JavaScript Alert: " + event.getData());
        });
        
        Scene scene = new Scene(webView);
        jfxPanel.setScene(scene);
    }
    
    private void loadGoogleMaps() {
        String htmlContent = generateGoogleMapsHTML();
        webEngine.loadContent(htmlContent);
    }
    
    private String generateGoogleMapsHTML() {
        return "<!DOCTYPE html>" +
               "<html><head>" +
               "<meta charset='utf-8'>" +
               "<title>RouteMaker Google Maps</title>" +
               "<style>" +
               "html, body { height: 100%; margin: 0; padding: 0; }" +
               "#map { height: 100%; }" +
               "</style></head><body>" +
               "<div id='map'></div>" +
               "<script>" +
               "var map;" +
               "var waypoints = [];" +
               "var markers = [];" +
               "var polyline;" +
               "" +
               "function initMap() {" +
               "  try {" +
               "    map = new google.maps.Map(document.getElementById('map'), {" +
               "      center: { lat: 55.030180, lng: 24.370464 }," +
               "      zoom: 13," +
               "      mapTypeId: google.maps.MapTypeId.SATELLITE," +
               "      draggable: false," +
               "      zoomControl: false," +
               "      scrollwheel: false," +
               "      disableDoubleClickZoom: true," +
               "      clickableIcons: false" +
               "    });" +
               "" +
               "    map.addListener('click', function(event) {" +
               "      if (!window.isMapReady) {" +
               "        console.log('Map not ready - click ignored');" +
               "        return;" +
               "      }" +
               "      addWaypoint(event.latLng);" +
               "    });" +
               "" +
               "" +
               "    console.log('Google Maps initialized');" +
               "    window.isMapReady = true;" +
               "    if (window.javaApp) {" +
               "      window.javaApp.onMapReady();" +
               "    }" +
               "" +
               "" +
               "  } catch (error) {" +
               "    console.error('Error initializing Google Maps:', error);" +
               "  }" +
               "}" +
               "" +
               "function addWaypoint(latLng) {" +
               "  if (window.javaApp) {" +
               "    window.javaApp.addWaypoint(latLng.lat(), latLng.lng());" +
               "  }" +
               "}" +
               "" +
               "function addWaypointToMap(latLng) {" +
               "  var pointNumber = waypoints.length === 0 ? 'S' : 'F';" +
               "  if (waypoints.length > 1) {" +
               "    waypoints[waypoints.length - 1].number = (waypoints.length - 1).toString();" +
               "    waypoints[waypoints.length - 1].marker.setLabel(waypoints[waypoints.length - 1].number);" +
               "  }" +
               "" +
               "  var marker = new google.maps.Marker({" +
               "    position: latLng," +
               "    map: map," +
               "    label: pointNumber" +
               "  });" +
               "" +
               "  var randomLetter = generateRandomLetter();" +
               "" +
               "  waypoints.push({" +
               "    position: latLng," +
               "    number: pointNumber," +
               "    letter: randomLetter," +
               "    marker: marker" +
               "  });" +
               "" +
               "  updateConnections();" +
               "}" +
               "" +
               "function generateRandomLetter() {" +
               "  var letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';" +
               "  return letters.charAt(Math.floor(Math.random() * letters.length));" +
               "}" +
               "" +
               "function updateConnections() {" +
               "  if (polyline) {" +
               "    polyline.setMap(null);" +
               "  }" +
               "  if (waypoints.length > 1) {" +
               "    var path = waypoints.map(function(wp) { return wp.position; });" +
               "    polyline = new google.maps.Polyline({" +
               "      path: path," +
               "      geodesic: true," +
               "      strokeColor: '#FF0000'," +
               "      strokeOpacity: 1.0," +
               "      strokeWeight: 2" +
               "    });" +
               "    polyline.setMap(map);" +
               "  }" +
               "}" +
               "" +
               "window.clearAllWaypoints = function() {" +
               "  waypoints.forEach(function(wp) {" +
               "    wp.marker.setMap(null);" +
               "  });" +
               "  if (polyline) polyline.setMap(null);" +
               "  waypoints = [];" +
               "};" +
               "" +
               "window.invertRoute = function() {" +
               "  waypoints.reverse();" +
               "  for (var i = 0; i < waypoints.length; i++) {" +
               "    if (i === 0) {" +
               "      waypoints[i].number = 'S';" +
               "      waypoints[i].marker.setLabel('S');" +
               "    } else if (i === waypoints.length - 1) {" +
               "      waypoints[i].number = 'F';" +
               "      waypoints[i].marker.setLabel('F');" +
               "    } else {" +
               "      waypoints[i].number = i.toString();" +
               "      waypoints[i].marker.setLabel(i.toString());" +
               "    }" +
               "  }" +
               "  updateConnections();" +
               "};" +
               "" +
               "window.toggleMapType = function() {" +
               "  var currentType = map.getMapTypeId();" +
               "  map.setMapTypeId(currentType === google.maps.MapTypeId.ROADMAP ? " +
               "    google.maps.MapTypeId.SATELLITE : google.maps.MapTypeId.ROADMAP);" +
               "};" +
               "" +
               "" +
               "window.zoomIn = function() { map.setZoom(map.getZoom() + 1); };" +
               "window.zoomOut = function() { map.setZoom(map.getZoom() - 1); };" +
               "" +
               "" +
               "</script>" +
               "<script async defer src='https://maps.googleapis.com/maps/api/js?key=" + GOOGLE_MAPS_API_KEY + "&callback=initMap'></script>" +
               "</body></html>";
    }
    
    // Methods to control the map from Java
    public void clearWaypoints() {
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript("clearAllWaypoints();");
            });
        }
        // Clear history when resetting
        clearHistory();
    }

    public void invertRoute() {
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript("invertRoute();");
            });
        }
    }
    
    public void toggleView() {
        toggleMapType();
    }
    
    public void toggleMapType() {
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript("toggleMapType();");
            });
        }
    }

    public void zoomIn() {
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript("zoomIn();");
            });
        }
    }

    public void zoomOut() {
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript("zoomOut();");
            });
        }
    }
    
    // This method is deprecated - use getWaypointsAsync instead
    public java.util.List<WayPoint> getWaypoints() {
        // Return empty list - this method was causing performance issues
        // Use getWaypointsAsync for actual waypoint retrieval
        return new java.util.ArrayList<>();
    }
    
    public void getWaypointsAsync(java.util.function.Consumer<java.util.List<WayPoint>> callback) {
        if (isInitialized) {
            Platform.runLater(() -> {
                try {
                    // Get waypoints data from JavaScript
                    String waypointsJson = (String) webEngine.executeScript(
                        "JSON.stringify(waypoints.map(function(wp) { " +
                        "  return {" +
                        "    lat: wp.position.lat()," +
                        "    lng: wp.position.lng()," +
                        "    number: wp.number," +
                        "    letter: wp.letter" +
                        "  };" +
                        "}));"
                    );
                    
                    java.util.List<WayPoint> waypoints = new java.util.ArrayList<>();
                    
                    if (waypointsJson != null && !waypointsJson.equals("[]")) {
                        // Parse the JSON manually (simple parsing for our structure)
                        String[] items = waypointsJson.replace("[", "").replace("]", "").split("\\},\\{");
                        
                        for (int i = 0; i < items.length; i++) {
                            String item = items[i].replace("{", "").replace("}", "");
                            String[] parts = item.split(",");
                            
                            if (parts.length >= 4) {
                                try {
                                    double lat = Double.parseDouble(parts[0].split(":")[1]);
                                    double lng = Double.parseDouble(parts[1].split(":")[1]);
                                    String number = parts[2].split(":")[1].replace("\"", "");
                                    String letter = parts[3].split(":")[1].replace("\"", "");
                                    
                                    WayPoint wp = new WayPoint(number, lat, lng);
                                    wp.setLetter(letter);
                                    waypoints.add(wp);
                                    
               // Print full UTM coordinates to console
               String fullUtmCoords = CoordinateUtils.toFullUTM(lat, lng);
               System.out.println("Waypoint " + number + ": " + String.format("%.6f°, %.6f°", lat, lng) + " -> " + fullUtmCoords);
                                } catch (NumberFormatException e) {
                                    System.err.println("Error parsing waypoint: " + e.getMessage());
                                }
                            }
                        }
                    }
                    
                    // Call callback with results
                    callback.accept(waypoints);
                    
                } catch (Exception e) {
                    System.err.println("Error in JavaFX thread: " + e.getMessage());
                    callback.accept(new java.util.ArrayList<>());
                }
            });
        } else {
            callback.accept(new java.util.ArrayList<>());
        }
    }
    
    public void addWaypoint(double lat, double lng) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWaypointTime < 200) {
            System.out.println("Waypoint creation debounced - too soon");
            return;
        }
        lastWaypointTime = currentTime;
        
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript("addWaypointToMap(new google.maps.LatLng(" + lat + ", " + lng + "));");
            });
        }
    }
    
    public void setMapReadyCallback(Runnable callback) {
        this.mapReadyCallback = callback;
    }

    public String getUTMCoordinates(double lat, double lng) {
        return CoordinateUtils.toUTM(lat, lng);
    }
    
    public void undo() {
        System.out.println("Undo method called, isInitialized: " + isInitialized + ", undoCount: " + undoCount);
        
        // Check undo limit
        if (undoCount >= MAX_HISTORY_SIZE) {
            System.out.println("Undo limit reached (20 operations)");
            return;
        }
        
        if (isInitialized) {
            Platform.runLater(() -> {
                webEngine.executeScript(
                    "console.log('Undo called, waypoints length:', waypoints.length);" +
                    "if (waypoints.length > 0) {" +
                    "  var lastWaypoint = waypoints[waypoints.length - 1];" +
                    "  console.log('Removing waypoint:', lastWaypoint.number);" +
                    "  lastWaypoint.marker.setMap(null);" +
                    "  waypoints.pop();" +
                    "  updateConnections();" +
                    "  console.log('Waypoint removed, new length:', waypoints.length);" +
                    "  for (var i = 0; i < waypoints.length; i++) {" +
                    "    if (i === 0) {" +
                    "      waypoints[i].number = 'S';" +
                    "      waypoints[i].marker.setLabel('S');" +
                    "    } else if (i === waypoints.length - 1) {" +
                    "      waypoints[i].number = 'F';" +
                    "      waypoints[i].marker.setLabel('F');" +
                    "    } else {" +
                    "      waypoints[i].number = i.toString();" +
                    "      waypoints[i].marker.setLabel(i.toString());" +
                    "    }" +
                    "  }" +
                    "  if (window.javaApp && window.javaApp.storeUndoWaypoint) {" +
                    "    window.javaApp.storeUndoWaypoint(lastWaypoint.position.lat(), lastWaypoint.position.lng(), lastWaypoint.number, lastWaypoint.letter);" +
                    "  } else {" +
                    "    console.log('Java method not available');" +
                    "  }" +
                    "} else {" +
                    "  console.log('No waypoints to remove');" +
                    "}"
                );
            });
        }
    }
    
    public void redo() {
        if (!redoHistory.isEmpty()) {
            WayPoint waypoint = redoHistory.remove(redoHistory.size() - 1);
            if (isInitialized) {
                Platform.runLater(() -> {
                    webEngine.executeScript(
                        "addWaypointToMap(new google.maps.LatLng(" + waypoint.getLatitude() + ", " + waypoint.getLongitude() + "));" +
                        "waypoints[waypoints.length - 1].number = '" + waypoint.getNumber() + "';" +
                        "waypoints[waypoints.length - 1].letter = '" + waypoint.getLetter() + "';" +
                        "waypoints[waypoints.length - 1].marker.setLabel('" + waypoint.getNumber() + "');" +
                        "for (var i = 0; i < waypoints.length; i++) {" +
                        "  if (i === 0) {" +
                        "    waypoints[i].number = 'S';" +
                        "    waypoints[i].marker.setLabel('S');" +
                        "  } else if (i === waypoints.length - 1) {" +
                        "    waypoints[i].number = 'F';" +
                        "    waypoints[i].marker.setLabel('F');" +
                        "  } else {" +
                        "    waypoints[i].number = i.toString();" +
                        "    waypoints[i].marker.setLabel(i.toString());" +
                        "  }" +
                        "}"
                    );
                });
            }
            // Decrement undo counter when redoing
            undoCount--;
            System.out.println("Redo: restored waypoint " + waypoint.getNumber() + " (undo count: " + undoCount + ")");
        } else {
            System.out.println("Nothing to redo");
        }
    }
    
    public void storeUndoWaypoint(double lat, double lng, String number, String letter) {
        System.out.println("storeUndoWaypoint called with: " + number + " at " + lat + ", " + lng);
        WayPoint waypoint = new WayPoint(number, lat, lng);
        waypoint.setLetter(letter);
        redoHistory.add(waypoint);
        
        // Increment undo counter
        undoCount++;
        
        // Limit redo history size to 20
        if (redoHistory.size() > MAX_HISTORY_SIZE) {
            redoHistory.remove(0);
        }
        
        System.out.println("Stored undo waypoint: " + number + " at " + lat + ", " + lng + " (redo history: " + redoHistory.size() + ", undo count: " + undoCount + ")");
    }
    
    public void clearHistory() {
        undoHistory.clear();
        redoHistory.clear();
        undoCount = 0;
        System.out.println("History cleared, undo count reset to 0");
    }

    public void addCoordinateDisplay() {
        if (isInitialized) {
            Platform.runLater(() -> {
                // Add coordinate display overlay
                webEngine.executeScript(
                    "var coordDiv = document.createElement('div');" +
                    "coordDiv.id = 'coordinateDisplay';" +
                    "coordDiv.style.cssText = 'position:absolute;top:10px;left:10px;background:rgba(0,0,0,0.8);color:white;padding:8px 12px;border-radius:4px;font-family:monospace;font-size:14px;z-index:1000;pointer-events:none;';" +
                    "coordDiv.textContent = '-- --';" +
                    "document.body.appendChild(coordDiv);" +
                    "map.addListener('mousemove', function(event) {" +
                    "  var lat = event.latLng.lat();" +
                    "  var lng = event.latLng.lng();" +
                    "  if (window.javaApp && window.javaApp.getUTMCoordinates) {" +
                    "    try {" +
                    "      var coords = window.javaApp.getUTMCoordinates(lat, lng);" +
                    "      document.getElementById('coordinateDisplay').textContent = coords;" +
                    "    } catch (e) {" +
                    "      document.getElementById('coordinateDisplay').textContent = lat.toFixed(4) + ', ' + lng.toFixed(4);" +
                    "    }" +
                    "  } else {" +
                    "    document.getElementById('coordinateDisplay').textContent = lat.toFixed(4) + ', ' + lng.toFixed(4);" +
                    "  }" +
                    "});"
                );
            });
        }
    }
    
    public void onMapReady() {
        isMapReady = true;
        System.out.println("Map is ready for interaction!");
        
        // Enable map interactions
        if (isInitialized) {
            Platform.runLater(() -> {
                // Update the JavaScript flag
                webEngine.executeScript("window.isMapReady = true;");
                
                webEngine.executeScript(
                    "map.setOptions({" +
                    "  draggable: true," +
                    "  zoomControl: true," +
                    "  scrollwheel: true," +
                    "  disableDoubleClickZoom: false," +
                    "  clickableIcons: true" +
                    "});"
                );
                
                // Add coordinate display after map is ready
                addCoordinateDisplay();
            });
        }
        
        if (mapReadyCallback != null) {
            mapReadyCallback.run();
        }
    }
    
    public void cleanup() {
        if (jfxPanel != null) {
            Platform.runLater(() -> {
                if (webEngine != null) {
                    webEngine.load("about:blank");
                }
            });
        }
    }
}