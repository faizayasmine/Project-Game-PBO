package anara.ui;

import anara.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MapSelectScreen extends BasePanel {
    private int hoveredMap = -1;

    private static final String[] MAP_NAMES = {
        "MAP I  — PASUKAN PENJAGA",
        "MAP II — BERTAHAN HIDUP",
        "MAP III — DUA MINI BOSS",
        "MAP IV  — PENGUASA KEGELAPAN"
    };

    private static final String[] MAP_DESC = {
        "Kalahkan 5 prajurit monster + Mini Boss\nyang diturunkan oleh Final Boss.", "Bertahan 15 detik dari serangan massal\nprajurit + Mini Boss Final Boss.",
        "Hadapi 2 Mini Boss secara bersamaan\nyang dikerahkan oleh Final Boss.", "FINAL BATTLE — Hadapi Final Boss\ndengan segala kekuatanmu!"
    };

    private static final Color[] MAP_COLORS = {
        new Color(30, 80, 30),
        new Color(90, 70, 15),
        new Color(70, 25, 80),
        new Color(110, 15, 15)
    };

    public MapSelectScreen() {
        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredMap;
                hoveredMap = -1;
                for (int i = 0; i < 4; i++) {
                    if (getMapRect(i).contains(e.getPoint())) { hoveredMap = i; break; }
                }
                if (prev != hoveredMap) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < 4; i++) {
                    if (getMapRect(i).contains(e.getPoint())) {
                        GameEngine.getInstance().showBattle(i + 1);
                        return;
                    }
                }
                // Back button
                Rectangle back = getBackRect();
                if (back.contains(e.getPoint())) {
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
                }
            }
        });
    }

    private Rectangle getMapRect(int i) {
        int cols = 2;
        int col = i % cols, row = i / cols;
        int x = 60 + col * 400, y = 160 + row * 200;
        return new Rectangle(x, y, 360, 170);
    }

    private Rectangle getBackRect() {
        return new Rectangle(30, 570, 120, 38);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        drawBackground(g2, w, h);

        drawTitle(g2, "PILIH MEDAN PERANG", cx, 90, 38);

        // Subtitle
        g2.setFont(new Font("Serif", Font.ITALIC, 13));
        g2.setColor(COL_TEXT_DIM);
        String sub = "Setiap medan memiliki tantangan berbeda";
        g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub) / 2, 112);

        for (int i = 0; i < 4; i++) {
            drawMapCard(g2, i);
        }

        // Back button
        drawButton(g2, 30, 570, 120, 38, "◄ KEMBALI", false, false);

        g2.dispose();
    }

    private void drawMapCard(Graphics2D g2, int i) {
        Rectangle r = getMapRect(i);
        boolean hov = hoveredMap == i;

        // Card background
        Color baseColor = MAP_COLORS[i];
        Color bgColor = new Color(
            Math.min(255, baseColor.getRed() + (hov ? 15 : 0)),
            Math.min(255, baseColor.getGreen() + (hov ? 15 : 0)),
            Math.min(255, baseColor.getBlue() + (hov ? 15 : 0)),
            hov ? 200 : 160
        );

        g2.setColor(new Color(10, 8, 15, 180));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Colored top accent bar
        g2.setColor(bgColor);
        g2.fillRoundRect(r.x, r.y, r.width, 8, 4, 4);

        // Border
        g2.setColor(hov ? baseColor.brighter() : new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 180));
        g2.setStroke(new BasicStroke(hov ? 2.5f : 1.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Map number badge
        g2.setColor(baseColor);
        g2.fillOval(r.x + 12, r.y + 15, 36, 36);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.drawString(String.valueOf(i + 1), r.x + 12 + (i < 3 ? 12 : 9), r.y + 40);

        // Map name
        g2.setFont(new Font("Serif", Font.BOLD, 15));
        g2.setColor(hov ? COL_GOLD_LIGHT : COL_TEXT);
        g2.drawString(MAP_NAMES[i], r.x + 58, r.y + 35);

        // Divider
        g2.setColor(new Color(100, 80, 30, 100));
        g2.drawLine(r.x + 58, r.y + 45, r.x + r.width - 15, r.y + 45);

        // Description
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(COL_TEXT_DIM);
        String[] lines = MAP_DESC[i].split("\n");
        for (int l = 0; l < lines.length; l++) {
            g2.drawString(lines[l], r.x + 20, r.y + 70 + l * 18);
        }

        // Difficulty dots
        int dots = i + 1;
        for (int d = 0; d < 4; d++) {
            g2.setColor(d < dots ? baseColor.brighter() : new Color(40, 35, 50));
            g2.fillOval(r.x + 20 + d * 18, r.y + r.height - 30, 12, 12);
        }
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("KESULITAN", r.x + 90, r.y + r.height - 20);

        // Play arrow on hover
        if (hov) {
            g2.setColor(COL_GOLD_LIGHT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("▶ MULAI", r.x + r.width - 70, r.y + r.height - 20);
        }

        // Final boss warning
        if (i == 3) {
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 11));
            g2.setColor(COL_RED_LIGHT);
            g2.drawString("⚠ BOSS TERAKHIR — PERSIAPKAN DIRIMU!", r.x + 20, r.y + r.height - 45);
        }
    }
}