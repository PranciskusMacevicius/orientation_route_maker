public class CoordinateUtils {
    
    // Default coordinates for Rukla, Lithuania
    public static final double DEFAULT_LATITUDE = 55.030180;
    public static final double DEFAULT_LONGITUDE = 24.370464;
    
    /**
     * Converts latitude and longitude to MGRS format (simplified approximation)
     * This is a simplified implementation for demonstration purposes.
     * In a real application, you would use a proper MGRS conversion library.
     */
    public static String toMGRS(double latitude, double longitude) {
        // This is a simplified MGRS-like format for demonstration
        // Real MGRS conversion requires complex UTM calculations
        
        // Convert to pseudo-UTM coordinates (simplified)
        int zone = (int) Math.floor((longitude + 180) / 6) + 1;
        
        // Generate grid square letters (simplified approach)
        char gridSquare1 = (char) ('A' + ((int) Math.abs(latitude * 10) % 26));
        char gridSquare2 = (char) ('A' + ((int) Math.abs(longitude * 10) % 26));
        
        // Calculate easting and northing (simplified)
        int easting = (int) Math.abs((longitude - Math.floor(longitude)) * 100000);
        int northing = (int) Math.abs((latitude - Math.floor(latitude)) * 100000);
        
        // Format as MGRS-like: ZONE GRID_SQUARE EASTING NORTHING
        String fullMGRS = String.format("%02d%c%c %04d %04d", 
                           zone, gridSquare1, gridSquare2, 
                           easting % 10000, northing % 10000);
        
        // Remove first 4 characters as requested
        if (fullMGRS.length() > 4) {
            return fullMGRS.substring(4);
        }
        return fullMGRS;
    }
    
    /**
     * Converts screen coordinates to geographical coordinates based on map bounds
     */
    public static double[] screenToGeo(int screenX, int screenY, int mapWidth, int mapHeight, 
                                     double centerLat, double centerLon, double zoomLevel) {
        
        // Calculate the degrees per pixel based on zoom level
        double degreesPerPixel = getDegreesPerPixel(zoomLevel);
        
        // Calculate offset from center
        int offsetX = screenX - mapWidth / 2;
        int offsetY = screenY - mapHeight / 2;
        
        // Convert to geographical coordinates
        double longitude = centerLon + (offsetX * degreesPerPixel);
        double latitude = centerLat - (offsetY * degreesPerPixel); // Y is inverted
        
        return new double[]{latitude, longitude};
    }
    
    /**
     * Converts geographical coordinates to screen coordinates
     */
    public static int[] geoToScreen(double latitude, double longitude, int mapWidth, int mapHeight,
                                  double centerLat, double centerLon, double zoomLevel) {
        
        double degreesPerPixel = getDegreesPerPixel(zoomLevel);
        
        // Calculate offset from center in degrees
        double lonOffset = longitude - centerLon;
        double latOffset = centerLat - latitude; // Y is inverted
        
        // Convert to screen coordinates
        int screenX = (int) (mapWidth / 2 + lonOffset / degreesPerPixel);
        int screenY = (int) (mapHeight / 2 + latOffset / degreesPerPixel);
        
        return new int[]{screenX, screenY};
    }
    
    /**
     * Calculate degrees per pixel based on zoom level
     */
    private static double getDegreesPerPixel(double zoomLevel) {
        // Base degree per pixel at zoom level 1 (approximately 200m height view)
        double baseDegreePerPixel = 0.001; // Adjust this value as needed
        return baseDegreePerPixel / zoomLevel;
    }
    
    /**
     * Calculate distance between two points in meters (Haversine formula)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371000; // Earth's radius in meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Generate coordinates around a center point for demonstration
     */
    public static double[] generateNearbyCoordinates(double centerLat, double centerLon, 
                                                   double radiusInMeters) {
        double radiusInDegrees = radiusInMeters / 111000.0; // Rough conversion
        
        double angle = Math.random() * 2 * Math.PI;
        double radius = Math.random() * radiusInDegrees;
        
        double lat = centerLat + radius * Math.cos(angle);
        double lon = centerLon + radius * Math.sin(angle);
        
        return new double[]{lat, lon};
    }
    
    /**
     * Validate if coordinates are within reasonable bounds
     */
    public static boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
    
    /**
     * Format coordinates for display
     */
    public static String formatCoordinates(double latitude, double longitude) {
        return String.format("%.6f°, %.6f°", latitude, longitude);
    }
}
