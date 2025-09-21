public class CoordinateUtils {
    
    // Default coordinates for Rukla, Lithuania
    public static final double DEFAULT_LATITUDE = 55.030180;
    public static final double DEFAULT_LONGITUDE = 24.370464;
    
    /**
     * Converts latitude and longitude to UTM format (simplified approximation)
     * This is a simplified implementation for demonstration purposes.
     * In a real application, you would use a proper UTM conversion library.
     */
    public static String toUTM(double latitude, double longitude) {
        // Calculate the correct UTM zone
        int zone = (int) Math.floor((longitude + 180) / 6) + 1;
        
        double[] utm = convertWGS84ToUTM(latitude, longitude, zone);
        double easting = utm[0];
        double northing = utm[1];

        // Get first 4 digits for PDF format
        int eastingFirst4 = ((int) Math.abs(easting)) / 100;  // Remove last 2 digits, keep first 4
        int northingFirst4 = ((int) Math.abs(northing)) / 1000; // Remove last 3 digits, keep first 4

        return String.format("%04d %04d", eastingFirst4, northingFirst4);
    }
    
    public static String toFullUTM(double latitude, double longitude) {
        // Determine the correct UTM zone based on longitude
        int zone = (int) Math.floor((longitude + 180) / 6) + 1;
        
        // Debug: Let's try both zones to see which is more accurate
        double[] utm34 = convertWGS84ToUTM(latitude, longitude, 34);
        double[] utm35 = convertWGS84ToUTM(latitude, longitude, 35);
        
        // Use the calculated zone
        double[] utmCorrect = convertWGS84ToUTM(latitude, longitude, zone);
        
        // Debug output
        System.out.println("DEBUG: Lat=" + String.format("%.6f", latitude) + "°, Lon=" + String.format("%.6f", longitude) + "°");
        System.out.println("DEBUG: Calculated zone=" + zone);
        System.out.println("DEBUG: Zone 34 -> E=" + (int)utm34[0] + ", N=" + (int)utm34[1]);
        System.out.println("DEBUG: Zone 35 -> E=" + (int)utm35[0] + ", N=" + (int)utm35[1]);
        System.out.println("DEBUG: Zone " + zone + " -> E=" + (int)utmCorrect[0] + ", N=" + (int)utmCorrect[1]);
        
        // For Lithuania (longitude ~24.37), the correct zone should be 35
        // But let's use the calculated zone
        double easting = utmCorrect[0];
        double northing = utmCorrect[1];
        
        return String.format("%dU %06.0f %07.0f", zone, Math.abs(easting), Math.abs(northing));
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
    
    /**
     * Simplified but more accurate WGS84 to UTM conversion 
     * Based on standard UTM projection formulas
     * @param latitude WGS84 latitude in decimal degrees
     * @param longitude WGS84 longitude in decimal degrees  
     * @param zone UTM zone number
     * @return array with [easting, northing] in meters
     */
    private static double[] convertWGS84ToUTM(double latitude, double longitude, int zone) {
        // WGS84 ellipsoid parameters
        double a = 6378137.0;              // Semi-major axis (meters)
        double e = 0.0818191908;           // First eccentricity
        double e1sq = 0.006739497;         // e1 squared
        
        // UTM parameters
        double k0 = 0.9996;                // Scale factor
        double E0 = 500000.0;              // False easting
        double N0 = 0.0;                   // False northing (Northern hemisphere)
        
        // Convert to radians
        double lat = Math.toRadians(latitude);
        double lon = Math.toRadians(longitude);
        double lon0 = Math.toRadians((zone - 1) * 6 - 180 + 3); // Central meridian
        System.out.println("DEBUG: Zone " + zone + " central meridian = " + Math.toDegrees(lon0) + "°");
        
        double dLon = lon - lon0;
        
        // Calculate M (meridional arc)
        double M = a * ((1 - Math.pow(e, 2) / 4 - 3 * Math.pow(e, 4) / 64 - 5 * Math.pow(e, 6) / 256) * lat
                - (3 * Math.pow(e, 2) / 8 + 3 * Math.pow(e, 4) / 32 + 45 * Math.pow(e, 6) / 1024) * Math.sin(2 * lat)
                + (15 * Math.pow(e, 4) / 256 + 45 * Math.pow(e, 6) / 1024) * Math.sin(4 * lat)
                - (35 * Math.pow(e, 6) / 3072) * Math.sin(6 * lat));
        
        // Calculate other parameters
        double nu = a / Math.sqrt(1 - Math.pow(e * Math.sin(lat), 2));
        double rho = nu * (1 - Math.pow(e, 2)) / (1 - Math.pow(e * Math.sin(lat), 2));
        double T = Math.pow(Math.tan(lat), 2);
        double C = e1sq * Math.pow(Math.cos(lat), 2);
        double A_coeff = dLon * Math.cos(lat);
        
        // Calculate easting
        double easting = k0 * nu * (A_coeff + (1 - T + C) * Math.pow(A_coeff, 3) / 6
                + (5 - 18 * T + Math.pow(T, 2) + 72 * C - 58 * e1sq) * Math.pow(A_coeff, 5) / 120) + E0;
        
        // Calculate northing
        double northing = k0 * (M + nu * Math.tan(lat) * (Math.pow(A_coeff, 2) / 2
                + (5 - T + 9 * C + 4 * Math.pow(C, 2)) * Math.pow(A_coeff, 4) / 24
                + (61 - 58 * T + Math.pow(T, 2) + 600 * C - 330 * e1sq) * Math.pow(A_coeff, 6) / 720)) + N0;
        
        return new double[]{easting, northing};
    }
}
