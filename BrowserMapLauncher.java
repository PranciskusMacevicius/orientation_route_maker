import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class BrowserMapLauncher {
    
    public static void openGoogleMapsWithRoute(List<WayPoint> wayPoints) {
        if (wayPoints.isEmpty()) {
            System.out.println("No waypoints to show on map");
            return;
        }
        
        try {
            String googleMapsUrl = buildGoogleMapsUrl(wayPoints);
            Desktop.getDesktop().browse(new URI(googleMapsUrl));
            System.out.println("Opening Google Maps in browser: " + googleMapsUrl);
        } catch (Exception e) {
            System.err.println("Error opening Google Maps: " + e.getMessage());
        }
    }
    
    private static String buildGoogleMapsUrl(List<WayPoint> wayPoints) {
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/");
        
        for (int i = 0; i < wayPoints.size(); i++) {
            WayPoint wp = wayPoints.get(i);
            if (i > 0) {
                url.append("/");
            }
            url.append(wp.getLatitude()).append(",").append(wp.getLongitude());
        }
        
        // Add some parameters for better display
        url.append("/@");
        WayPoint firstPoint = wayPoints.get(0);
        url.append(firstPoint.getLatitude()).append(",").append(firstPoint.getLongitude());
        url.append(",15z"); // Zoom level
        
        return url.toString();
    }
    
    public static void openSinglePoint(double latitude, double longitude, String label) {
        try {
            String url = "https://www.google.com/maps/@" + latitude + "," + longitude + ",15z";
            Desktop.getDesktop().browse(new URI(url));
            System.out.println("Opening point in Google Maps: " + label);
        } catch (Exception e) {
            System.err.println("Error opening Google Maps: " + e.getMessage());
        }
    }
}
