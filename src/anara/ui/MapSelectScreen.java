package anara.ui;

import anara.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.GeneralPath;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;

public class MapSelectScreen extends BasePanel {
    private int hoveredMap = -1;
    private int currentIndex = 0;
    private double animatedScroll = 0.0;
    private javax.swing.Timer animationTimer;

    // Partikel ambient per kartu (posisi acak tapi konsisten)
    private final float[][] particleX = new float[4][8];
    private final float[][] particleY = new float[4][8];
    private float particleTick = 0f;
    private javax.swing.Timer particleTimer;

    // =============================================
    // ASSETS
    // =============================================
    private BufferedImage backgroundImage;
    private BufferedImage[] mapImages = new BufferedImage[4];

    private final Rectangle leftArrowRect  = new Rectangle(30, 290, 45, 55);
    private final Rectangle rightArrowRect = new Rectangle(825, 290, 45, 55);

    private static final String[] MAP_NAMES = {
        "DESA BARAT",
        "HUTAN RAWA",
        "BENTENG KABUT",
        "ISTANA ARUNA"
    };
    private static final String[] MAP_SUBTITLES = {
        "MAP  I", "MAP  II", "MAP  III", "MAP  IV"
    };
    private static final String[] MAP_DESC = {
        "Kalahkan 5 prajurit monster\n& Mini Boss Final Boss.",
        "Bertahan 15 detik dari\nserangan massal musuh.",
        "Hadapi 2 Mini Boss\nsecara bersamaan.",
        "FINAL BATTLE — Hadapi\nFinal Boss sejati!"
    };

    // Label kesulitan tematis
    private static final String[] DIFF_LABEL = {
        "MUDAH", "SEDANG", "SUSAH", "SANGAT SUSAH"
    };

    // ── Palet warna lengkap per map ──────────────────────────
    //   [0] bg gelap,  [1] bg tengah,  [2] bg terang,
    //   [3] aksen border, [4] badge, [5] partikel, [6] shimmer
    private static final Color[][] PALETTE = {
        // MAP I — Hutan Mistis: hijau lumut + emas antik
        {
            new Color(12, 32, 16),   // bg gelap
            new Color(24, 58, 28),   // bg tengah
            new Color(38, 82, 40),   // bg terang
            new Color(80, 200, 100), // border hijau cerah
            new Color(50, 160, 70),  // badge hijau
            new Color(100, 230, 130),// partikel (cahaya hutan)
            new Color(180, 255, 160) // shimmer
        },
        // MAP II — Padang Pasir: amber + oranye membara
        {
            new Color(45, 22, 5),
            new Color(85, 42, 10),
            new Color(120, 65, 18),
            new Color(240, 160, 40),
            new Color(200, 120, 20),
            new Color(255, 180, 60),
            new Color(255, 230, 140)
        },
        // MAP III — Gunung Api: merah darah + oranye lava
        {
            new Color(50, 8, 8),
            new Color(90, 15, 12),
            new Color(130, 25, 18),
            new Color(230, 70, 50),
            new Color(190, 40, 30),
            new Color(255, 100, 60),
            new Color(255, 180, 120)
        },
        // MAP IV — Kegelapan Abadi: ungu void + biru gelap
        {
            new Color(10, 5, 25),
            new Color(22, 10, 50),
            new Color(38, 18, 80),
            new Color(160, 80, 255),
            new Color(120, 50, 210),
            new Color(190, 110, 255),
            new Color(230, 180, 255)
        }
    };

    public MapSelectScreen() {
        loadAssets();
        initParticles();
        setupMouseListeners();
        setupAnimationTimer();
        setupParticleTimer();
    }

    // ── Inisialisasi posisi partikel acak ──────────────────
    private void initParticles() {
        Random rng = new Random(42);
        for (int i = 0; i < 4; i++)
            for (int p = 0; p < 8; p++) {
                particleX[i][p] = rng.nextFloat();
                particleY[i][p] = rng.nextFloat();
            }
    }

    private void setupParticleTimer() {
        particleTimer = new javax.swing.Timer(50, e -> {
            particleTick += 0.04f;
            if (particleTick > 1000f) particleTick = 0f;
            repaint();
        });
        particleTimer.start();
    }

    // =============================================
    // ASSETS
    // =============================================
    private void loadAssets() {
        backgroundImage = loadImage("/assets/images/map.png");
        mapImages[0]    = loadImage("/assets/images/map1.png");
        mapImages[1]    = loadImage("/assets/images/map2new.png");
        mapImages[2]    = loadImage("/assets/images/map3.png");
        mapImages[3]    = loadImage("/assets/images/map4new.png");
    }
    private BufferedImage loadImage(String path) {
        try {
            var s = getClass().getResourceAsStream(path);
            if (s == null) { System.err.println("Tidak ditemukan: " + path); return null; }
            return ImageIO.read(s);
        } catch (IOException e) { return null; }
    }

    // =============================================
    // ANIMATION
    // =============================================
    private void setupAnimationTimer() {
        animationTimer = new javax.swing.Timer(16, e -> {
            double diff = currentIndex - animatedScroll;
            if (Math.abs(diff) > 0.001) { animatedScroll += diff * 0.15; repaint(); }
            else { animatedScroll = currentIndex; animationTimer.stop(); repaint(); }
        });
    }

    // =============================================
    // MOUSE
    // =============================================
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int prev = hoveredMap; hoveredMap = -1;
                for (int i = 0; i < 4; i++)
                    if (getMapRect(i).contains(e.getPoint())) { hoveredMap = i; break; }
                if (prev != hoveredMap) repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (currentIndex > 0 && leftArrowRect.contains(e.getPoint())) {
                    currentIndex--; if (!animationTimer.isRunning()) animationTimer.start(); return;
                }
                if (currentIndex < 3 && rightArrowRect.contains(e.getPoint())) {
                    currentIndex++; if (!animationTimer.isRunning()) animationTimer.start(); return;
                }
                for (int i = 0; i < 4; i++)
                    if (getMapRect(i).contains(e.getPoint())) { GameEngine.getInstance().showBattle(i+1); return; }
                if (getBackRect().contains(e.getPoint()))
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
            }
        });
    }

    // =============================================
    // LAYOUT
    // =============================================
    private Rectangle getMapRect(int i) {
        int baseW = 260, baseH = 400, gap = 25;
        int centerX = 900/2 - baseW/2, centerY = 130;
        double offset = i - animatedScroll;
        int slotX = (int)(centerX + offset*(baseW+gap));
        double scale = 1.0 - Math.min(1.0, Math.abs(offset))*0.08;
        int cardW=(int)(baseW*scale), cardH=(int)(baseH*scale);
        return new Rectangle(slotX+(baseW-cardW)/2, centerY+(baseH-cardH)/2, cardW, cardH);
    }
    private Rectangle getBackRect() { return new Rectangle(30, 570, 120, 38); }

    // =============================================
    // PAINT
    // =============================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight(), cx = w/2;
        drawBackground(g2, w, h);
        drawTitle(g2, "PILIH MEDAN PERANG", cx, 78, 36);

        g2.setFont(new Font("Serif", Font.ITALIC, 12));
        g2.setColor(COL_TEXT_DIM);
        String sub = "Setiap medan memiliki tantangan berbeda";
        g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub)/2, 100);

        // Render kartu — aktif paling atas
for (int i = 0; i < 4; i++) if (i != currentIndex) drawMapCard(g2, i);
drawMapCard(g2, currentIndex);

// Panah navigasi & dots
drawArrows(g2);
drawDots(g2, cx);

// Tombol KEMBALI - hijau
g2.setColor(new Color(0x596900));
g2.fillRoundRect(30, 570, 120, 38, 10, 10);
g2.setColor(new Color(50, 200, 80));
g2.setStroke(new BasicStroke(2f));
g2.drawRoundRect(30, 570, 120, 38, 10, 10);
g2.setFont(new Font("Serif", Font.BOLD, 14));
g2.setColor(new Color(200, 255, 210));
g2.drawString("◄ KEMBALI", 42, 594);

g2.dispose();
    }

    @Override
    protected void drawBackground(Graphics2D g2, int w, int h) {
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, w, h, null);
            g2.setColor(new Color(0,0,0,150));
            g2.fillRect(0,0,w,h);
        } else {
            g2.setColor(new Color(10,8,18));
            g2.fillRect(0,0,w,h);
        }
    }

    // =============================================
    // KARTU — kaya visual, berbasis proporsi
    // =============================================
    private void drawMapCard(Graphics2D g2, int i) {
        Rectangle r      = getMapRect(i);
        boolean hov      = (hoveredMap == i);
        boolean isActive = (i == currentIndex);
        Color[] pal      = PALETTE[i];

        int PAD   = 14;
        int imgH  = (int)(r.height * 0.40);
        int footH = 62;
        int bodyY = r.y + imgH;
        int bodyH = r.height - imgH - footH;

        // ── 1. LATAR KARTU — gradient 3-titik bertema ────────
        // Bawah gelap → tengah → atas sedikit lebih terang
        Color c0 = pal[0], c1 = pal[1], c2 = pal[2];
        // Simulasi 3-stop gradient lewat 2 pass
        GradientPaint gp1 = new GradientPaint(r.x, r.y+r.height, c0, r.x, r.y+r.height/2, c1);
        GradientPaint gp2 = new GradientPaint(r.x, r.y+r.height/2, c1, r.x, r.y, c2);
        Shape cardShape = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 16, 16);
        g2.setPaint(gp1); g2.fill(cardShape);
        g2.setPaint(gp2); g2.fill(cardShape);

        // Noise tekstur diagonal halus
        drawNoiseTexture(g2, r, pal[2], i);

        // ── 2. THUMBNAIL GAMBAR ───────────────────────────────
        Shape savedClip = g2.getClip();
        g2.setClip(new RoundRectangle2D.Float(r.x, r.y, r.width, imgH+16, 16, 16));
        if (mapImages[i] != null) {
            g2.drawImage(mapImages[i], r.x, r.y, r.width, imgH, null);
        } else {
            drawPlaceholderThumb(g2, r, imgH, i, pal);
        }
        // Gradient fade bawah gambar
        GradientPaint fade = new GradientPaint(r.x, r.y+imgH*0.45f, new Color(0,0,0,0),
                                               r.x, r.y+imgH, c1);
        g2.setPaint(fade); g2.fillRect(r.x, r.y, r.width, imgH);
        g2.setClip(savedClip);

        // ── 3. ELEMEN DEKORASI TEMATIS ────────────────────────
        drawThemeDecorations(g2, r, i, pal, imgH, bodyY, bodyH, PAD, isActive);

        // ── 4. PARTIKEL AMBIENT ───────────────────────────────
        if (isActive || hov) drawParticles(g2, r, i, pal[5]);

        // ── 5. HOVER HIGHLIGHT ────────────────────────────────
        if (hov) {
            g2.setColor(new Color(255,255,255,14));
            g2.fill(cardShape);
        }

        // ── 6. BADGE NOMOR ────────────────────────────────────
        int bSz = Math.max(28, Math.min(36, (int)(r.width*0.135)));
        int bx = r.x + PAD, by = r.y + PAD;
        // Cincin luar badge
        g2.setColor(new Color(pal[4].getRed(), pal[4].getGreen(), pal[4].getBlue(), 70));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(bx-2, by-2, bSz+4, bSz+4);
        // Lingkaran badge solid
        GradientPaint badgeGrad = new GradientPaint(bx, by, pal[4].brighter(), bx, by+bSz, pal[4].darker());
        g2.setPaint(badgeGrad); g2.fillOval(bx, by, bSz, bSz);
        // Angka
        int nFs = Math.max(11, (int)(bSz*0.55));
        g2.setFont(new Font("Serif", Font.BOLD, nFs));
        g2.setColor(Color.WHITE);
        FontMetrics fmN = g2.getFontMetrics();
        String numStr = String.valueOf(i+1);
        g2.drawString(numStr, bx+(bSz-fmN.stringWidth(numStr))/2, by+(bSz+fmN.getAscent()-fmN.getDescent())/2);
//
//        // ── 7. LABEL TERBUKA ──────────────────────────────────
//        int stFs = Math.max(8, (int)(r.width*0.040));
//        g2.setFont(new Font("SansSerif", Font.BOLD, stFs));
//        // Latar label kecil
//        String stTxt = "✓ TERBUKA";
//        FontMetrics fmSt = g2.getFontMetrics();
//        int stW = fmSt.stringWidth(stTxt);
//        int stX = r.x + r.width - stW - PAD - 4;
//        int stY = r.y + PAD;
//        g2.setColor(new Color(0,0,0,70));
//        g2.fillRoundRect(stX-4, stY-1, stW+8, fmSt.getHeight()+2, 6, 6);
//        g2.setColor(new Color(100,255,130));
//        g2.drawString(stTxt, stX, stY + fmSt.getAscent());

        // ── 8. BODY: SUBTITLE + NAMA MAP ─────────────────────
        int curY = bodyY + 10;

        // Subtitle (MAP I dst)
        int subFs = Math.max(8, (int)(r.width*0.038));
        g2.setFont(new Font("SansSerif", Font.BOLD, subFs));
        g2.setColor(pal[3]);
        g2.drawString(MAP_SUBTITLES[i], r.x+PAD, curY+g2.getFontMetrics().getAscent());
        curY += g2.getFontMetrics().getHeight()+1;

        // Nama map (bold, putih terang)
        int nameFs = Math.max(10, (int)(r.width*0.050));
        g2.setFont(new Font("Serif", Font.BOLD, nameFs));
        g2.setColor(Color.WHITE);
        // Drop shadow nama
        FontMetrics fmName = g2.getFontMetrics();
        String name = truncate(MAP_NAMES[i], fmName, r.width - PAD*2);
        g2.setColor(new Color(0,0,0,120));
        g2.drawString(name, r.x+PAD+1, curY+fmName.getAscent()+1);
        g2.setColor(Color.WHITE);
        g2.drawString(name, r.x+PAD, curY+fmName.getAscent());
        curY += fmName.getHeight() + 3;

        // Garis pemisah — warna aksen
        g2.setStroke(new BasicStroke(1f));
        GradientPaint divider = new GradientPaint(
            r.x+PAD, curY, pal[3],
            r.x+r.width-PAD, curY, new Color(pal[3].getRed(),pal[3].getGreen(),pal[3].getBlue(),0));
        g2.setPaint(divider);
        g2.drawLine(r.x+PAD, curY, r.x+r.width-PAD, curY);
        curY += 8;

        // Deskripsi
        int descFs = Math.max(9, (int)(r.width*0.042));
        g2.setFont(new Font("SansSerif", Font.PLAIN, descFs));
        g2.setColor(new Color(215,215,205));
        FontMetrics fmDesc = g2.getFontMetrics();
        for (String line : MAP_DESC[i].split("\n")) {
            if (curY + fmDesc.getHeight() < bodyY + bodyH + 2) {
                g2.drawString(line, r.x+PAD, curY+fmDesc.getAscent());
                curY += fmDesc.getHeight()+1;
            }
        }

        // ── 9. FOOTER ─────────────────────────────────────────
        int footY = r.y + r.height - footH;

        // Garis footer gradient
        GradientPaint footLine = new GradientPaint(
            r.x+PAD, footY, new Color(pal[3].getRed(),pal[3].getGreen(),pal[3].getBlue(),80),
            r.x+r.width-PAD, footY, new Color(pal[3].getRed(),pal[3].getGreen(),pal[3].getBlue(),0));
        g2.setPaint(footLine);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(r.x+PAD, footY, r.x+r.width-PAD, footY);

        // Label KESULITAN + label teks tingkat
        int diffFs = Math.max(7, (int)(r.width*0.034));
        g2.setFont(new Font("SansSerif", Font.BOLD, diffFs));
        g2.setColor(new Color(170,170,155));
        g2.drawString("KESULITAN", r.x+PAD, footY+14);
        // Label teks tingkat kesulitan (kanan)
        g2.setColor(pal[3]);
        String dlbl = DIFF_LABEL[i];
        g2.drawString(dlbl, r.x+r.width-PAD-g2.getFontMetrics().stringWidth(dlbl), footY+14);

        // Dots kesulitan — lebih besar, dengan glow
        int dotSz  = Math.max(7, (int)(r.width*0.037));
        int dotGap = dotSz + 5;
        int dY     = footY + 18;
        for (int d = 0; d < 4; d++) {
            if (d < i+1) {
                // Dot aktif: glow + filled
                g2.setColor(new Color(pal[5].getRed(),pal[5].getGreen(),pal[5].getBlue(),50));
                g2.fillOval(r.x+PAD + d*dotGap - 2, dY-2, dotSz+4, dotSz+4);
                GradientPaint dotGrad = new GradientPaint(
                    r.x+PAD+d*dotGap, dY, pal[6],
                    r.x+PAD+d*dotGap, dY+dotSz, pal[5]);
                g2.setPaint(dotGrad);
                g2.fillOval(r.x+PAD+d*dotGap, dY, dotSz, dotSz);
            } else {
                g2.setColor(new Color(255,255,255,30));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(r.x+PAD+d*dotGap, dY, dotSz, dotSz);
            }
        }

        // Teks aksi klik
        int actFs = Math.max(8, (int)(r.width*0.040));
        g2.setFont(new Font("SansSerif", Font.BOLD, actFs));
        FontMetrics fmAct = g2.getFontMetrics();
        String actTxt = hov ? "▶ KLIK UNTUK MEMULAI" : "Klik untuk memilih";
        g2.setColor(hov ? pal[3] : new Color(180,180,175));
        int actW = fmAct.stringWidth(actTxt);
        g2.drawString(actTxt, r.x+(r.width-actW)/2, r.y+r.height-7);

        // ── 10. BORDER UTAMA ──────────────────────────────────
        float bw = isActive ? 2.5f : (hov ? 1.8f : 0.8f);
        g2.setStroke(new BasicStroke(bw));
        g2.setColor(isActive ? pal[3] : (hov ? pal[3].brighter() : new Color(255,255,255,25)));
        g2.draw(cardShape);

        // Inner glow kartu aktif
        if (isActive) {
            g2.setColor(new Color(pal[3].getRed(),pal[3].getGreen(),pal[3].getBlue(),45));
            g2.setStroke(new BasicStroke(7f));
            g2.draw(new RoundRectangle2D.Float(r.x+3,r.y+3,r.width-6,r.height-6,13,13));
            g2.setStroke(new BasicStroke(2f));
        }

        // Shimmer highlight pojok kiri atas
        GradientPaint shimmer = new GradientPaint(
            r.x, r.y, new Color(pal[6].getRed(),pal[6].getGreen(),pal[6].getBlue(),60),
            r.x+r.width*0.5f, r.y+r.height*0.3f, new Color(0,0,0,0));
        g2.setPaint(shimmer); g2.fill(cardShape);
    }

    // ── Dekorasi elemen tematis per map ────────────────────
    private void drawThemeDecorations(Graphics2D g2, Rectangle r, int i,
                                      Color[] pal, int imgH, int bodyY, int bodyH,
                                      int PAD, boolean isActive) {
        g2.setStroke(new BasicStroke(1f));
        int cx = r.x + r.width/2;

        switch(i) {
            case 0: // MAP I: Hutan — sulur tanaman di sudut bawah + bintik cahaya
                drawVines(g2, r, pal[3], pal[5]);
                // Dekorasi rune kecil di bawah deskripsi
                drawRuneRow(g2, r.x+PAD, bodyY+bodyH-14, r.width-PAD*2, pal[3], 5);
                break;

            case 1: // MAP II: Padang Pasir — ornamen pasir + garis diagonal
                drawDesertOrnament(g2, r, pal[3], pal[5], PAD, imgH);
                break;

            case 2: // MAP III: Lava — retakan api + gelombang panas bawah
                drawLavaCracks(g2, r, pal[3], pal[5], imgH);
                break;

            case 3: // MAP IV: Kegelapan — bintang latar + simbol kegelapan
                drawDarknessOrbs(g2, r, pal, isActive, bodyY, bodyH, PAD);
                break;
        }
    }

    // MAP I — Sulur tanaman di sudut kartu
    private void drawVines(Graphics2D g2, Rectangle r, Color vineCol, Color glowCol) {
        g2.setColor(new Color(vineCol.getRed(), vineCol.getGreen(), vineCol.getBlue(), 50));
        g2.setStroke(new BasicStroke(1.2f));
        // Kiri bawah
        GeneralPath v = new GeneralPath();
        v.moveTo(r.x+4, r.y+r.height-20);
        v.curveTo(r.x+10, r.y+r.height-60, r.x+20, r.y+r.height-50, r.x+18, r.y+r.height-85);
        g2.draw(v);
        // Cabang kecil
        GeneralPath v2 = new GeneralPath();
        v2.moveTo(r.x+10, r.y+r.height-55);
        v2.curveTo(r.x+20, r.y+r.height-65, r.x+28, r.y+r.height-58, r.x+32, r.y+r.height-60);
        g2.draw(v2);
        // Kanan bawah
        g2.setColor(new Color(vineCol.getRed(), vineCol.getGreen(), vineCol.getBlue(), 35));
        GeneralPath v3 = new GeneralPath();
        v3.moveTo(r.x+r.width-4, r.y+r.height-18);
        v3.curveTo(r.x+r.width-12, r.y+r.height-58, r.x+r.width-22, r.y+r.height-48, r.x+r.width-20, r.y+r.height-78);
        g2.draw(v3);
    }

    // MAP I — Baris rune dekoratif
    private void drawRuneRow(Graphics2D g2, int x, int y, int w, Color col, int count) {
        String[] runes = {"᛫","ᚠ","ᚢ","ᚦ","ᚨ","ᚱ","ᚲ"};
        g2.setFont(new Font("Serif", Font.PLAIN, 9));
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 55));
        int spacing = w / (count+1);
        FontMetrics fm = g2.getFontMetrics();
        for (int k=0; k<count; k++) {
            String r2 = runes[k % runes.length];
            g2.drawString(r2, x + spacing*(k+1) - fm.stringWidth(r2)/2, y);
        }
    }

    // MAP II — Ornamen pasir: garis-garis diagonal + titik
    private void drawDesertOrnament(Graphics2D g2, Rectangle r, Color col, Color glow, int PAD, int imgH) {
        // Garis diagonal salib di bawah gambar
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 30));
        g2.setStroke(new BasicStroke(0.8f));
        int bodyTop = r.y + imgH + 2;
        int bodyBot = r.y + r.height - 64;
        // Garis-garis diagonal latar
        for (int d = -3; d <= 8; d++) {
            int startX = r.x + PAD + d*20;
            g2.drawLine(startX, bodyTop, startX+20, bodyBot);
        }
        // Titik sudut dekoratif (4 sudut dalam kartu)
        g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 100));
        int off = 6;
        g2.fillOval(r.x+off, r.y+off, 5, 5);
        g2.fillOval(r.x+r.width-off-5, r.y+off, 5, 5);
        g2.fillOval(r.x+off, r.y+r.height-off-5, 5, 5);
        g2.fillOval(r.x+r.width-off-5, r.y+r.height-off-5, 5, 5);
        // Garis horisontal bawah ornamen emas
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(r.x+PAD*2, r.y+r.height-65, r.x+r.width-PAD*2, r.y+r.height-65);
    }

    // MAP III — Retakan lava di bawah gambar
    private void drawLavaCracks(Graphics2D g2, Rectangle r, Color col, Color glow, int imgH) {
        g2.setStroke(new BasicStroke(1.2f));
        // Glow retak
        g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 80));
        int bY = r.y + imgH - 5;
        // Retak kiri
        GeneralPath crack1 = new GeneralPath();
        crack1.moveTo(r.x+30, bY);
        crack1.lineTo(r.x+20, bY+12); crack1.lineTo(r.x+28, bY+18);
        crack1.lineTo(r.x+16, bY+30);
        g2.draw(crack1);
        // Retak tengah
        GeneralPath crack2 = new GeneralPath();
        crack2.moveTo(r.x+r.width/2, bY);
        crack2.lineTo(r.x+r.width/2-8, bY+8); crack2.lineTo(r.x+r.width/2+4, bY+16);
        crack2.lineTo(r.x+r.width/2-4, bY+26);
        g2.draw(crack2);
        // Retak kanan
        GeneralPath crack3 = new GeneralPath();
        crack3.moveTo(r.x+r.width-40, bY);
        crack3.lineTo(r.x+r.width-28, bY+14); crack3.lineTo(r.x+r.width-36, bY+22);
        crack3.lineTo(r.x+r.width-24, bY+32);
        g2.draw(crack3);
        // Glow inti retak
        g2.setColor(new Color(255,80,0,60));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(crack2);
    }

    // MAP IV — Orb-orb kegelapan + bintang mikro
    private void drawDarknessOrbs(Graphics2D g2, Rectangle r, Color[] pal,
                                   boolean active, int bodyY, int bodyH, int PAD) {
        // Mini-star scattered di body
        g2.setColor(new Color(pal[6].getRed(), pal[6].getGreen(), pal[6].getBlue(), 55));
        int[][] starPos = {{20,15},{80,30},{50,55},{r.width-25,20},{r.width-40,45},{30,70}};
        for (int[] sp : starPos) {
            int sx = r.x + sp[0], sy = bodyY + sp[1];
            if (sy < bodyY + bodyH) {
                g2.fillOval(sx, sy, 2, 2);
                g2.fillOval(sx-4, sy+1, 1, 1);
                g2.fillOval(sx+4, sy-1, 1, 1);
            }
        }
        // Orb ungu di pojok kanan bawah body
        int ox = r.x + r.width - PAD - 22, oy = bodyY + bodyH - 26;
        if (oy > bodyY+10) {
            g2.setColor(new Color(pal[5].getRed(), pal[5].getGreen(), pal[5].getBlue(), active?80:45));
            g2.fillOval(ox, oy, 18, 18);
            g2.setColor(new Color(pal[6].getRed(), pal[6].getGreen(), pal[6].getBlue(), 100));
            g2.fillOval(ox+4, oy+4, 8, 8);
        }
        // Simbol kegelapan (lingkaran ganda) di kiri bawah body
        int sx2 = r.x+PAD+2, sy2 = bodyY+bodyH-20;
        if (sy2 > bodyY+10) {
            g2.setColor(new Color(pal[3].getRed(), pal[3].getGreen(), pal[3].getBlue(), 50));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(sx2, sy2, 12, 12);
            g2.drawOval(sx2+3, sy2+3, 6, 6);
        }
    }

    // ── Partikel melayang tematis ───────────────────────────
    private void drawParticles(Graphics2D g2, Rectangle r, int mapIdx, Color pCol) {
        Composite old = g2.getComposite();
        for (int p = 0; p < 8; p++) {
            float baseX = particleX[mapIdx][p];
            float baseY = particleY[mapIdx][p];
            // Gerak: melayang ke atas dengan osilasi horizontal
            float phase = (float)(particleTick + p * 0.8f);
            float px = r.x + baseX * r.width + (float)Math.sin(phase*1.2f + p) * 8f;
            float py = r.y + ((baseY + particleTick*0.018f*(1f+p*0.1f)) % 1.0f) * r.height;

            float alpha = (float)(0.25f + 0.2f * Math.sin(phase*2.0f));
            int sz = (p % 3 == 0) ? 4 : (p % 3 == 1) ? 3 : 2;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
            g2.setColor(pCol);
            g2.fillOval((int)px - sz/2, (int)py - sz/2, sz, sz);

            // Cahaya luar partikel (glow)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha*0.3f));
            g2.fillOval((int)px - sz, (int)py - sz, sz*2, sz*2);
        }
        g2.setComposite(old);
    }

    // ── Noise tekstur subtle di latar kartu ────────────────
    private void drawNoiseTexture(Graphics2D g2, Rectangle r, Color col, int seed) {
        Random rng = new Random(seed * 7919L);
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 8));
        for (int n = 0; n < 40; n++) {
            int nx = r.x + rng.nextInt(r.width);
            int ny = r.y + rng.nextInt(r.height);
            int ns = rng.nextInt(3)+1;
            g2.fillOval(nx, ny, ns, ns);
        }
    }

    // ── Placeholder thumbnail tanpa gambar ─────────────────
    private void drawPlaceholderThumb(Graphics2D g2, Rectangle r, int imgH, int i, Color[] pal) {
        GradientPaint ph = new GradientPaint(r.x, r.y, pal[2].brighter(), r.x, r.y+imgH, pal[1]);
        g2.setPaint(ph); g2.fillRect(r.x, r.y, r.width, imgH);
        g2.setColor(new Color(255,255,255,30));
        g2.setFont(new Font("Serif", Font.ITALIC, 11));
        String phTxt = "[ Gambar Map "+(i+1)+" ]";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(phTxt, r.x+(r.width-fm.stringWidth(phTxt))/2, r.y+imgH/2+4);
    }

    // ── Helper: truncate teks ─────────────────────────────
    private String truncate(String s, FontMetrics fm, int maxW) {
        while (fm.stringWidth(s) > maxW && s.length() > 3)
            s = s.substring(0, s.length()-1);
        return fm.stringWidth(MAP_NAMES[0]) <= maxW ? s : s+"..."; // safe fallback
    }

    // =============================================
    // PANAH
    // =============================================
    private void drawArrows(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2f));
        if (currentIndex > 0) {
            g2.setColor(new Color(20,15,10,210)); g2.fillRoundRect(leftArrowRect.x,leftArrowRect.y,leftArrowRect.width,leftArrowRect.height,8,8);
            g2.setColor(COL_GOLD_LIGHT); g2.drawRoundRect(leftArrowRect.x,leftArrowRect.y,leftArrowRect.width,leftArrowRect.height,8,8);
            g2.fillPolygon(new int[]{leftArrowRect.x+28,leftArrowRect.x+14,leftArrowRect.x+28},
                           new int[]{leftArrowRect.y+13,leftArrowRect.y+27,leftArrowRect.y+41},3);
        }
        if (currentIndex < 3) {
            g2.setColor(new Color(20,15,10,210)); g2.fillRoundRect(rightArrowRect.x,rightArrowRect.y,rightArrowRect.width,rightArrowRect.height,8,8);
            g2.setColor(COL_GOLD_LIGHT); g2.drawRoundRect(rightArrowRect.x,rightArrowRect.y,rightArrowRect.width,rightArrowRect.height,8,8);
            g2.fillPolygon(new int[]{rightArrowRect.x+17,rightArrowRect.x+31,rightArrowRect.x+17},
                           new int[]{rightArrowRect.y+13,rightArrowRect.y+27,rightArrowRect.y+41},3);
        }
    }

    // =============================================
    // DOTS
    // =============================================
    private void drawDots(Graphics2D g2, int cx) {
        int dotsX = cx-(4*20)/2;
        for (int d=0; d<4; d++) {
            if (d==currentIndex) { g2.setColor(COL_GOLD_LIGHT); g2.fillOval(dotsX+d*20,553,10,10); }
            else { g2.setColor(new Color(100,90,80,140)); g2.drawOval(dotsX+d*20,553,10,10); }
        }
    }
}