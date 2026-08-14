package anara.ui;

import javax.swing.*;
import java.awt.*;

public class LoadingScreen extends BasePanel {

    private static final long serialVersionUID = 1L;
    private Timer animTimer;
    private int dotCount = 0;
    private float barProgress = 0f;
    private int animTick = 0;

    public LoadingScreen() {
        startAnimation();
    }

    private void startAnimation() {
        barProgress = 0f;
        animTick = 0;
        animTimer = new Timer(40, e -> {
            animTick++;
            if (animTick % 10 == 0) dotCount = (dotCount + 1) % 4;
            barProgress = Math.min(1f, barProgress + 0.012f);
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;
        drawBackground(g2, w, h);

        // Draw spinning warrior symbol
        drawLoadingSymbol(g2, cx, cy - 80, animTick);

        // Loading text
        String dots = ".".repeat(dotCount);
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.setColor(COL_GOLD);
        String txt = "Memuat Dunia" + dots;
        g2.drawString(txt, cx - g2.getFontMetrics().stringWidth(txt) / 2, cy + 20);

        // Progress bar
        int barW = 400, barH = 16;
        int barX = cx - barW / 2, barY = cy + 45;

        g2.setColor(new Color(20, 15, 30));
        g2.fillRoundRect(barX, barY, barW, barH, 8, 8);

        // Animated fill
        GradientPaint gp = new GradientPaint(barX, barY, COL_RED, barX + barW, barY, COL_GOLD);
        g2.setPaint(gp);
        g2.fillRoundRect(barX, barY, (int)(barW * barProgress), barH, 8, 8);

        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(barX, barY, barW, barH, 8, 8);

        // Percent
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(COL_TEXT_DIM);
        String pct = (int)(barProgress * 100) + "%";
        g2.drawString(pct, cx - g2.getFontMetrics().stringWidth(pct) / 2, barY + barH + 18);

        // Flavor text
        g2.setFont(new Font("Serif", Font.ITALIC, 13));
        g2.setColor(new Color(120, 100, 70));
        String flavor = "\"Pedang yang tajam lahir dari tempaan yang menyakitkan...\"";
        g2.drawString(flavor, cx - g2.getFontMetrics().stringWidth(flavor) / 2, cy + 110);

        g2.dispose();
    }

    private void drawLoadingSymbol(Graphics2D g2, int cx, int cy, int tick) {
        double angle = Math.toRadians(tick * 3);
        int r = 45;

        // Rotating circle segments
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double a = angle + i * Math.PI / 4;
            float alpha = 1f - (i * 0.1f);
            g2.setColor(new Color(220, 175, 60, (int)(alpha * 255)));
            int x1 = cx + (int)(Math.cos(a) * (r - 10));
            int y1 = cy + (int)(Math.sin(a) * (r - 10));
            int x2 = cx + (int)(Math.cos(a) * r);
            int y2 = cy + (int)(Math.sin(a) * r);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Center sword icon
        g2.setColor(COL_GOLD);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy - 30, cx, cy + 30);
        g2.drawLine(cx - 12, cy - 10, cx + 12, cy - 10);
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(160, 130, 60));
        g2.drawLine(cx - 5, cy + 22, cx + 5, cy + 30);
    }
}