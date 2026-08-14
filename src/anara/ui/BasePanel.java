package anara.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public abstract class BasePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // === THEME PALETTE ===
    protected static final Color COL_BG_DARK     = new Color(10, 8, 15);
    protected static final Color COL_BG_MID      = new Color(20, 16, 30);
    protected static final Color COL_GOLD        = new Color(220, 175, 60);
    protected static final Color COL_GOLD_LIGHT  = new Color(255, 220, 100);
    protected static final Color COL_RED         = new Color(180, 40, 40);
    protected static final Color COL_RED_LIGHT   = new Color(220, 80, 80);
    protected static final Color COL_TEXT        = new Color(230, 220, 200);
    protected static final Color COL_TEXT_DIM    = new Color(150, 140, 120);
    protected static final Color COL_PANEL_BG    = new Color(15, 12, 22, 210);
    protected static final Color COL_BORDER      = new Color(100, 80, 30);
    protected static final Color COL_GREEN       = new Color(60, 180, 80);
    protected static final Color COL_HP_BAR      = new Color(200, 40, 40);
    protected static final Color COL_HP_BG       = new Color(40, 10, 10);

    public BasePanel() {
        setBackground(COL_BG_DARK);
    }

    // Draw decorative dark background with rune-like patterns
    protected void drawBackground(Graphics2D g, int w, int h) {
        // Base gradient
        GradientPaint gp = new GradientPaint(0, 0, COL_BG_DARK, w, h, new Color(25, 15, 35));
        g.setPaint(gp);
        g.fillRect(0, 0, w, h);

        // Subtle grid lines (rune map feel)
        g.setColor(new Color(60, 45, 80, 25));
        g.setStroke(new BasicStroke(1f));
        for (int x = 0; x < w; x += 40) g.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 40) g.drawLine(0, y, w, y);

        // Corner ornaments
        drawCornerOrnament(g, 20, 20, false, false);
        drawCornerOrnament(g, w - 20, 20, true, false);
        drawCornerOrnament(g, 20, h - 20, false, true);
        drawCornerOrnament(g, w - 20, h - 20, true, true);

        // Outer border
        g.setColor(COL_BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(8, 8, w - 16, h - 16);
        g.setColor(new Color(100, 80, 30, 80));
        g.setStroke(new BasicStroke(1f));
        g.drawRect(12, 12, w - 24, h - 24);
    }

    private void drawCornerOrnament(Graphics2D g, int x, int y, boolean flipX, boolean flipY) {
        g.setColor(COL_GOLD);
        g.setStroke(new BasicStroke(1.5f));
        int dx = flipX ? -1 : 1;
        int dy = flipY ? -1 : 1;
        g.drawLine(x, y, x + dx * 30, y);
        g.drawLine(x, y, x, y + dy * 30);
        g.drawLine(x + dx * 10, y, x + dx * 10, y + dy * 10);
        g.drawLine(x, y + dy * 10, x + dx * 10, y + dy * 10);
    }

    // Draw stylized title text
    protected void drawTitle(Graphics2D g, String title, int x, int y, float size) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font titleFont = new Font("Serif", Font.BOLD, (int) size);
        g.setFont(titleFont);

        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(title);

        // Shadow layers
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(title, x - tw / 2 + 3, y + 3);
        g.setColor(new Color(100, 60, 10, 120));
        g.drawString(title, x - tw / 2 + 1, y + 1);

        // Gold gradient fill
        GradientPaint gp = new GradientPaint(0, y - size, COL_GOLD_LIGHT, 0, y, COL_GOLD);
        g.setPaint(gp);
        g.drawString(title, x - tw / 2, y);
    }

    // Draw stylized button
    protected void drawButton(Graphics2D g, int x, int y, int w, int h, String label, boolean hovered, boolean active) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = active ? new Color(60, 45, 10, 220)
                 : hovered ? new Color(40, 30, 8, 200)
                 : new Color(20, 16, 6, 180);
        Color border = active ? COL_GOLD_LIGHT : hovered ? COL_GOLD : COL_BORDER;

        // Background
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, 6, 6);

        // Border
        g.setColor(border);
        g.setStroke(new BasicStroke(hovered ? 2f : 1.5f));
        g.drawRoundRect(x, y, w, h, 6, 6);

        // Side decorators on hover
        if (hovered) {
            g.setColor(COL_GOLD_LIGHT);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x + 6, y + h / 2, x + 14, y + h / 2);
            g.drawLine(x + w - 14, y + h / 2, x + w - 6, y + h / 2);
        }

        // Label
        g.setFont(new Font("Serif", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(label)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(hovered ? COL_GOLD_LIGHT : COL_TEXT);
        g.drawString(label, tx, ty);
    }

    // Draw HP bar
    protected void drawHPBar(Graphics2D g, int x, int y, int w, int h, float ratio, String label) {
        g.setColor(COL_HP_BG);
        g.fillRoundRect(x, y, w, h, 4, 4);
        if (ratio > 0) {
            Color barColor = ratio > 0.5f ? COL_HP_BAR
                           : ratio > 0.25f ? new Color(200, 120, 40)
                           : new Color(220, 40, 40);
            g.setColor(barColor);
            g.fillRoundRect(x, y, (int)(w * ratio), h, 4, 4);
        }
        g.setColor(new Color(80, 20, 20));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x, y, w, h, 4, 4);
        if (label != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + fm.getAscent() - 1);
        }
    }

    // Draw panel box
    protected void drawPanel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(COL_PANEL_BG);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(COL_BORDER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 10, 10);
    }
}
