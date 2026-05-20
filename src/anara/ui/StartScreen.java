package anara.ui;

import anara.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * StartScreen - Layar pembuka game "Survival Sylvan"
 * Tampil sebelum LoginScreen. Berisi judul, tagline, dan tombol mulai.
 * Wireframe: Semua placeholder siap diganti asset gambar.
 *
 * Cara integrasi ke GameEngine:
 *   - Tambahkan konstanta: public static final int SCREEN_START = 0;
 *   - Ganti urutan tampilan awal ke SCREEN_START
 *   - Dari StartScreen -> showScreen(GameEngine.SCREEN_LOGIN)
 */
public class StartScreen extends BasePanel {

    // ── Animasi ──────────────────────────────────────────────────────────────
    private Timer animTimer;
    private float glowPhase   = 0f;
    private float floatPhase  = 0f;
    private float starPhase   = 0f;
    private int[] starX, starY;
    private float[] starBright;

    // ── Background image (opsional) ──────────────────────────────────────────
    // Uncomment dan sesuaikan path saat asset sudah siap:
    // private Image bgImage;
    // private Image characterImage;

    public StartScreen() {
        setLayout(null);
        setOpaque(true);
        initStars();
        // loadAssets(); // Uncomment saat asset tersedia
        startAnimation();
        setupButton();
    }

    // ── Asset loader (opsional) ──────────────────────────────────────────────
    /*
    private void loadAssets() {
        bgImage        = new ImageIcon(getClass().getResource("/assets/images/sylvan_bg.png")).getImage();
        characterImage = new ImageIcon(getClass().getResource("/assets/images/character_start.png")).getImage();
    }
    */

    // ── Bintang acak di background ───────────────────────────────────────────
    private void initStars() {
        int n = 120;
        starX      = new int[n];
        starY      = new int[n];
        starBright = new float[n];
        Random rnd = new Random(42);
        for (int i = 0; i < n; i++) {
            starX[i]      = rnd.nextInt(900);
            starY[i]      = rnd.nextInt(300);
            starBright[i] = 0.3f + rnd.nextFloat() * 0.7f;
        }
    }

    // ── Tombol "Mulai Petualangan" ───────────────────────────────────────────
    private void setupButton() {
        int cx = 900 / 2;
        JButton btnStart = new JButton("Mulai Petualangan  >>>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hov = getModel().isRollover();
                boolean prs = getModel().isPressed();

                // Background tombol hijau seperti gambar referensi
                Color bgTop = hov ? new Color(120, 180, 60) : new Color(90, 145, 40);
                Color bgBot = hov ? new Color(70, 120, 25) : new Color(50, 95, 15);
                GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, getHeight(), bgBot);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border
                g2.setColor(new Color(180, 220, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                // Pressed offset
                int dy = prs ? 2 : 0;

                // Teks pixel-style
                g2.setFont(new Font("Monospaced", Font.BOLD, 16));
                g2.setColor(new Color(30, 20, 10));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                g2.drawString(getText(), tx, (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + dy);

                g2.dispose();
            }
        };
        btnStart.setBounds(cx - 130, 400, 260, 48);
        btnStart.setOpaque(false);
        btnStart.setContentAreaFilled(false);
        btnStart.setBorderPainted(false);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> {
            // Ke LayarLogin
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_LOGIN);
        });
        add(btnStart);
    }

    // ── Animasi timer ────────────────────────────────────────────────────────
    private void startAnimation() {
        animTimer = new Timer(30, e -> {
            glowPhase  += 0.04f;
            floatPhase += 0.03f;
            starPhase  += 0.02f;
            if (glowPhase  > Math.PI * 2) glowPhase  = 0;
            if (floatPhase > Math.PI * 2) floatPhase = 0;
            if (starPhase  > Math.PI * 2) starPhase  = 0;
            repaint();
        });
        animTimer.start();
    }

    public void stopAnimation() {
        if (animTimer != null) animTimer.stop();
    }

    // ── Render utama ─────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // ── 1. BACKGROUND ────────────────────────────────────────────────────
        // Saat bgImage tersedia, ganti blok ini:
        //   g2.drawImage(bgImage, 0, 0, w, h, this);
        // Sekarang: gradient placeholder ungu-biru malam
        GradientPaint bg = new GradientPaint(
            0, 0,   new Color(18, 10, 35),
            0, h/2, new Color(35, 20, 60),
            true
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Lapisan bawah: hijau gelap (hutan)
        GradientPaint groundGrad = new GradientPaint(
            0, h * 0.55f, new Color(15, 40, 15),
            0, h,         new Color(5, 15, 5)
        );
        g2.setPaint(groundGrad);
        g2.fillRect(0, (int)(h * 0.55f), w, h);

        // Bukit silhouette kiri
        drawHillSilhouette(g2, w, h);

        // ── 2. BINTANG ───────────────────────────────────────────────────────
        for (int i = 0; i < starX.length; i++) {
            float b = starBright[i] * (0.6f + 0.4f * (float) Math.sin(starPhase + i * 0.5f));
            g2.setColor(new Color(1f, 1f, 1f, b));
            int sz = (i % 5 == 0) ? 2 : 1;
            g2.fillOval(starX[i], starY[i], sz, sz);
        }

        // ── 3. PLACEHOLDER KARAKTER (kanan tengah) ───────────────────────────
        // Ganti dengan: g2.drawImage(characterImage, cx + 80, (int)(h*0.25f), 220, 320, this);
        float charFloat = (float) Math.sin(floatPhase) * 4f;
        drawCharacterPlaceholder(g2, w - 230, (int)(h * 0.25f + charFloat));

        // ── 4. KASTIL SILHOUETTE (background kanan) ──────────────────────────
        drawCastleSilhouette(g2, w - 100, (int)(h * 0.05f));

        // ── 5. JUDUL "Survival SYLVAN" ───────────────────────────────────────
        int titleX = 40;
        int titleY = 80;

        // Shadow
        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString("Survival", titleX + 3, titleY + 3);
        g2.setFont(new Font("Monospaced", Font.BOLD, 70));
        g2.drawString("SYLVAN", titleX + 3, titleY + 68 + 3);

        // Glow
        float glow = (float)(0.6 + 0.4 * Math.sin(glowPhase));
        g2.setColor(new Color(160, 220, 80, (int)(glow * 60)));
        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        g2.drawString("Survival", titleX - 1, titleY - 1);
        g2.setFont(new Font("Monospaced", Font.BOLD, 70));
        g2.drawString("SYLVAN", titleX - 1, titleY + 68 - 1);

        // Teks utama
        g2.setColor(new Color(200, 240, 100));
        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        g2.drawString("Survival", titleX, titleY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 70));
        g2.drawString("SYLVAN", titleX, titleY + 68);

        // ── 6. TAGLINE ───────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
        g2.setColor(new Color(200, 200, 180, 200));
        String line1 = "\"Masuki liar dan misteriusnya Sylvan, tempat";
        String line2 = "insting survivalmu diuji di setiap langkah.\"";
        g2.drawString(line1, titleX, titleY + 148);
        g2.drawString(line2, titleX, titleY + 164);

        // ── 7. MUSUH KECIL (placeholder zombie/skeleton kiri bawah) ─────────
        drawEnemyPlaceholder(g2, (int)(w * 0.38f), (int)(h * 0.72f));

        // ── 8. FOOTER ────────────────────────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(100, 100, 80, 150));
        g2.drawString("© Anara Game Studio  |  Survival Sylvan", w / 2 - 130, h - 10);

        g2.dispose();
    }

    // ── Helper shapes ─────────────────────────────────────────────────────────

    private void drawHillSilhouette(Graphics2D g2, int w, int h) {
        // Bukit hijau gelap
        Polygon hill = new Polygon();
        hill.addPoint(0, h);
        hill.addPoint(0, (int)(h * 0.58f));
        hill.addPoint((int)(w * 0.15f), (int)(h * 0.42f));
        hill.addPoint((int)(w * 0.35f), (int)(h * 0.55f));
        hill.addPoint((int)(w * 0.5f), (int)(h * 0.48f));
        hill.addPoint((int)(w * 0.65f), (int)(h * 0.58f));
        hill.addPoint(w, (int)(h * 0.52f));
        hill.addPoint(w, h);
        g2.setColor(new Color(10, 35, 10, 220));
        g2.fillPolygon(hill);

        // Tepi lebih terang
        g2.setColor(new Color(30, 80, 30, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolyline(hill.xpoints, hill.ypoints, hill.npoints - 2);
    }

    /**
     * Placeholder karakter utama.
     * Ganti dengan g2.drawImage(characterImage, x, y, w, h, this) saat asset siap.
     */
    private void drawCharacterPlaceholder(Graphics2D g2, int x, int y) {
        // Label wireframe
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(x, y, 160, 260, 12, 12);
        g2.setColor(new Color(160, 220, 80, 120));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[]{4, 4}, 0));
        g2.drawRoundRect(x, y, 160, 260, 12, 12);

        // Ikon orang
        g2.setColor(new Color(160, 220, 80, 80));
        g2.fillOval(x + 55, y + 20, 50, 50);          // kepala
        g2.fillRoundRect(x + 50, y + 72, 60, 90, 8, 8); // badan
        g2.fillRoundRect(x + 30, y + 80, 22, 70, 6, 6); // lengan kiri
        g2.fillRoundRect(x + 108, y + 80, 22, 70, 6, 6);// lengan kanan
        g2.fillRoundRect(x + 50, y + 162, 24, 75, 6, 6);// kaki kiri
        g2.fillRoundRect(x + 86, y + 162, 24, 75, 6, 6);// kaki kanan

        // Tongkat/staff
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(100, 160, 200, 150));
        g2.drawLine(x + 130, y + 50, x + 130, y + 240);
        g2.fillOval(x + 122, y + 40, 16, 16);

        // Teks label
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(160, 220, 80, 160));
        g2.drawString("[CHARACTER SPRITE]", x - 10, y + 280);
    }

    /**
     * Placeholder musuh kecil (zombie/skeleton).
     */
    private void drawEnemyPlaceholder(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(x, y, 70, 80, 8, 8);
        g2.setColor(new Color(200, 100, 80, 100));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[]{3, 3}, 0));
        g2.drawRoundRect(x, y, 70, 80, 8, 8);

        g2.setColor(new Color(200, 100, 80, 80));
        g2.fillOval(x + 20, y + 8, 30, 30);
        g2.fillRoundRect(x + 22, y + 38, 26, 35, 4, 4);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.setColor(new Color(200, 100, 80, 150));
        g2.drawString("[ENEMY]", x + 5, y + 95);
    }

    /**
     * Silhouette kastil sederhana di background kanan.
     */
    private void drawCastleSilhouette(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(50, 30, 70, 160));

        // Menara utama
        g2.fillRect(x - 20, y + 40, 40, 100);
        // Puncak menara
        int[] xp = {x - 22, x, x + 22};
        int[] yp = {y + 42, y + 10, y + 42};
        g2.fillPolygon(xp, yp, 3);

        // Menara kecil kiri
        g2.fillRect(x - 55, y + 60, 25, 80);
        int[] xp2 = {x - 57, x - 42, x - 28};
        int[] yp2 = {y + 62, y + 35, y + 62};
        g2.fillPolygon(xp2, yp2, 3);

        // Tembok
        g2.fillRect(x - 80, y + 100, 80, 40);

        // Jendela
        g2.setColor(new Color(255, 220, 80, 60));
        g2.fillRect(x - 8, y + 60, 16, 22);
        g2.fillRect(x - 47, y + 78, 10, 14);
    }
}