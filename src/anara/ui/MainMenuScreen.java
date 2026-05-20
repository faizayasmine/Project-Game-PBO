package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.utils.AssetManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.BasicStroke;

public class MainMenuScreen extends BasePanel {

    private int hoveredBtn = -1;
    private Timer animTimer;
    private float animPhase = 0f;
    private float[] btnScale = {1f, 1f, 1f, 1f}; // animasi scale per button

    private static final String[] MENU_LABELS = {"Mulai Game", "Toko", "Data Pemain", "Pengaturan"};
  private static final Color[] BTN_COLORS = {
    new Color(0x59, 0x69, 0x00),  // Mulai Game
    new Color(0x59, 0x69, 0x00),  // Toko
    new Color(0x59, 0x69, 0x00),  // Data Pemain
    new Color(0x59, 0x69, 0x00),  // Pengaturan
};

    private static final int BTN_W = 200, BTN_H = 45;

    public MainMenuScreen() {
        setLayout(null);
        setupMouseListeners();
        startAnimation();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredBtn;
                hoveredBtn = -1;
                for (int i = 0; i < MENU_LABELS.length; i++) {
                    if (getBtnRect(i).contains(e.getPoint())) {
                        hoveredBtn = i;
                        break;
                    }
                }
                if (prev != hoveredBtn) {
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < MENU_LABELS.length; i++) {
                    if (getBtnRect(i).contains(e.getPoint())) {
                        handleMenuClick(i);
                        break;
                    }
                }
            }
        });
    }
private Rectangle getBtnRect(int index) {
    int cols = 2;
    int col = index % cols;
    int row = index / cols;
    
    // Geser ke kanan sejajar logo (logo ada di kiri ~80px)
    int startX = 80; // ← mulai dari kiri sejajar logo
    int startY = getHeight() - 200; // ← posisi vertikal
    
    int x = startX + col * (BTN_W + 15);
    int y = startY + row * (BTN_H + 14);
    return new Rectangle(x, y, BTN_W, BTN_H);
}
    private void handleMenuClick(int index) {
        switch (index) {
            case 0:
                GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAP_SELECT);
                break;
            case 1:
                GameEngine.getInstance().showScreen(GameEngine.SCREEN_SHOP);
                break;
            case 2:
                GameEngine.getInstance().showScreen(GameEngine.SCREEN_PLAYER_DATA);
                break;
            case 3:
                GameEngine.getInstance().showScreen(GameEngine.SCREEN_SETTING);
                break;
        }
    }

    private void startAnimation() {
        animTimer = new Timer(30, e -> {
            animPhase += 0.04f;
            if (animPhase > Math.PI * 2) {
                animPhase = 0;
            }

            // Animasi scale button saat hover
            for (int i = 0; i < btnScale.length; i++) {
                if (i == hoveredBtn) {
                    btnScale[i] = Math.min(1.08f, btnScale[i] + 0.01f);
                } else {
                    btnScale[i] = Math.max(1.0f, btnScale[i] - 0.01f);
                }
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // ===== BACKGROUND =====
        
        if (AssetManager.beranda != null) {
            g2.drawImage(AssetManager.beranda, 0, 0, w, h, null);
        } else if (AssetManager.beranda != null) {
            g2.drawImage(AssetManager.beranda, 0, 0, w, h, null);
        }else {
            // Fallback gradient
            GradientPaint bg = new GradientPaint(0, 0, new Color(10, 5, 25), 0, h, new Color(30, 15, 50));
            g2.setPaint(bg);
            g2.fillRect(0, 0, w, h);
        }

        // Overlay gelap tipis agar teks mudah dibaca
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRect(0, 0, w, h);

        // ===== PARTIKEL =====
        drawParticles(g2, w, h);

        // ===== LOGO / JUDUL =====
        drawTitle(g2, w, h);

        // ===== INFO PLAYER =====
        drawPlayerInfo(g2, w, h);

        // ===== BUTTONS =====
        for (int i = 0; i < MENU_LABELS.length; i++) {
            drawFancyButton(g2, i);
        }
        
        g2.dispose();
    }

    private void drawTitle(Graphics2D g2, int w, int h) {
        // Glow efek di belakang judul
        float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));
        g2.setColor(new Color(220, 175, 60, (int) (glow * 60)));
        g2.setFont(new Font("Serif", Font.BOLD, 72));
        FontMetrics fm = g2.getFontMetrics();
        
        // Garis dekorasi bawah judul
        g2.setColor(new Color(220, 175, 60, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(w / 2 - 200, 92, w / 2 + 200, 92);
        g2.setColor(new Color(220, 175, 60, 80));
        g2.drawLine(w / 2 - 150, 96, w / 2 + 150, 96);
    }

    private void drawPlayerInfo(Graphics2D g2, int w, int h) {
    PlayerData player = GameEngine.getInstance().getCurrentPlayer();
    if (player == null) return;

    // ===== "Selamat Datang, Nama!" — tengah atas, bawah logo, tanpa kotak =====
    String welcome = "Selamat Datang,";
    String name = player.getName() + "!";

    // Teks "Selamat Datang," — kecil, italic, warna krem
    g2.setFont(new Font("Georgia", Font.ITALIC, 13));
    g2.setColor(new Color(220, 200, 150, 200));
    FontMetrics fm1 = g2.getFontMetrics();
    int wx = w / 2 - fm1.stringWidth(welcome) / 2;
    // Sesuaikan y ini agar pas di bawah logo (sekitar 110-130)
    g2.drawString(welcome, wx, 112);

    // Teks nama — lebih besar, bold, warna emas dengan glow
    g2.setFont(new Font("Georgia", Font.BOLD, 17));
    FontMetrics fm2 = g2.getFontMetrics();
    int nx = w / 2 - fm2.stringWidth(name) / 2;

    // Shadow tipis
    g2.setColor(new Color(0, 0, 0, 120));
    g2.drawString(name, nx + 1, 133);

    // Teks nama dengan gradient emas
    GradientPaint nameGrad = new GradientPaint(
        nx, 115, new Color(255, 230, 100),
        nx + fm2.stringWidth(name), 133, new Color(200, 140, 40)
    );
    g2.setPaint(nameGrad);
    g2.drawString(name, nx, 132);

    // Garis dekorasi tipis di bawah nama (opsional, elegan)
    g2.setColor(new Color(220, 175, 60, 80));
    g2.setStroke(new BasicStroke(1f));
    int lineW = Math.max(fm2.stringWidth(name), fm1.stringWidth(welcome)) + 20;
    g2.drawLine(w / 2 - lineW / 2, 138, w / 2 + lineW / 2, 138);

    // ===== Koin & Diamond — tetap pojok kanan atas =====
    g2.setColor(new Color(0, 0, 0, 120));
    g2.fill(new RoundRectangle2D.Float(w - 240, 15, 225, 35, 12, 12));
    g2.setColor(new Color(220, 175, 60, 80));
    g2.setStroke(new BasicStroke(1f));
    g2.draw(new RoundRectangle2D.Float(w - 240, 15, 225, 35, 12, 12));

    // Icon koin
    g2.setColor(new Color(255, 200, 0));
    g2.fillOval(w - 230, 22, 18, 18);
    g2.setColor(new Color(200, 150, 0));
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawOval(w - 230, 22, 18, 18);

    g2.setFont(new Font("Georgia", Font.BOLD, 13));
    g2.setColor(new Color(255, 220, 80));
    g2.drawString(String.valueOf(player.getGold()), w - 206, 36);

    // Icon diamond
    g2.setColor(new Color(100, 200, 255));
    int[] dx = {w - 130, w - 120, w - 110, w - 120};
    int[] dy = {31, 22, 31, 40};
    g2.fillPolygon(dx, dy, 4);
    g2.setColor(new Color(60, 150, 220));
    g2.setStroke(new BasicStroke(1f));
    g2.drawPolygon(dx, dy, 4);

    g2.setColor(new Color(180, 230, 255));
    g2.drawString(String.valueOf(player.getDiamond()), w - 102, 36);
}
    private void drawFancyButton(Graphics2D g2, int index) {
        Rectangle r = getBtnRect(index);
        boolean hovered = hoveredBtn == index;
        float scale = btnScale[index];
        Color baseColor = BTN_COLORS[index];

        // Hitung posisi dengan scale dari tengah button
        int bx = (int) (r.x + r.width / 2 - (r.width * scale) / 2);
        int by = (int) (r.y + r.height / 2 - (r.height * scale) / 2);
        int bw = (int) (r.width * scale);
        int bh = (int) (r.height * scale);

        // Glow saat hover
        if (hovered) {
            g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 60));
            g2.fill(new RoundRectangle2D.Float(bx - 6, by - 6, bw + 12, bh + 12, 18, 18));
        }

        // Background button dengan gradient
        GradientPaint btnGrad = new GradientPaint(
                bx, by,
                hovered ? baseColor.brighter() : baseColor.darker(),
                bx, by + bh,
                baseColor.darker().darker()
        );
        g2.setPaint(btnGrad);
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 12, 12));

        // Border button
        g2.setColor(hovered ? baseColor.brighter() : new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
        g2.draw(new RoundRectangle2D.Float(bx, by, bw, bh, 12, 12));

        // Shine efek di atas button
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fill(new RoundRectangle2D.Float(bx + 4, by + 3, bw - 8, bh / 2 - 2, 8, 8));

        // Teks button
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int tx = bx + bw / 2 - fm.stringWidth(MENU_LABELS[index]) / 2;
        int ty = by + bh / 2 + fm.getAscent() / 2 - 2;

        // Shadow teks
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(MENU_LABELS[index], tx + 1, ty + 1);

        // Teks utama
        g2.setColor(hovered ? Color.WHITE : new Color(240, 230, 200));
        g2.drawString(MENU_LABELS[index], tx, ty);
    }

    private void drawParticles(Graphics2D g2, int w, int h) {
        for (int i = 0; i < 15; i++) {
            float x = (float) ((i * 137.5 + animPhase * 20) % w);
            float y = (float) (h - ((animPhase * 25 + i * 65) % h));
            float alpha = (float) (0.15 + 0.1 * Math.sin(animPhase + i));
            g2.setColor(new Color(220, 175, 60, (int) (alpha * 255)));
            g2.fillOval((int) x, (int) y, 3, 3);
        }
    }

    static void drawTopDownWarrior(Graphics2D g2, int cx, int cy, float phase) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(cx - 30, cy + 30, 60, 20);

        // Body armor
        g2.setColor(new Color(80, 55, 30));
        g2.fillOval(cx - 22, cy - 22, 44, 44);
        g2.setColor(new Color(110, 80, 45));
        g2.fillOval(cx - 14, cy - 16, 28, 20);

        // Head
        g2.setColor(new Color(200, 170, 130));
        g2.fillOval(cx - 12, cy - 36, 24, 24);

        // Ponytail
        g2.setColor(new Color(60, 40, 15));
        float swing = (float) Math.sin(phase) * 6f;
        int[] tailXPts = {cx - 4, cx + 4, cx + 2 + (int) swing, cx - 2 + (int) swing};
        int[] tailYPts = {cy - 36, cy - 36, cy + 7, cy + 7};
        g2.fillPolygon(tailXPts, tailYPts, 4);

        // Hair top
        g2.setColor(new Color(70, 50, 20));
        g2.fillOval(cx - 10, cy - 42, 20, 14);

        // Arms
        g2.setColor(new Color(80, 55, 30));
        g2.fillOval(cx - 32, cy - 10, 14, 20);
        g2.fillOval(cx + 18, cy - 10, 14, 20);

        // Sword
        g2.setColor(new Color(130, 130, 150));
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx - 10, cy - 30, cx + 16, cy + 30);
        g2.setColor(new Color(160, 130, 60));
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 14, cy + 26, cx + 20, cy + 38);

        // Feet
        g2.setColor(new Color(50, 35, 20));
        g2.fillOval(cx - 18, cy + 22, 14, 12);
        g2.fillOval(cx + 4, cy + 22, 14, 12);

        // Glow aura
        float glow = (float) (0.5 + 0.5 * Math.sin(phase * 1.3));
        g2.setColor(new Color(220, 175, 60, (int) (glow * 40)));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(cx - 28, cy - 40, 56, 80);
    }
}
