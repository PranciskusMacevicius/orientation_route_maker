import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RouteMaker extends JFrame {
    private GoogleMapsPanel googleMapsPanel;
    private ActionManager actionManager;
    private JButton zoomInButton, zoomOutButton, resetButton, invertButton, undoButton, redoButton, showCoordsButton, pdfButton, toggleViewButton, exitButton;
    
    public RouteMaker() {
        setTitle("RouteMaker - Google Maps Route Planning");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Add window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = JOptionPane.showConfirmDialog(
                    RouteMaker.this,
                    "Are you sure you want to exit RouteMaker?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    // Clean up resources
                    if (googleMapsPanel != null) {
                        googleMapsPanel.cleanup();
                    }
                    
                    // Exit the application
                    System.exit(0);
                }
            }
        });
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        actionManager = new ActionManager();
        googleMapsPanel = new GoogleMapsPanel(actionManager);
        
        // Set up map ready callback to enable buttons
        googleMapsPanel.setMapReadyCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                enableButtons(true);
            });
        });
        
        // Create control buttons
        zoomInButton = new JButton("Zoom In");
        zoomOutButton = new JButton("Zoom Out");
        resetButton = new JButton("Reset");
        invertButton = new JButton("Invert Route");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        showCoordsButton = new JButton("Show Coords");
        pdfButton = new JButton("Generate PDF");
        toggleViewButton = new JButton("Toggle View");
        exitButton = new JButton("Exit");
        
        // Set button sizes
        Dimension buttonSize = new Dimension(120, 30);
        zoomInButton.setPreferredSize(buttonSize);
        zoomOutButton.setPreferredSize(buttonSize);
        resetButton.setPreferredSize(buttonSize);
        invertButton.setPreferredSize(buttonSize);
        undoButton.setPreferredSize(buttonSize);
        redoButton.setPreferredSize(buttonSize);
        showCoordsButton.setPreferredSize(buttonSize);
        pdfButton.setPreferredSize(buttonSize);
        toggleViewButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);
        
        
        exitButton.setBackground(new Color(220, 80, 80));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        
        // Initially disable all buttons until map is ready
        enableButtons(false);
    }
    
    private void enableButtons(boolean enabled) {
        zoomInButton.setEnabled(enabled);
        zoomOutButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        invertButton.setEnabled(enabled);
        undoButton.setEnabled(enabled);
        redoButton.setEnabled(enabled);
        showCoordsButton.setEnabled(enabled);
        pdfButton.setEnabled(enabled);
        toggleViewButton.setEnabled(enabled);
        // Keep exit button always enabled
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Add Google Maps panel to center
        add(googleMapsPanel, BorderLayout.CENTER);
        
        // Create control panel for lower right corner
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setOpaque(false);
        
        controlPanel.add(zoomInButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(zoomOutButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(toggleViewButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(resetButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(invertButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(undoButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(redoButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(showCoordsButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(pdfButton);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(exitButton);
        
        // Position control panel in lower right
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(rightPanel, BorderLayout.EAST);
    }
    
    private void setupEventHandlers() {
        zoomInButton.addActionListener(e -> googleMapsPanel.zoomIn());
        zoomOutButton.addActionListener(e -> googleMapsPanel.zoomOut());
        resetButton.addActionListener(e -> googleMapsPanel.clearWaypoints());
        invertButton.addActionListener(e -> googleMapsPanel.invertRoute());
        undoButton.addActionListener(e -> googleMapsPanel.undo());
        redoButton.addActionListener(e -> googleMapsPanel.redo());
        showCoordsButton.addActionListener(e -> showCoordinates());
        pdfButton.addActionListener(e -> generatePDF());
        toggleViewButton.addActionListener(e -> googleMapsPanel.toggleMapType());
        exitButton.addActionListener(e -> exitApplication());
    }
    
    private void generatePDF() {
        // Run PDF generation in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Get waypoints from Google Maps (async)
                googleMapsPanel.getWaypointsAsync((waypoints) -> {
                    if (waypoints.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "No waypoints to generate PDF. Please add some points to the map first.", "No Waypoints", JOptionPane.WARNING_MESSAGE);
                        });
                        return;
                    }
                    
                    // Print the EXACT coordinates being used for PDF generation
                    System.out.println("\n=== PDF Generation - Using These Coordinates ===");
                    for (WayPoint wp : waypoints) {
                        String fullUtmCoords = CoordinateUtils.toFullUTM(wp.getLatitude(), wp.getLongitude());
                        String geoCoords = String.format("%.6f°, %.6f°", wp.getLatitude(), wp.getLongitude());
                        System.out.println("PDF Waypoint " + wp.getNumber() + ": " + geoCoords + " -> " + fullUtmCoords + " (Letter: " + wp.getLetter() + ")");
                    }
                    System.out.println("===============================================\n");
                    
                    // Update next point coordinates for each waypoint
                    for (int i = 0; i < waypoints.size(); i++) {
                        if (i < waypoints.size() - 1) {
                            waypoints.get(i).setNextPoint(waypoints.get(i + 1));
                        }
                    }
                    
                    try {
                        PDFGenerator.generateRoutePDF(waypoints);
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        });
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showCoordinates() {
        // Get waypoints from Google Maps and display their MGRS coordinates
        googleMapsPanel.getWaypointsAsync((waypoints) -> {
            if (waypoints.isEmpty()) {
                System.out.println("No waypoints found. Please add some points to the map first.");
                return;
            }
            
               System.out.println("\n=== Current Waypoints ===");
               for (WayPoint wp : waypoints) {
                   String fullUtmCoords = CoordinateUtils.toFullUTM(wp.getLatitude(), wp.getLongitude());
                   String geoCoords = String.format("%.6f°, %.6f°", wp.getLatitude(), wp.getLongitude());
                   System.out.println("Waypoint " + wp.getNumber() + ": " + geoCoords + " -> " + fullUtmCoords + " (Letter: " + wp.getLetter() + ")");
               }
               System.out.println("========================\n");
        });
    }
    
    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit RouteMaker?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Clean up resources
            if (googleMapsPanel != null) {
                googleMapsPanel.cleanup();
            }
            
            // Exit the application
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RouteMaker());
    }
}
