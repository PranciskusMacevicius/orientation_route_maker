import java.awt.Point;
import java.util.Random;

public class WayPoint {
    private String number;
    private double latitude;
    private double longitude;
    private String letter;
    private WayPoint nextPoint;
    private Point screenPosition;
    
    private static final Random random = new Random();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    public WayPoint(String number, double latitude, double longitude) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.letter = generateRandomLetter();
        this.nextPoint = null;
        this.screenPosition = new Point();
    }
    
    public WayPoint(String number, double latitude, double longitude, Point screenPos) {
        this(number, latitude, longitude);
        this.screenPosition = new Point(screenPos);
    }
    
    private String generateRandomLetter() {
        return String.valueOf(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    
    // Getters and Setters
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getLetter() {
        return letter;
    }
    
    public void setLetter(String letter) {
        this.letter = letter;
    }
    
    public WayPoint getNextPoint() {
        return nextPoint;
    }
    
    public void setNextPoint(WayPoint nextPoint) {
        this.nextPoint = nextPoint;
    }
    
    public Point getScreenPosition() {
        return screenPosition;
    }
    
    public void setScreenPosition(Point screenPosition) {
        this.screenPosition = new Point(screenPosition);
    }
    
    public String getDisplayNumber() {
        switch (number) {
            case "S":
                return "Startas";
            case "F":
                return "Finisas";
            default:
                return number;
        }
    }
    
    public String getCurrentCoordinates() {
        return CoordinateUtils.toMGRS(latitude, longitude);
    }
    
    public String getNextPointCoordinates() {
        if (nextPoint != null) {
            return CoordinateUtils.toMGRS(nextPoint.getLatitude(), nextPoint.getLongitude());
        }
        return "N/A";
    }
    
    public String getTooltipText() {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("<html>");
        tooltip.append("<b>Taskas:</b> ").append(getDisplayNumber()).append("<br>");
        tooltip.append("<b>Koordinates:</b> ").append(getCurrentCoordinates()).append("<br>");
        tooltip.append("<b>Raide:</b> ").append(letter).append("<br>");
        tooltip.append("<b>Sekancio tasko koordinates:</b> ").append(getNextPointCoordinates());
        tooltip.append("</html>");
        return tooltip.toString();
    }
    
    @Override
    public String toString() {
        return String.format("WayPoint[%s] at (%f, %f) letter: %s", 
                           number, latitude, longitude, letter);
    }
    
    @Override
    public WayPoint clone() {
        WayPoint clone = new WayPoint(this.number, this.latitude, this.longitude, this.screenPosition);
        clone.setLetter(this.letter);
        // Note: nextPoint reference is not cloned to avoid circular references
        return clone;
    }
}
