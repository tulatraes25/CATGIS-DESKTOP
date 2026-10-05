package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LayoutTable implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;

    private List<String[]> rows = new ArrayList<>();
    private List<String[]> columns = new ArrayList<>();
    private int[] colWidths;
    private Font font = new Font("SansSerif", Font.PLAIN, 9);
    private Font headerFont = new Font("SansSerif", Font.BOLD, 9);
    private Color textColor = new Color(50, 55, 65);
    private Color headerBg = new Color(45, 55, 72);
    private Color headerTextColor = Color.WHITE;
    private Color borderColor = new Color(170, 180, 190);
    private Color rowAltBg = new Color(245, 247, 250);
    private Color rowSeparatorColor = new Color(220, 225, 232);
    private float borderThickness = 0.5f;
    private double cellPadMm = 1.0;
    private boolean firstRowIsHeader = true;
    private boolean showBorders = true;
    private boolean alternateRows = true;
    private int maxVisibleRows = 20;

    public LayoutTable(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id; this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    public void loadCsv(File file) throws Exception {
        rows.clear();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                rows.add(parseCsvLine(line));
            }
        }
        columns.clear();
        if (!rows.isEmpty()) {
            String[] header = rows.get(0);
            for (String h : header) {
                String[] col = new String[Math.min(rows.size(), maxVisibleRows + 1)];
                for (int r = 0; r < col.length && r < rows.size(); r++) {
                    String[] row = rows.get(r);
                    col[r] = row.length > h.length() ? row[r] : "";
                }
                columns.add(col);
            }
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }

    public void setFirstRowIsHeader(boolean b) { firstRowIsHeader = b; }
    public void setShowBorders(boolean b) { showBorders = b; }
    public void setAlternateRows(boolean b) { alternateRows = b; }
    public void setFont(Font f) { font = f; }
    public void setHeaderFont(Font f) { headerFont = f; }
    public void setTextColor(Color c) { textColor = c; }
    public void setHeaderBg(Color c) { headerBg = c; }
    public void setBorderColor(Color c) { borderColor = c; }
    public int getMaxVisibleRows() { return maxVisibleRows; }
    public void setMaxVisibleRows(int n) { maxVisibleRows = n; }
    public List<String[]> getRows() { return rows; }

    /**
     * Create a LayoutTable directly from string data (no CSV file needed).
     * @param id unique element id
     * @param xMm X position in mm
     * @param yMm Y position in mm
     * @param data String[][] where first row is header
     * @return new LayoutTable populated with the given data
     */
    public static LayoutTable createFromData(String id, double xMm, double yMm, String[][] data) {
        LayoutTable table = new LayoutTable(id, xMm, yMm, 180, 60);
        table.loadFromData(data);
        return table;
    }

    /**
     * Load table data from a String[][] array (first row is header).
     * Automatically computes column widths and adjusts table bounds.
     */
    public void loadFromData(String[][] data) {
        rows.clear();
        columns.clear();
        if (data == null || data.length == 0) return;
        for (String[] row : data) {
            if (row != null) {
                rows.add(row);
            }
        }
        int colCount = 0;
        for (String[] r : rows) colCount = Math.max(colCount, r.length);
        colWidths = new int[colCount];
        for (int c = 0; c < colCount; c++) {
            int maxW = 60;
            for (int r = 0; r < rows.size(); r++) {
                String[] row = rows.get(r);
                String cell = c < row.length ? row[c] : "";
                maxW = Math.max(maxW, cell.length() * 7 + 10);
            }
            colWidths[c] = Math.min(maxW, 200);
        }
        int totalW = 0;
        for (int w : colWidths) totalW += w;
        boundsMm.width = Math.max(boundsMm.width, totalW * 0.3528 + 4);
        boundsMm.height = Math.max(boundsMm.height, rows.size() * 6.0);
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public void setName(String n) { name = n; }
    @Override public Rectangle2D.Double getBoundsMm() { return boundsMm; }
    @Override public void setBoundsMm(double x, double y, double w, double h) { boundsMm.setRect(x, y, w, h); }
    @Override public int getZOrder() { return zOrder; }
    @Override public void setZOrder(int z) { zOrder = z; }
    @Override public boolean isVisible() { return visible; }
    @Override public void setVisible(boolean v) { visible = v; }
    @Override public boolean isLocked() { return locked; }
    @Override public void setLocked(boolean l) { locked = l; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean s) { selected = s; }
    @Override public boolean containsMm(double x, double y) { return boundsMm.contains(x, y); }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        if (rows.isEmpty()) return;
        int xPx = ctx.mmToPxInt(boundsMm.x);
        int yPx = ctx.mmToPxInt(boundsMm.y);
        int wPx = ctx.mmToPxInt(boundsMm.width);
        int cellPadPx = ctx.mmToPxInt(cellPadMm);
        int visibleRows = Math.min(rows.size(), maxVisibleRows);
        if (firstRowIsHeader && visibleRows == 1 && rows.size() > 1) visibleRows = Math.min(2, rows.size());

        int colCount = 0;
        for (String[] r : rows) colCount = Math.max(colCount, r.length);
        int[] cw = colWidths != null && colWidths.length == colCount ? colWidths : new int[colCount];
        int totalCw = 0;
        Font sFont = font.deriveFont((float) (font.getSize() * ctx.getDpi() / 72));
        Font sHdrFont = headerFont.deriveFont((float) (headerFont.getSize() * ctx.getDpi() / 72));
        FontMetrics fm = g2.getFontMetrics(sFont);

        if (colWidths == null) {
            for (int c = 0; c < colCount; c++) {
                int max = 40;
                for (int r = 0; r < visibleRows && r < rows.size(); r++) {
                    String[] row = rows.get(r);
                    max = Math.max(max, fm.stringWidth(c < row.length ? row[c] : "") + cellPadPx * 2);
                }
                cw[c] = max;
                totalCw += max;
            }
            if (totalCw > wPx) {
                double ratio = (double) wPx / totalCw;
                for (int c = 0; c < colCount; c++) cw[c] = (int) (cw[c] * ratio);
            }
        }

        int rowH = fm.getHeight() + cellPadPx * 2;
        g2.setColor(Color.WHITE);
        g2.fillRect(xPx, yPx, wPx, rowH * visibleRows + 2);
        g2.setColor(borderColor);

        int cy = yPx;
        for (int r = 0; r < visibleRows && r < rows.size(); r++) {
            String[] row = rows.get(r);
            boolean isHeader = firstRowIsHeader && r == 0;

            if (alternateRows && !isHeader && r % 2 == 0) {
                g2.setColor(rowAltBg);
                g2.fillRect(xPx, cy, wPx, rowH);
            }
            if (isHeader) {
                g2.setColor(headerBg);
                g2.fillRect(xPx, cy, wPx, rowH);
            }

            g2.setFont(isHeader ? sHdrFont : sFont);
            g2.setColor(isHeader ? headerTextColor : textColor);
            FontMetrics useFm = isHeader ? g2.getFontMetrics() : fm;
            int cx = xPx + cellPadPx;
            for (int c = 0; c < colCount; c++) {
                String cell = c < row.length ? row[c] : "";
                if (cell.length() > 0) {
                    String clipped = clipText(cell, useFm, cw[c] - cellPadPx * 2);
                    g2.drawString(clipped, cx, cy + rowH - cellPadPx - 1);
                }
                cx += cw[c];
            }
            // Row separator line
            if (!isHeader && r > 0 && showBorders) {
                g2.setColor(rowSeparatorColor);
                g2.setStroke(new BasicStroke(0.3f));
                g2.drawLine(xPx, cy, xPx + wPx, cy);
            }
            cy += rowH;
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderThickness));
        g2.drawRect(xPx, yPx, wPx, rowH * visibleRows);
        if (showBorders) {
            int cx = xPx;
            for (int c = 0; c < colCount; c++) {
                cx += cw[c];
                g2.drawLine(cx, yPx, cx, yPx + rowH * visibleRows);
            }
        }
    }

    private String clipText(String s, FontMetrics fm, int maxW) {
        if (fm.stringWidth(s) <= maxW) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append(s.charAt(i));
            if (fm.stringWidth(sb.toString()) > maxW - fm.stringWidth("...")) {
                return sb.substring(0, sb.length() - 1) + "...";
            }
        }
        return sb.toString();
    }
}
