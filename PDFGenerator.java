import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import java.io.*;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
        
        // Generate PDF using Java's built-in printing system
        generatePDFWithPrintable(wayPoints);
    }
    
    private static void generatePDFWithPrintable(List<WayPoint> wayPoints) throws Exception {
        // Create Route Papers directory next to JAR file
        File jarLocation = new File(System.getProperty("user.dir"));
        File routePapersDir = new File(jarLocation, "Route Papers");
        if (!routePapersDir.exists()) {
            routePapersDir.mkdirs();
        }
        
        // Generate filename with timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fileName = "route_points_" + timestamp + ".pdf";
        File outputFile = new File(routePapersDir, fileName);
        
        // Create actual PDF using Java's Graphics2D to PDF approach
        createRealPDF(wayPoints, outputFile);
    }
    
    private static void createRealPDF(List<WayPoint> wayPoints, File outputFile) throws Exception {
        // Update next point coordinates for each waypoint
        for (int i = 0; i < wayPoints.size(); i++) {
            if (i < wayPoints.size() - 1) {
                wayPoints.get(i).setNextPoint(wayPoints.get(i + 1));
            }
        }
        
        // Create actual PDF using manual PDF generation
        createActualPDF(wayPoints, outputFile);
    }
    
    private static void createActualPDF(List<WayPoint> wayPoints, File outputFile) throws Exception {
        // Create a proper PDF using basic PDF structure
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(fos, "UTF-8")) {
            
            // Calculate pages and objects
            int totalPages = (int) Math.ceil((double) wayPoints.size() / POINTS_PER_PAGE);
            
            // PDF Header
            writer.write("%PDF-1.4\n");
            
            // Catalog object (1 0 obj)
            long catalogPos = fos.getChannel().position();
            writer.write("1 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Catalog\n");
            writer.write("/Pages 2 0 R\n");
            writer.write(">>\n");
            writer.write("endobj\n\n");
            
            // Pages object (2 0 obj)
            long pagesPos = fos.getChannel().position();
            writer.write("2 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Pages\n");
            writer.write("/Count " + totalPages + "\n");
            writer.write("/Kids [");
            for (int i = 0; i < totalPages; i++) {
                writer.write((3 + i * 2) + " 0 R ");
            }
            writer.write("]\n");
            writer.write(">>\n");
            writer.write("endobj\n\n");
            
            // Font object (will be referenced by all pages)
            long fontPos = fos.getChannel().position();
            int fontObjNum = 3 + totalPages * 2;
            writer.write(fontObjNum + " 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Font\n");
            writer.write("/Subtype /Type1\n");
            writer.write("/BaseFont /Helvetica\n");
            writer.write(">>\n");
            writer.write("endobj\n\n");
            
            // Create pages and content
            java.util.List<Long> pagePositions = new java.util.ArrayList<>();
            java.util.List<Long> contentPositions = new java.util.ArrayList<>();
            
            for (int page = 0; page < totalPages; page++) {
                // Page object
                pagePositions.add(fos.getChannel().position());
                writer.write((3 + page * 2) + " 0 obj\n");
                writer.write("<<\n");
                writer.write("/Type /Page\n");
                writer.write("/Parent 2 0 R\n");
                writer.write("/MediaBox [0 0 595 842]\n"); // A4 size
                writer.write("/Resources <<\n");
                writer.write("  /Font <<\n");
                writer.write("    /F1 " + fontObjNum + " 0 R\n");
                writer.write("  >>\n");
                writer.write(">>\n");
                writer.write("/Contents " + (4 + page * 2) + " 0 R\n");
                writer.write(">>\n");
                writer.write("endobj\n\n");
                
                // Content stream
                contentPositions.add(fos.getChannel().position());
                String content = createPageContent(wayPoints, page, totalPages);
                writer.write((4 + page * 2) + " 0 obj\n");
                writer.write("<<\n");
                writer.write("/Length " + content.length() + "\n");
                writer.write(">>\n");
                writer.write("stream\n");
                writer.write(content);
                writer.write("endstream\n");
                writer.write("endobj\n\n");
            }
            
            // Cross-reference table
            long xrefPos = fos.getChannel().position();
            writer.write("xref\n");
            writer.write("0 " + (fontObjNum + 1) + "\n");
            writer.write("0000000000 65535 f \n");
            writer.write(String.format("%010d 00000 n \n", catalogPos));
            writer.write(String.format("%010d 00000 n \n", pagesPos));
            
            for (int i = 0; i < totalPages; i++) {
                writer.write(String.format("%010d 00000 n \n", pagePositions.get(i)));
                writer.write(String.format("%010d 00000 n \n", contentPositions.get(i)));
            }
            writer.write(String.format("%010d 00000 n \n", fontPos));
            
            // Trailer
            writer.write("trailer\n");
            writer.write("<<\n");
            writer.write("/Size " + (fontObjNum + 1) + "\n");
            writer.write("/Root 1 0 R\n");
            writer.write(">>\n");
            writer.write("startxref\n");
            writer.write(xrefPos + "\n");
            writer.write("%%EOF\n");
            
            writer.flush();
        }
        
        // Show success message
        JOptionPane.showMessageDialog(null, 
            "PDF saved successfully to:\n" + outputFile.getAbsolutePath(), 
            "PDF Saved", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static String createPageContent(List<WayPoint> wayPoints, int pageIndex, int totalPages) {
        StringBuilder content = new StringBuilder();
        
        // Calculate which waypoints to show on this page
        int startIndex = pageIndex * POINTS_PER_PAGE;
        int endIndex = Math.min(startIndex + POINTS_PER_PAGE, wayPoints.size());
        
        // Grid dimensions - full page, no title, no margins
        int cols = 2;
        int rows = 4;
        double pageWidth = 595;  // A4 width
        double pageHeight = 842; // A4 height
        double cellWidth = pageWidth / cols;
        double cellHeight = pageHeight / rows;
        double startX = 0;
        double startY = pageHeight;
        
        // Only draw borders for cells that have waypoints
        int actualWaypoints = Math.min(endIndex - startIndex, POINTS_PER_PAGE);
        
        if (actualWaypoints > 0) {
            content.append("q\n"); // Save graphics state
            content.append("1 0 0 1 0 0 cm\n"); // Identity matrix
            content.append("0 0 0 RG\n"); // Black stroke color
            
            // Draw thick borders for point separation
            content.append("2 w\n"); // Thick line width for point separation
            
            // Draw borders only around cells that have waypoints
            for (int i = 0; i < actualWaypoints; i++) {
                int row = i / cols;
                int col = i % cols;
                
                double cellX = startX + col * cellWidth;
                double cellY = startY - row * cellHeight;
                
                // Draw cell border
                content.append(cellX).append(" ").append(cellY - cellHeight).append(" m\n");
                content.append(cellX + cellWidth).append(" ").append(cellY - cellHeight).append(" l\n");
                content.append(cellX + cellWidth).append(" ").append(cellY).append(" l\n");
                content.append(cellX).append(" ").append(cellY).append(" l\n");
                content.append("h\n");
                content.append("S\n");
            }
            
            // Draw thin internal table lines for property separation
            content.append("0.5 w\n"); // Thin line width for property separation
            for (int i = 0; i < actualWaypoints; i++) {
                int row = i / cols;
                int col = i % cols;
                
                double cellX = startX + col * cellWidth;
                double cellY = startY - row * cellHeight;
                
                // Draw internal table lines (3 lines to create 4 rows for the 4 properties)
                double tableRowHeight = cellHeight / 4;
                for (int tableRow = 1; tableRow < 4; tableRow++) {
                    double y = cellY - tableRow * tableRowHeight;
                    content.append(cellX).append(" ").append(y).append(" m\n");
                    content.append(cellX + cellWidth).append(" ").append(y).append(" l\n");
                    content.append("S\n");
                }
            }
            
            content.append("Q\n"); // Restore graphics state
        }
        
        
        // Add waypoint text in each cell - centered both horizontally and vertically
        for (int i = 0; i < actualWaypoints; i++) {
            WayPoint wp = wayPoints.get(startIndex + i);
            
            int row = i / cols;
            int col = i % cols;
            
            double cellX = startX + col * cellWidth;
            double cellY = startY - row * cellHeight;
            double tableRowHeight = cellHeight / 4;
            
            // Array of texts for each row
            String[] texts = {
                "Taškas: " + wp.getDisplayNumber(),
                "Koordinatės: " + wp.getCurrentCoordinates(),
                "Raidė: " + wp.getLetter(),
                "Sekančio taško koordinatės: " + wp.getNextPointCoordinates()
            };
            
            // Draw each text row centered within its table row
            for (int textRow = 0; textRow < 4; textRow++) {
                String text = texts[textRow];
                
                content.append("BT\n");
                content.append("/F1 10 Tf\n");
                
                // Calculate horizontal centering
                double textWidth = text.length() * 6; // Approximate character width for size 10 font
                double textX = cellX + (cellWidth - textWidth) / 2;
                
                // Calculate vertical centering within the table row
                double rowCenterY = cellY - (textRow * tableRowHeight) - (tableRowHeight / 2) + 3; // +3 for baseline adjustment
                
                // Position and draw text
                content.append(textX).append(" ").append(rowCenterY).append(" Td\n");
                content.append("(").append(text).append(") Tj\n");
                content.append("ET\n");
            }
        }
        
        return content.toString();
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
        private int totalPages;
        
        public WayPointPrintable(List<WayPoint> wayPoints) {
            this.wayPoints = wayPoints;
            this.totalPages = (int) Math.ceil((double) wayPoints.size() / POINTS_PER_PAGE);
            
            // Update next point coordinates for each waypoint
            for (int i = 0; i < wayPoints.size(); i++) {
                if (i < wayPoints.size() - 1) {
                    wayPoints.get(i).setNextPoint(wayPoints.get(i + 1));
                }
            }
        }
        
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
                throws PrinterException {
            
            if (pageIndex >= totalPages) {
                return NO_SUCH_PAGE;
            }
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Get printable area
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();
            double x = pageFormat.getImageableX();
            double y = pageFormat.getImageableY();
            
            // Draw title with page number
            g2d.setFont(HEADER_FONT);
            g2d.setColor(Color.BLACK);
            String title = "RouteMaker - Maršruto Taškai";
            if (totalPages > 1) {
                title += " (Page " + (pageIndex + 1) + " of " + totalPages + ")";
            }
            FontMetrics titleFm = g2d.getFontMetrics();
            double titleX = x + (pageWidth - titleFm.stringWidth(title)) / 2;
            g2d.drawString(title, (int) titleX, (int) (y + 30));
            
            // Calculate which waypoints to show on this page
            int startIndex = pageIndex * POINTS_PER_PAGE;
            int endIndex = Math.min(startIndex + POINTS_PER_PAGE, wayPoints.size());
            int waypointsOnThisPage = endIndex - startIndex;
            
            // Calculate grid layout (2x4)
            int cols = 2;
            int rows = 4;
            double cellWidth = pageWidth / cols;
            double cellHeight = (pageHeight - 60) / rows; // Reserve space for title
            
            // Draw waypoints in grid for this page
            for (int i = 0; i < waypointsOnThisPage; i++) {
                WayPoint wp = wayPoints.get(startIndex + i);
                
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
