package anara.ui;

import anara.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MapSelectScreen extends BasePanel {
    private int hoveredMap = -1;
    private int currentIndex = 0; // Melacak map yang sedang aktif di tengah (0 sampai 3)
    private final Rectangle leftArrowRect = new Rectangle(5, 310, 30, 60);
    private final Rectangle rightArrowRect = new Rectangle(865, 310, 30, 60);

    private static final String[] MAP_NAMES = {
        "MAP I  — PASUKAN PENJAGA",
        "MAP II — BERTAHAN HIDUP",
        "MAP III — DUA MINI BOSS",
        "MAP IV  — PENGUASA KEGELAPAN"
    };

    private static final String[] MAP_DESC = {
        "Kalahkan 5 prajurit monster + Mini Boss\nyang diturunkan oleh Final Boss.",
        "Bertahan 15 detik dari serangan massal\nprajurit + Mini Boss Final Boss.",
        "Hadapi 2 Mini Boss secara bersamaan\nyang dikerahkan oleh Final Boss.",
        "FINAL BATTLE — Hadapi Final Boss\ndengan segala kekuatanmu!"
    };

    private static final Color[] MAP_COLORS = {
        new Color(60, 120, 60),
        new Color(120, 100, 40),
        new Color(120, 60, 40),
        new Color(120, 20, 20)
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
                    if (getMapRect(i).contains(e.getPoint())) { 
                        hoveredMap = i; 
                        break; 
                    }
                }
                if (prev != hoveredMap) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Deteksi klik pada tombol panah Kiri
                if (currentIndex > 0 && leftArrowRect.contains(e.getPoint())) {
                    currentIndex--;
                    repaint();
                    return;
                }
                
                // Deteksi klik pada tombol panah Kanan
                if (currentIndex < 3 && rightArrowRect.contains(e.getPoint())) {
                    currentIndex++;
                    repaint();
                    return;
                }

                // Deteksi klik pada kartu map
                for (int i = 0; i < 4; i++) {
                    if (getMapRect(i).contains(e.getPoint())) {
                        GameEngine.getInstance().showBattle(i + 1);
                        return;
                    }
                }
                
                // Tombol Kembali
                Rectangle back = getBackRect();
                if (back.contains(e.getPoint())) {
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
                }
            }
        });
    }

    private Rectangle getMapRect(int i) {
        int cardW = 260; // Ukuran lebar kartu vertikal
        int cardH = 380; // Ukuran tinggi kartu vertikal
        int gap = 20;    // Jarak antar kartu
        
        // Menghitung titik tengah layar (Frame width = 900)
        int centerX = 900 / 2 - cardW / 2; 
        int centerY = 150;
        
        // Geser posisi X kartu berdasarkan selisih indeks dengan currentIndex
        int offset = i - currentIndex;
        int x = centerX + offset * (cardW + gap);
        
        return new Rectangle(x, centerY, cardW, cardH);
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

        drawTitle(g2, "PILIH MEDAN PERANG", cx, 80, 38);

        // Subtitle
        g2.setFont(new Font("Serif", Font.ITALIC, 13));
        g2.setColor(COL_TEXT_DIM);
        String sub = "Setiap medan memiliki tantangan berbeda";
        g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub) / 2, 105);

        // Gambar semua kartu map (posisinya otomatis berjejer horizontal)
        for (int i = 0; i < 4; i++) {
            drawMapCard(g2, i);
        }

        // Gambar Navigasi Panah Kiri
        if (currentIndex > 0) {
            g2.setColor(COL_GOLD_LIGHT);
            g2.fillPolygon(new int[]{25, 10, 25}, new int[]{320, 340, 360}, 3);
        }

        // Gambar Navigasi Panah Kanan
        if (currentIndex < 3) {
            g2.setColor(COL_GOLD_LIGHT);
            g2.fillPolygon(new int[]{875, 890, 875}, new int[]{320, 340, 360}, 3);
        }

        // Gambar Indikator Titik Halaman (Page Indicator Dots)
        int dotsX = cx - (4 * 20) / 2;
        for (int d = 0; d < 4; d++) {
            if (d == currentIndex) {
                g2.setColor(COL_GOLD_LIGHT);
                g2.fillOval(dotsX + d * 20, 555, 10, 10);
            } else {
                g2.setColor(new Color(100, 90, 80, 150));
                g2.drawOval(dotsX + d * 20, 555, 10, 10);
            }
        }

        // Tombol Kembali
        drawButton(g2, 30, 570, 120, 38, "◄ KEMBALI", false, false);

        g2.dispose();
    }

    private void drawMapCard(Graphics2D g2, int i) {
        Rectangle r = getMapRect(i);
        boolean hov = hoveredMap == i;

        // Background kartu
        Color baseColor = MAP_COLORS[i];
        Color bgColor = new Color(
            Math.min(255, baseColor.getRed() + (hov ? 15 : 0)),
            Math.min(255, baseColor.getGreen() + (hov ? 15 : 0)),
            Math.min(255, baseColor.getBlue() + (hov ? 15 : 0)),
            hov ? 200 : 160
        );

        g2.setColor(new Color(10, 8, 15, 220));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Accent bar atas kartu
        g2.setColor(bgColor);
        g2.fillRoundRect(r.x, r.y, r.width, 8, 4, 4);

        // Border kartu (lebih terang jika di-hover atau sedang aktif di tengah)
        boolean isActive = (i == currentIndex);
        g2.setColor(isActive ? COL_GOLD_LIGHT : (hov ? baseColor.brighter() : new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 120)));
        g2.setStroke(new BasicStroke(isActive ? 2.5f : (hov ? 2.0f : 1.0f)));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Nomor Badge Map
        g2.setColor(baseColor);
        g2.fillOval(r.x + 20, r.y + 25, 36, 36);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.drawString(String.valueOf(i + 1), r.x + 32, r.y + 50);

        // Nama Map
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(hov ? COL_GOLD_LIGHT : COL_TEXT);
        g2.drawString(MAP_NAMES[i], r.x + 20, r.y + 90);

        // Garis Pembatas (Divider)
        g2.setColor(new Color(100, 80, 30, 100));
        g2.drawLine(r.x + 20, r.y + 105, r.x + r.width - 20, r.y + 105);

        // Deskripsi Map
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(COL_TEXT_DIM);
        String[] lines = MAP_DESC[i].split("\n");
        for (int l = 0; l < lines.length; l++) {
            g2.drawString(lines[l], r.x + 20, r.y + 135 + l * 20);
        }

        // Peringatan Boss Terakhir (Khusus Map IV)
        if (i == 3) {
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 11));
            g2.setColor(COL_RED_LIGHT);
            g2.drawString("⚠ PERSIAPKAN DIRIMU!", r.x + 20, r.y + 230);
        }

        // Tingkat Kesulitan (Difficulty Dots)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("KESULITAN", r.x + 20, r.y + r.height - 65);
        
        int dots = i + 1;
        for (int d = 0; d < 4; d++) {
            g2.setColor(d < dots ? baseColor.brighter() : new Color(40, 35, 50));
            g2.fillOval(r.x + 20 + d * 18, r.y + r.height - 55, 12, 12);
        }

        // Teks Petunjuk Klik di Bagian Bawah Kartu
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.setColor(hov ? COL_GOLD_LIGHT : COL_TEXT_DIM);
        String actionText = hov ? "▶ CLIK UNTUK MEMULAI" : "Klik untuk memilih";
        int textW = g2.getFontMetrics().stringWidth(actionText);
        g2.drawString(actionText, r.x + (r.width - textW) / 2, r.y + r.height - 20);
    }
}