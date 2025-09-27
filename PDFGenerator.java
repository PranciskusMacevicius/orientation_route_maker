import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

// Using Apache PDFBox for proper PDF generation with Unicode support
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

public class PDFGenerator {
    
    private static final int POINTS_PER_PAGE = 8; // 2x4 grid
    
    public static void generateRoutePDF(List<WayPoint> wayPoints) throws Exception {
        if (wayPoints.isEmpty()) {
            throw new Exception("No waypoints to generate PDF");
        }
        
        // Update next point coordinates for each waypoint
        for (int i = 0; i < wayPoints.size(); i++) {
            if (i < wayPoints.size() - 1) {
                wayPoints.get(i).setNextPoint(wayPoints.get(i + 1));
            }
        }
        
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
        
        // Generate actual PDF using PDFBox
        generatePDFWithPDFBox(wayPoints, outputFile);
        
        // Show success message
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, 
                "PDF saved successfully to:\n" + outputFile.getAbsolutePath(), 
                "PDF Saved",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    private static void generatePDFWithPDFBox(List<WayPoint> wayPoints, File outputFile) throws Exception {
        try (PDDocument document = new PDDocument()) {
            
            // Load Times New Roman font
            PDFont font;
            try {
                // Try to load Times New Roman font
                File fontFile = new File("C:/Windows/Fonts/times.ttf");
                if (fontFile.exists()) {
                    System.out.println("Loading Times New Roman font from: " + fontFile.getAbsolutePath());
                    font = PDType0Font.load(document, new FileInputStream(fontFile));
                    System.out.println("Times New Roman font loaded successfully!");
                } else {
                    // Try Times New Roman with different filename
                    fontFile = new File("C:/Windows/Fonts/timesnr.ttf");
                    if (fontFile.exists()) {
                        System.out.println("Loading Times New Roman font from: " + fontFile.getAbsolutePath());
                        font = PDType0Font.load(document, new FileInputStream(fontFile));
                        System.out.println("Times New Roman font loaded successfully!");
                    } else {
                        // Try Times New Roman with another filename
                        fontFile = new File("C:/Windows/Fonts/timesnewroman.ttf");
                        if (fontFile.exists()) {
                            System.out.println("Loading Times New Roman font from: " + fontFile.getAbsolutePath());
                            font = PDType0Font.load(document, new FileInputStream(fontFile));
                            System.out.println("Times New Roman font loaded successfully!");
                        } else {
                            throw new Exception("Times New Roman font not found. Checked times.ttf, timesnr.ttf, and timesnewroman.ttf in C:/Windows/Fonts/");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Font loading error: " + e.getMessage());
                throw new Exception("Could not load Times New Roman font: " + e.getMessage());
            }
            
            int totalPages = (int) Math.ceil((double) wayPoints.size() / POINTS_PER_PAGE);
            
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    
                    // Calculate which waypoints to show on this page
                    int startIndex = pageIndex * POINTS_PER_PAGE;
                    int endIndex = Math.min(startIndex + POINTS_PER_PAGE, wayPoints.size());
                    int actualWaypoints = endIndex - startIndex;
                    
                    // Grid dimensions - full page, no margins
        int cols = 2;
        int rows = 4;
                    float pageWidth = PDRectangle.A4.getWidth();
                    float pageHeight = PDRectangle.A4.getHeight();
                    float cellWidth = pageWidth / cols;
                    float cellHeight = pageHeight / rows;
                    
                    // Draw thick borders for point separation
                    contentStream.setLineWidth(2f);
                    for (int i = 0; i < actualWaypoints; i++) {
            int row = i / cols;
            int col = i % cols;
            
                        float cellX = col * cellWidth;
                        float cellY = pageHeight - (row + 1) * cellHeight;
                        
                        // Draw cell border
                        contentStream.addRect(cellX, cellY, cellWidth, cellHeight);
                        contentStream.stroke();
                        
                        // Draw additional thick bottom border if there's no waypoint below this cell
                        if (i + cols >= actualWaypoints) {
                            contentStream.setLineWidth(2f);
                            contentStream.moveTo(cellX, cellY);
                            contentStream.lineTo(cellX + cellWidth, cellY);
                            contentStream.stroke();
                        }
                    }
                    
                    // Draw thin internal lines for property separation
                    contentStream.setLineWidth(0.5f);
                    for (int i = 0; i < actualWaypoints; i++) {
                        WayPoint wp = wayPoints.get(startIndex + i);
                        boolean isFinish = wp.getNumber().equals("F");
                        
                        int row = i / cols;
                        int col = i % cols;
                        
                        float cellX = col * cellWidth;
                        float cellY = pageHeight - (row + 1) * cellHeight;
                        float tableRowHeight = cellHeight / 4;
                        
                        if (isFinish) {
                            // For finish point: 2 thin lines, then 1 thick line RIGHT after "Raidė"  
                            // Text positioning: (3-textRow) * tableRowHeight + tableRowHeight/2
                            // We need lines at the row boundaries, not through the text
                            
                            // Thin line after "Taškas" row (at 3 * tableRowHeight)
                            float y1 = cellY + 3 * tableRowHeight;
                            contentStream.moveTo(cellX, y1);
                            contentStream.lineTo(cellX + cellWidth, y1);
                            contentStream.stroke();
                            
                            // Thin line after "Koordinatės" row (at 2 * tableRowHeight)
                            float y2 = cellY + 2 * tableRowHeight;
                            contentStream.moveTo(cellX, y2);
                            contentStream.lineTo(cellX + cellWidth, y2);
                            contentStream.stroke();
                            
                            // THICK line RIGHT after "Raidė" row (at 1 * tableRowHeight)
                            contentStream.setLineWidth(2f);
                            float thickY = cellY + 1 * tableRowHeight;
                            contentStream.moveTo(cellX, thickY);
                            contentStream.lineTo(cellX + cellWidth, thickY);
                            contentStream.stroke();
                            contentStream.setLineWidth(0.5f); // Reset to thin
                        } else {
                            // For other points, draw 3 lines to create 4 rows
                            for (int tableRow = 1; tableRow < 4; tableRow++) {
                                float y = cellY + tableRow * tableRowHeight;
                                contentStream.moveTo(cellX, y);
                                contentStream.lineTo(cellX + cellWidth, y);
                                contentStream.stroke();
                            }
                        }
                    }
                    
                    // Add text content with proper Lithuanian characters
                    contentStream.setFont(font, 14);
                    
                    for (int i = 0; i < actualWaypoints; i++) {
                        WayPoint wp = wayPoints.get(startIndex + i);
                
                int row = i / cols;
                int col = i % cols;
                
                        float cellX = col * cellWidth;
                        float cellY = pageHeight - (row + 1) * cellHeight;
                        float tableRowHeight = cellHeight / 4;
                        
                        // Array of texts for each row - check if it's finish point
                        String[] texts;
                        boolean isFinish = wp.getNumber().equals("F");
                        
                        if (isFinish) {
                            // For finish point, only show 3 rows (no next coordinates)
                            texts = new String[]{
                                "Taškas: " + wp.getDisplayNumber(),
                                "Koordinatės: " + wp.getCurrentCoordinates(),
                                "Raidė: " + wp.getLetter()
                            };
                        } else {
                            // For other points, show all 4 rows
                            texts = new String[]{
                                "Taškas: " + wp.getDisplayNumber(),
                                "Koordinatės: " + wp.getCurrentCoordinates(),
                                "Raidė: " + wp.getLetter(),
                                "Sekančio taško koordinatės: " + wp.getNextPointCoordinates()
                            };
                        }
                        
                        // Draw each text row centered within its table row
                        for (int textRow = 0; textRow < texts.length; textRow++) {
                            String text = texts[textRow];
                            
                            // Calculate text positioning (centered)
                            float textWidth = font.getStringWidth(text) / 1000f * 14;
                            float textX = cellX + (cellWidth - textWidth) / 2;
                            float textY = cellY + (3 - textRow) * tableRowHeight + tableRowHeight / 2 - 3;
                            
                            contentStream.beginText();
                            contentStream.newLineAtOffset(textX, textY);
                            contentStream.showText(text);
                            contentStream.endText();
                        }
                    }
                }
            }
            
            document.save(outputFile);
        }
    }
}