package anara.ui;

import anara.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.net.URL;
import java.util.Random;

/**
 * StartScreen - Layar pembuka game "Survival Sylvan" Menggunakan asset gambar
 * asli sebagai background dan murni menggunakan kustom font kustom dari lokal.
 */
public class StartScreen extends BasePanel {

    // ── Animasi ──────────────────────────────────────────────────────────────
    private Timer animTimer;
    private float glowPhase = 0f;
    private float starPhase = 0f;
    private int[] starX, starY;
    private float[] starBright;

    // ── Background image & Custom Font ───────────────────────────────────────
    private Image bgImage;
    private Font customFont;

    public StartScreen() {
        setLayout(null);
        setOpaque(true);
        initStars();
        loadAssets();
        loadCustomFont(); // Memuat font ari-w9500 dari direktori lokal Anda
        startAnimation();
        setupButton();
    }

    // ── Load asset gambar (dengan debug lengkap) ──────────────────────────────
    private void loadAssets() {
        String fileName = "start.png";

        String[] classpathPaths = {
            "/Assets/images/" + fileName,
            "/assets/images/" + fileName,
            "/Assets/Images/" + fileName,
            "/" + fileName,
            "Assets/images/" + fileName,
            "assets/images/" + fileName,};

        System.out.println("[StartScreen] Mencari: " + fileName);

        for (String path : classpathPaths) {
            URL url = getClass().getResource(path);
            if (url != null) {
                bgImage = new ImageIcon(url).getImage();
                System.out.println("[StartScreen] Berhasil load dari: " + path);
                return;
            }
        }

        String[] fsPaths = {
            "src/Assets/images/" + fileName,
            "src/assets/images/" + fileName,
            "Assets/images/" + fileName,
            fileName,};
        for (String path : fsPaths) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                bgImage = new ImageIcon(f.getAbsolutePath()).getImage();
                System.out.println("[StartScreen] Berhasil load dari filesystem: " + path);
                return;
            }
        }

        System.err.println("[StartScreen] Gambar tidak ditemukan di mana pun!");
    }

    // ── Load Custom Font dari Direktori Spesifik ─────────────────────────────
    private void loadCustomFont() {
        // Path absolut mengarah langsung ke folder font di Windows Anda
        String fontPath = "C:/Users/Iqbal/Project-Game-PBO/src/Assets/font/ari-w9500-condensed-bold.ttf";
        File fontFile = new File(fontPath);

        if (fontFile.exists()) {
            try {
                // Membaca file font .ttf kustom
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                System.out.println("[StartScreen] Font ari-w9500 berhasil dimuat dari: " + fontFile.getAbsolutePath());
                return;
            } catch (Exception e) {
                System.err.println("[StartScreen] Gagal membaca berkas font di " + fontPath + " (" + e.getMessage() + ")");
            }
        } else {
            System.err.println("[StartScreen] Berkas font TIDAK DITEMUKAN di path: " + fontPath);
        }

        // Fallback jika file tidak ditemukan atau error saat dibaca
        System.err.println("[StartScreen] Menggunakan font sistem Monospaced sebagai cadangan.");
        customFont = new Font("Monospaced", Font.BOLD, 12);
    }

    // ── Bintang kecil overlay ────────────────────────────────────────────────
    private void initStars() {
        int n = 40;
        starX = new int[n];
        starY = new int[n];
        starBright = new float[n];
        Random rnd = new Random(42);
        for (int i = 0; i < n; i++) {
            starX[i] = rnd.nextInt(900);
            starY[i] = rnd.nextInt(200);
            starBright[i] = 0.2f + rnd.nextFloat() * 0.5f;
        }
    }

    // ── Tombol "Mulai Petualangan" ───────────────────────────────────────────
    private void setupButton() {
        int cx = 900 / 2; // Titik tengah layar (X = 450)
        JButton btnStart = new JButton("Mulai Petualangan   >>>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hov = getModel().isRollover();
                boolean prs = getModel().isPressed();

                Color bgTop = hov ? new Color(120, 180, 60) : new Color(90, 145, 40);
                Color bgBot = hov ? new Color(70, 120, 25) : new Color(50, 95, 15);
                GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, getHeight(), bgBot);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setColor(new Color(180, 220, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() / 2 - 3, 6, 6);

                int dy = prs ? 2 : 0;

                // Menggunakan font baru Anda dengan ukuran 18f
                g2.setFont(customFont.deriveFont(Font.PLAIN, 18f));

                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + dy;

                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawString(getText(), tx + 1, ty + 1);
                g2.setColor(new Color(240, 255, 200));
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };

        // ── PANDUAN MENGGESER TOMBOL ──────────────────────────────────────────
        // Parameter: setBounds(X, Y, Lebar, Tinggi)
        // Lebar dinaikkan jadi 400 agar teks ukuran 18f muat sempurna.
        // cx - (Lebar/2) -> 450 - 200 = 250 (Membuat tombol center otomatis)
        int tombolX = cx - 390; // geser kanan kiri
        int tombolY = 430;      // <--- UBAH ANGKA INI UNTUK MENGGESER KE ATAS / BAWAH (misal: 410, 440, dll)

        btnStart.setBounds(tombolX, tombolY, 400, 55);
        // ──────────────────────────────────────────────────────────────────────

        btnStart.setOpaque(false);
        btnStart.setContentAreaFilled(false);
        btnStart.setBorderPainted(false);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e
                -> GameEngine.getInstance().showScreen(GameEngine.SCREEN_LOGIN)
        );
        add(btnStart);
    }

    private void startAnimation() {
        animTimer = new Timer(30, e -> {
            glowPhase += 0.04f;
            starPhase += 0.02f;
            if (glowPhase > Math.PI * 2) {
                glowPhase = 0;
            }
            if (starPhase > Math.PI * 2) {
                starPhase = 0;
            }
            repaint();
        });
        animTimer.start();
    }

    public void stopAnimation() {
        if (animTimer != null) {
            animTimer.stop();
        }
    }

    // ── Render utama ─────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();

        // ── 1. BACKGROUND ────────────────────────────────────────────────────
        if (bgImage != null) {
            int imgW = bgImage.getWidth(this);
            int imgH = bgImage.getHeight(this);
            if (imgW > 0 && imgH > 0) {
                double scaleX = (double) w / imgW;
                double scaleY = (double) h / imgH;
                double scale = Math.max(scaleX, scaleY);
                int drawW = (int) (imgW * scale);
                int drawH = (int) (imgH * scale);
                int offX = (w - drawW) / 2;
                int offY = (h - drawH) / 2;
                g2.drawImage(bgImage, offX, offY, drawW, drawH, this);
            } else {
                drawFallbackBg(g2, w, h);
            }

            GradientPaint textAreaOverlay = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 20),
                    w * 0.58f, 0, new Color(0, 0, 0, 20)
            );
            g2.setPaint(textAreaOverlay);
            g2.fillRect(0, 0, w, h);

            GradientPaint bottomOverlay = new GradientPaint(
                    0, h * 0.6f, new Color(0, 0, 0, 0),
                    0, h, new Color(0, 0, 0, 130)
            );
            g2.setPaint(bottomOverlay);
            g2.fillRect(0, (int) (h * 0.6f), w, h);

        } else {
            drawFallbackBg(g2, w, h);
        }

        // ── 2. BINTANG OVERLAY ───────────────────────────────────────────────
        for (int i = 0; i < starX.length; i++) {
            float b = starBright[i] * (0.5f + 0.5f * (float) Math.sin(starPhase + i * 0.7f));
            g2.setColor(new Color(1f, 1f, 1f, b));
            int sz = (i % 7 == 0) ? 2 : 1;
            g2.fillOval(starX[i], starY[i], sz, sz);
        }

        // ── 3. POSISI KATA (DISET SEBAGAI KOORDINAT UTAMA TAGLINE) ───────────
        // Menggunakan font baru Anda dengan ukuran
        g2.setFont(customFont.deriveFont(Font.PLAIN, 18f));

        int taglineX = 65;
        int taglineY1 = 350;
        int taglineY2 = 375;

        // Bayangan Teks (Hitam)
        g2.setColor(new Color(0, 0, 0, 220));
        g2.drawString("\"Masuki liar dan misteriusnya Sylvan, tempat", taglineX + 2, taglineY1 + 2);
        g2.drawString("insting survivalmu diuji di setiap langkah.\"", taglineX + 2, taglineY2 + 2);

        // Teks Utama (Putih Krem Retro)
        g2.setColor(new Color(235, 235, 210, 240));
        g2.drawString("\"Masuki liar dan misteriusnya Sylvan, tempat", taglineX, taglineY1);
        g2.drawString("insting survivalmu diuji di setiap langkah.\"", taglineX, taglineY2);

        // ── 4. FOOTER ────────────────────────────────────────────────────────
        // Menggunakan font baru Anda dengan ukuran
        g2.setFont(customFont.deriveFont(14f));
        g2.setColor(new Color(180, 180, 160, 150));
        String footer = "© Game PBO | Survival Sylvan";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(footer, (w - fm.stringWidth(footer)) / 2, h - 15);

        g2.dispose();
    }

    // ── Fallback background ───────────────────────────────────────────────────
    private void drawFallbackBg(Graphics2D g2, int w, int h) {
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(18, 10, 35),
                0, h / 2, new Color(35, 20, 60),
                true
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        GradientPaint groundGrad = new GradientPaint(
                0, h * 0.55f, new Color(15, 40, 15),
                0, h, new Color(5, 15, 5)
        );
        g2.setPaint(groundGrad);
        g2.fillRect(0, (int) (h * 0.55f), w, h);

        g2.setFont(customFont.deriveFont(14f));
        g2.setColor(new Color(255, 100, 100, 200));
        g2.drawString("[BG IMAGE NOT FOUND - cek Output untuk path debug]", 20, h - 30);
    }
}
