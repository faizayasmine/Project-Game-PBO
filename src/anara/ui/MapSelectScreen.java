package anara.ui;

import anara.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MapSelectScreen extends BasePanel {
    private int hoveredMap = -1;
    private int currentIndex = 0; // Melacak map yang sedang aktif di tengah (0 sampai 3)
    private double animatedScroll = 0.0; // Menyimpan posisi scroll transisi desimal
    private javax.swing.Timer animationTimer; // Timer penggerak animasi
    
    private final Rectangle leftArrowRect = new Rectangle(30, 290, 45, 55);
    private final Rectangle rightArrowRect = new Rectangle(825, 290, 45, 55);

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
        setupAnimationTimer(); // Inisialisasi timer
    }
    
    private void setupAnimationTimer() {
        // Berjalan setiap 16 milidetik (setara ~60 FPS)
        animationTimer = new javax.swing.Timer(16, e -> {
            double diff = currentIndex - animatedScroll;
            
            // Jika jarak antara target dan posisi animasi masih jauh, geser perlahan
            if (Math.abs(diff) > 0.001) {
                animatedScroll += diff * 0.15; // 0.15 adalah kecepatan/kehalusan sliding
                repaint();
            } else {
                animatedScroll = currentIndex;
                animationTimer.stop(); // Hentikan timer jika sudah sampai tujuan
                repaint();
            }
        });
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
                // Klik Panah Kiri
                if (currentIndex > 0 && leftArrowRect.contains(e.getPoint())) {
                    currentIndex--;
                    if (!animationTimer.isRunning()) animationTimer.start(); // Jalankan animasi
                    return;
                }
                
                // Klik Panah Kanan
                if (currentIndex < 3 && rightArrowRect.contains(e.getPoint())) {
                    currentIndex++;
                    if (!animationTimer.isRunning()) animationTimer.start(); // Jalankan animasi
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
        int baseW = 260;
        int baseH = 380;
        int gap = 25; // Jarak antar kartu
        
        int centerX = 900 / 2 - baseW / 2; 
        int centerY = 150;
        
        // Hitung selisih jarak dinamis berdasarkan animatedScroll
        double offset = i - animatedScroll;
        int slotX = (int) (centerX + offset * (baseW + gap));
        
        // EFEK ANIMASI ZOOM: Kartu mengecil 8% jika bergeser ke samping
        double dist = Math.abs(offset);
        double scale = 1.0 - Math.min(1.0, dist) * 0.08; 
        
        int cardW = (int) (baseW * scale);
        int cardH = (int) (baseH * scale);
        
        // Penyesuaian koordinat XY agar kartu yang mengecil tetap presisi di tengah
        int x = slotX + (baseW - cardW) / 2;
        int y = centerY + (baseH - cardH) / 2;
        
        return new Rectangle(x, y, cardW, cardH);
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
            g2.setColor(new Color(40, 35, 25, 220));
            g2.fillRoundRect(leftArrowRect.x, leftArrowRect.y, leftArrowRect.width, leftArrowRect.height, 8, 8);
            g2.setColor(COL_GOLD_LIGHT);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(leftArrowRect.x, leftArrowRect.y, leftArrowRect.width, leftArrowRect.height, 8, 8);
            
            // Segitiga Panah Dalam Kotak
            g2.fillPolygon(new int[]{leftArrowRect.x + 28, leftArrowRect.x + 15, leftArrowRect.x + 28}, 
                           new int[]{leftArrowRect.y + 15, leftArrowRect.y + 27, leftArrowRect.y + 40}, 3);
        }

        // Gambar Navigasi Panah Kanan
        if (currentIndex < 3) {
            g2.setColor(new Color(40, 35, 25, 220));
            g2.fillRoundRect(rightArrowRect.x, rightArrowRect.y, rightArrowRect.width, rightArrowRect.height, 8, 8);
            g2.setColor(COL_GOLD_LIGHT);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(rightArrowRect.x, rightArrowRect.y, rightArrowRect.width, rightArrowRect.height, 8, 8);
            
            // Segitiga Panah Dalam Kotak
            g2.fillPolygon(new int[]{rightArrowRect.x + 17, rightArrowRect.x + 30, rightArrowRect.x + 17}, 
                           new int[]{rightArrowRect.y + 15, rightArrowRect.y + 27, rightArrowRect.y + 40}, 3);
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
        boolean isActive = (i == currentIndex);

        Color baseColor = MAP_COLORS[i];
        
        // BIAR TIDAK POLOS: Gunakan warna dasar bertema (Bukan Hitam/Gelap Polos)
        Color bgCard = new Color(
            Math.max(15, baseColor.getRed() - 20),
            Math.max(15, baseColor.getGreen() - 20),
            Math.max(15, baseColor.getBlue() - 20),
            245 // Opacity tinggi mendekati solid
        );

        g2.setColor(bgCard);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Efek highlight semi-transparan saat mouse di-hover
        if (hov) {
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        }

        // Border tebal mewah ala Gambar 2
        g2.setColor(isActive ? COL_GOLD_LIGHT : (hov ? Color.WHITE : new Color(255, 255, 255, 40)));
        g2.setStroke(new BasicStroke(isActive ? 3.0f : (hov ? 2.0f : 1.5f)));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // TAMBAHAN BIAR TIDAK POLOS: Label Status di Kanan Atas Kartu
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(160, 255, 160));
        g2.drawString("✓ TERBUKA", r.x + r.width - 95, r.y + 45);

        // Nomor Bulat Map
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillOval(r.x + 20, r.y + 25, 36, 36);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        String numStr = String.valueOf(i + 1);
        int numW = g2.getFontMetrics().stringWidth(numStr);
        g2.drawString(numStr, r.x + 20 + (36 - numW) / 2, r.y + 49);

        // Nama Map
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString(MAP_NAMES[i], r.x + 20, r.y + 95);

        // Garis Pembatas (Divider)
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawLine(r.x + 20, r.y + 110, r.x + r.width - 20, r.y + 110);

        // Deskripsi Teks (Putih Terang kontras tinggi)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(235, 235, 235));
        String[] lines = MAP_DESC[i].split("\n");
        for (int l = 0; l < lines.length; l++) {
            g2.drawString(lines[l], r.x + 20, r.y + 140 + l * 22);
        }

        // Peringatan Khusus Map IV
        if (i == 3) {
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 11));
            g2.setColor(new Color(255, 120, 120));
            g2.drawString("⚠ AWAS! BOSS TERAKHIR", r.x + 20, r.y + 245);
        }

        // Tingkat Kesulitan (Difficulty Dots warna Emas)
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(210, 210, 210));
        g2.drawString("KESULITAN", r.x + 20, r.y + r.height - 70);
        
        int dots = i + 1;
        for (int d = 0; d < 4; d++) {
            g2.setColor(d < dots ? new Color(255, 215, 0) : new Color(255, 255, 255, 45));
            g2.fillOval(r.x + 20 + d * 18, r.y + r.height - 60, 12, 12);
        }

        // Teks Aksi Klik di Bagian Bawah Kartu
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(hov ? COL_GOLD_LIGHT : Color.WHITE);
        String actionText = hov ? "▶ KLIK UNTUK MEMULAI" : "Klik untuk memilih";
        int textW = g2.getFontMetrics().stringWidth(actionText);
        g2.drawString(actionText, r.x + (r.width - textW) / 2, r.y + r.height - 25);
    }
}