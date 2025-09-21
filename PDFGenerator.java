import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class PDFGenerator {
    
    private static final int POINTS_PER_PAGE = 8; // 2x4 grid
    private static final int MARGIN = 50;
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
    
    public static void generateRoutePDF(List<WayPoint> wayPoints) throws Exception {
        if (wayPoints.isEmpty()) {
            throw new Exception("No waypoints to generate PDF");
        }
        
        // First, try to save as image (more reliable than printing)
        saveAsImage(wayPoints);
    }
    
    private static void saveAsImage(List<WayPoint> wayPoints) throws Exception {
        // Create an image representation of the PDF
        int width = 595;  // A4 width in points
        int height = 842; // A4 height in points
        
        BufferedImage image = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Set white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // Draw the waypoints directly
        drawWayPointsDocument(g2d, wayPoints, width, height);
        
        g2d.dispose();
        
        // Save the image to a default location first
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String defaultFileName = "route_points_" + timestamp + ".png";
        File defaultFile = new File(System.getProperty("user.home"), defaultFileName);
        
        try {
            javax.imageio.ImageIO.write(image, "PNG", defaultFile);
            
            // Show success message with file location
            int option = JOptionPane.showConfirmDialog(
                null,
                "PDF saved successfully to:\n" + defaultFile.getAbsolutePath() + "\n\nWould you like to choose a different location?",
                "PDF Saved",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // If user wants to choose different location, show file chooser
            if (option == JOptionPane.YES_OPTION) {
                try {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setSelectedFile(new File(defaultFileName));
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                    
                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File chosenFile = fileChooser.getSelectedFile();
                        if (!chosenFile.getName().toLowerCase().endsWith(".png")) {
                            chosenFile = new File(chosenFile.getAbsolutePath() + ".png");
                        }
                        
                        // Copy to chosen location
                        java.nio.file.Files.copy(defaultFile.toPath(), chosenFile.toPath(), 
                                               java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        // Delete the default file
                        defaultFile.delete();
                        
                        JOptionPane.showMessageDialog(null, 
                            "PDF saved to: " + chosenFile.getAbsolutePath(), 
                            "PDF Saved", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, 
                        "Could not save to chosen location, but file is available at:\n" + defaultFile.getAbsolutePath(),
                        "Save Location Issue", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to save PDF: " + e.getMessage());
        }
    }
    
    private static void drawWayPointsDocument(Graphics2D g2d, List<WayPoint> wayPoints, int width, int height) {
        // Draw title
        g2d.setFont(HEADER_FONT);
        g2d.setColor(Color.BLACK);
        String title = "RouteMaker - Maršruto Taškai";
        FontMetrics titleFm = g2d.getFontMetrics();
        int titleX = (width - titleFm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 30);
        
        // Calculate grid layout (2x4)
        int cols = 2;
        int rows = 4;
        double cellWidth = (width - 2 * MARGIN) / (double) cols;
        double cellHeight = (height - 80) / (double) rows; // Reserve space for title
        
        // Draw waypoints in grid
        for (int i = 0; i < Math.min(wayPoints.size(), POINTS_PER_PAGE); i++) {
            WayPoint wp = wayPoints.get(i);
            
            int row = i / cols;
            int col = i % cols;
            
            double cellX = MARGIN + col * cellWidth;
            double cellY = 60 + row * cellHeight; // Offset for title
            
            drawWayPointCard(g2d, wp, cellX, cellY, cellWidth, cellHeight);
        }
    }
    
    private static void drawWayPointCard(Graphics2D g2d, WayPoint wayPoint, 
                                double x, double y, double width, double height) {
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int) x + 5, (int) y + 5, (int) width - 10, (int) height - 10);
        
        // Interior coordinates
        double innerX = x + 15;
        double innerY = y + 25;
        double innerWidth = width - 30;
        
        // Draw table-like structure
        g2d.setFont(LABEL_FONT);
        FontMetrics labelFm = g2d.getFontMetrics();
        g2d.setFont(CONTENT_FONT);
        FontMetrics contentFm = g2d.getFontMetrics();
        
        int lineHeight = 25;
        int currentY = (int) innerY;
        
        // Point number/type
        g2d.setFont(LABEL_FONT);
        String pointLabel = "Taškas:";
        String pointValue = wayPoint.getDisplayNumber();
        drawTableRow(g2d, pointLabel, pointValue, innerX, currentY, innerWidth, labelFm, contentFm);
        currentY += lineHeight;
        
        // Draw separator line
        g2d.drawLine((int) innerX, currentY - 5, (int) (innerX + innerWidth), currentY - 5);
        
        // Coordinates
        String coordLabel = "Koordinatės:";
        String coordValue = wayPoint.getCurrentCoordinates();
        drawTableRow(g2d, coordLabel, coordValue, innerX, currentY, innerWidth, labelFm, contentFm);
        currentY += lineHeight;
        
        // Draw separator line
        g2d.drawLine((int) innerX, currentY - 5, (int) (innerX + innerWidth), currentY - 5);
        
        // Letter
        String letterLabel = "Raidė:";
        String letterValue = wayPoint.getLetter();
        drawTableRow(g2d, letterLabel, letterValue, innerX, currentY, innerWidth, labelFm, contentFm);
        currentY += lineHeight;
        
        // Draw separator line
        g2d.drawLine((int) innerX, currentY - 5, (int) (innerX + innerWidth), currentY - 5);
        
        // Next point coordinates
        String nextLabel = "Sekančio taško koordinatės:";
        String nextValue = wayPoint.getNextPointCoordinates();
        drawTableRow(g2d, nextLabel, nextValue, innerX, currentY, innerWidth, labelFm, contentFm);
    }
        
    private static void drawTableRow(Graphics2D g2d, String label, String value, 
                            double x, int y, double width, 
                            FontMetrics labelFm, FontMetrics contentFm) {
        
        // Draw label
        g2d.setFont(LABEL_FONT);
        g2d.drawString(label, (int) x, y);
        
        // Calculate value position (right-aligned or on next line if too long)
        g2d.setFont(CONTENT_FONT);
        int valueWidth = contentFm.stringWidth(value);
        int labelWidth = labelFm.stringWidth(label);
        
        if (labelWidth + valueWidth + 20 < width) {
            // Same line
            g2d.drawString(value, (int) (x + width - valueWidth), y);
        } else {
            // Next line, indented
            g2d.drawString(value, (int) (x + 20), y + 15);
        }
    }
    
    private static class WayPointPrintable implements Printable {
        private List<WayPoint> wayPoints;
        
        public WayPointPrintable(List<WayPoint> wayPoints) {
            this.wayPoints = wayPoints;
        }
        
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
                throws PrinterException {
            
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Get printable area
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();
            double x = pageFormat.getImageableX();
            double y = pageFormat.getImageableY();
            
            // Draw title
            g2d.setFont(HEADER_FONT);
            g2d.setColor(Color.BLACK);
            String title = "RouteMaker - Marsruto Taskai";
            FontMetrics titleFm = g2d.getFontMetrics();
            double titleX = x + (pageWidth - titleFm.stringWidth(title)) / 2;
            g2d.drawString(title, (int) titleX, (int) (y + 30));
            
            // Calculate grid layout (2x4)
            int cols = 2;
            int rows = 4;
            double cellWidth = pageWidth / cols;
            double cellHeight = (pageHeight - 60) / rows; // Reserve space for title
            
            // Draw waypoints in grid
            for (int i = 0; i < Math.min(wayPoints.size(), POINTS_PER_PAGE); i++) {
                WayPoint wp = wayPoints.get(i);
                
                int row = i / cols;
                int col = i % cols;
                
                double cellX = x + col * cellWidth;
                double cellY = y + 60 + row * cellHeight; // Offset for title
                
                drawWayPointCard(g2d, wp, cellX, cellY, cellWidth, cellHeight);
            }
            
            return PAGE_EXISTS;
        }
    }
}
