import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

public class MapPanel extends JPanel {
    private ActionManager actionManager;
    private List<WayPoint> wayPoints;
    private double centerLatitude;
    private double centerLongitude;
    private double zoomLevel;
    private boolean satelliteView;
    private WayPoint hoveredPoint;
    private Point mousePosition;
    
    // Visual constants
    private static final int POINT_SIZE = 12;
    private static final int HOVER_RADIUS = 15;
    private static final Color SATELLITE_COLOR = new Color(50, 70, 50);
    private static final Color NORMAL_COLOR = new Color(200, 220, 200);
    private static final Color POINT_COLOR = Color.RED;
    private static final Color LINE_COLOR = Color.BLUE;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color FINISH_COLOR = Color.RED;
    
    public MapPanel(ActionManager actionManager) {
        this.actionManager = actionManager;
        this.wayPoints = new ArrayList<>();
        this.centerLatitude = CoordinateUtils.DEFAULT_LATITUDE;
        this.centerLongitude = CoordinateUtils.DEFAULT_LONGITUDE;
        this.zoomLevel = 1.0;
        this.satelliteView = false;
        this.mousePosition = new Point();
        
        setupEventListeners();
        setToolTipText(""); // Enable tooltips
    }
    
    private void setupEventListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Left click
                    addWayPoint(e.getX(), e.getY());
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                updateHoveredPoint(e.getX(), e.getY());
                repaint();
            }
        });
    }
    
    private void updateHoveredPoint(int mouseX, int mouseY) {
        hoveredPoint = null;
        for (WayPoint wp : wayPoints) {
            Point screenPos = wp.getScreenPosition();
            double distance = Math.sqrt(Math.pow(mouseX - screenPos.x, 2) + Math.pow(mouseY - screenPos.y, 2));
            if (distance <= HOVER_RADIUS) {
                hoveredPoint = wp;
                setToolTipText(wp.getTooltipText());
                return;
            }
        }
        setToolTipText(null);
    }
    
    public void addWayPoint(int screenX, int screenY) {
        // Convert screen coordinates to geographical coordinates
        double[] geoCoords = CoordinateUtils.screenToGeo(
            screenX, screenY, getWidth(), getHeight(),
            centerLatitude, centerLongitude, zoomLevel
        );
        
        String pointNumber = determinePointNumber();
        WayPoint newPoint = new WayPoint(pointNumber, geoCoords[0], geoCoords[1], new Point(screenX, screenY));
        
        // Execute through action manager for undo support
        actionManager.executeAction(new AddWayPointAction(this, newPoint));
        repaint();
    }
    
    public void addWayPointInternal(WayPoint wayPoint) {
        // Update existing points numbering
        updatePointNumbering(wayPoint);
        wayPoints.add(wayPoint);
        updateConnections();
        updateScreenPositions();
    }
    
    private String determinePointNumber() {
        if (wayPoints.isEmpty()) {
            return "S"; // First point is Start
        } else if (wayPoints.size() == 1) {
            return "F"; // Second point is Finish
        } else {
            // Convert last point from F to numbered point
            return "F"; // New point becomes Finish
        }
    }
    
    private void updatePointNumbering(WayPoint newPoint) {
        if (wayPoints.isEmpty()) {
            // First point - already set as "S"
            return;
        }
        
        if (wayPoints.size() == 1) {
            // Second point - first becomes S, new becomes F
            wayPoints.get(0).setNumber("S");
            return;
        }
        
        // Multiple points - renumber existing points
        for (int i = 0; i < wayPoints.size(); i++) {
            WayPoint wp = wayPoints.get(i);
            if (i == 0) {
                wp.setNumber("S");
            } else if (i == wayPoints.size() - 1) {
                // Last point becomes numbered, new point will be F
                wp.setNumber(String.valueOf(i));
            } else {
                wp.setNumber(String.valueOf(i));
            }
        }
    }
    
    private void updateConnections() {
        // Connect points in sequence
        for (int i = 0; i < wayPoints.size() - 1; i++) {
            wayPoints.get(i).setNextPoint(wayPoints.get(i + 1));
        }
        // Last point has no next point
        if (!wayPoints.isEmpty()) {
            wayPoints.get(wayPoints.size() - 1).setNextPoint(null);
        }
    }
    
    private void updateScreenPositions() {
        for (WayPoint wp : wayPoints) {
            int[] screenCoords = CoordinateUtils.geoToScreen(
                wp.getLatitude(), wp.getLongitude(),
                getWidth(), getHeight(),
                centerLatitude, centerLongitude, zoomLevel
            );
            wp.setScreenPosition(new Point(screenCoords[0], screenCoords[1]));
        }
    }
    
    public void clearWayPoints() {
        wayPoints.clear();
    }
    
    public void invertRoute() {
        if (wayPoints.size() < 2) return;
        
        // Reverse the order of waypoints
        Collections.reverse(wayPoints);
        
        // Update numbering after reversal
        for (int i = 0; i < wayPoints.size(); i++) {
            if (i == 0) {
                wayPoints.get(i).setNumber("S");
            } else if (i == wayPoints.size() - 1) {
                wayPoints.get(i).setNumber("F");
            } else {
                wayPoints.get(i).setNumber(String.valueOf(i));
            }
        }
        
        updateConnections();
        updateScreenPositions();
    }
    
    public void zoomIn() {
        zoomLevel *= 1.5;
        updateScreenPositions();
        repaint();
    }
    
    public void zoomOut() {
        zoomLevel /= 1.5;
        updateScreenPositions();
        repaint();
    }
    
    public void toggleView() {
        satelliteView = !satelliteView;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        drawBackground(g2d);
        
        // Draw grid lines for reference
        drawGrid(g2d);
        
        // Draw connections between waypoints
        drawConnections(g2d);
        
        // Draw waypoints
        drawWayPoints(g2d);
        
        // Draw info panel
        drawInfoPanel(g2d);
    }
    
    private void drawBackground(Graphics2D g2d) {
        Color bgColor = satelliteView ? SATELLITE_COLOR : NORMAL_COLOR;
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Add some texture for satellite view
        if (satelliteView) {
            g2d.setColor(new Color(60, 80, 60));
            for (int i = 0; i < getWidth(); i += 20) {
                for (int j = 0; j < getHeight(); j += 20) {
                    if ((i + j) % 40 == 0) {
                        g2d.fillRect(i, j, 2, 2);
                    }
                }
            }
        }
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.setStroke(new BasicStroke(1));
        
        int gridSize = (int) (50 * zoomLevel);
        
        for (int x = 0; x < getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += gridSize) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }
    
    private void drawConnections(Graphics2D g2d) {
        g2d.setColor(LINE_COLOR);
        g2d.setStroke(new BasicStroke(3));
        
        for (int i = 0; i < wayPoints.size() - 1; i++) {
            Point p1 = wayPoints.get(i).getScreenPosition();
            Point p2 = wayPoints.get(i + 1).getScreenPosition();
            g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
        }
    }
    
    private void drawWayPoints(Graphics2D g2d) {
        for (WayPoint wp : wayPoints) {
            Point pos = wp.getScreenPosition();
            
            // Choose color based on point type
            Color pointColor = POINT_COLOR;
            if ("S".equals(wp.getNumber())) {
                pointColor = START_COLOR;
            } else if ("F".equals(wp.getNumber())) {
                pointColor = FINISH_COLOR;
            }
            
            // Highlight if hovered
            if (wp == hoveredPoint) {
                g2d.setColor(new Color(255, 255, 0, 100));
                g2d.fillOval(pos.x - HOVER_RADIUS, pos.y - HOVER_RADIUS, 
                           HOVER_RADIUS * 2, HOVER_RADIUS * 2);
            }
            
            // Draw point
            g2d.setColor(pointColor);
            g2d.fillOval(pos.x - POINT_SIZE/2, pos.y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            
            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(pos.x - POINT_SIZE/2, pos.y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            
            // Draw number/label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            String label = wp.getNumber();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            g2d.drawString(label, pos.x - labelWidth/2, pos.y + labelHeight/2);
        }
    }
    
    private void drawInfoPanel(Graphics2D g2d) {
        // Draw info panel in top-left corner
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(10, 10, 250, 100);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(10, 10, 250, 100);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("View: " + (satelliteView ? "Satellite" : "Normal"), 20, 30);
        g2d.drawString("Zoom: " + String.format("%.1fx", zoomLevel), 20, 50);
        g2d.drawString("Points: " + wayPoints.size(), 20, 70);
        g2d.drawString("Center: " + CoordinateUtils.toFullUTM(centerLatitude, centerLongitude), 20, 90);
    }
    
    // Getters and setters
    public List<WayPoint> getWayPoints() {
        return new ArrayList<>(wayPoints);
    }
    
    public void setWayPoints(List<WayPoint> wayPoints) {
        this.wayPoints = new ArrayList<>();
        for (WayPoint wp : wayPoints) {
            this.wayPoints.add(wp.clone());
        }
        updateConnections();
        updateScreenPositions();
    }
    
    public void cleanup() {
        // Clean up any resources
        if (wayPoints != null) {
            wayPoints.clear();
        }
        hoveredPoint = null;
        
        // Remove all listeners
        for (MouseListener ml : getMouseListeners()) {
            removeMouseListener(ml);
        }
        for (MouseMotionListener mml : getMouseMotionListeners()) {
            removeMouseMotionListener(mml);
        }
        
        System.out.println("MapPanel cleaned up successfully");
    }
}
