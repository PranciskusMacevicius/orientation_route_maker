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
                    isInitialized = true;
                    System.out.println("Google Maps loaded successfully!");
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
               "      center: { lat: 54.6872, lng: 25.2797 }," +
               "      zoom: 15," +
               "      mapTypeId: google.maps.MapTypeId.SATELLITE" +
               "    });" +
               "" +
               "    map.addListener('click', function(event) {" +
               "      addWaypoint(event.latLng);" +
               "    });" +
               "" +
               "    console.log('Google Maps initialized');" +
               "  } catch (error) {" +
               "    console.error('Error initializing Google Maps:', error);" +
               "  }" +
               "}" +
               "" +
               "function addWaypoint(latLng) {" +
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
               "window.zoomIn = function() { map.setZoom(map.getZoom() + 1); };" +
               "window.zoomOut = function() { map.setZoom(map.getZoom() - 1); };" +
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