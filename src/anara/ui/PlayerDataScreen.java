package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.model.ShopItem;
import anara.utils.AssetManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class PlayerDataScreen extends BasePanel {

    private static final long serialVersionUID = 1L;
    private int hoveredBtn = -1;
    private Timer animTimer;
    private float animPhase = 0f;
    private int spriteFrame = 0;
    private int spriteTick = 0;

    public PlayerDataScreen() {
        setupMouseListeners();
        animTimer = new Timer(30, e -> {
            animPhase += 0.05f;
            spriteTick++;
            if (spriteTick % 20 == 0) spriteFrame = (spriteFrame + 1) % 2;
            repaint();
        });
        animTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (animTimer != null) animTimer.stop();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredBtn;
                hoveredBtn = -1;
                if (getBackRect().contains(e.getPoint()))   hoveredBtn = 0;
                if (getLogoutRect().contains(e.getPoint())) hoveredBtn = 1;
                if (prev != hoveredBtn) repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getBackRect().contains(e.getPoint())) {
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
                } else if (getLogoutRect().contains(e.getPoint())) {
                    int confirm = JOptionPane.showConfirmDialog(null,
                        "Keluar dari akun?", "Konfirmasi",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        GameEngine.getInstance().setCurrentPlayer(null);
                        GameEngine.getInstance().showScreen(GameEngine.SCREEN_LOGIN);
                    }
                }
            }
        });
    }

    private Rectangle getBackRect()   { return new Rectangle(30, 570, 130, 38); }
    private Rectangle getLogoutRect() { return new Rectangle(740, 570, 130, 38); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int w = getWidth(), h = getHeight(), cx = w / 2;

        // ── BACKGROUND: gambar pixel art ────────────────────────────
        if (AssetManager.dataplayer != null) {
            g2.drawImage(AssetManager.dataplayer, 0, 0, w, h, null);
        } else {
            // Fallback gradient jika gambar belum dimuat
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x2a0d4e),
                                                  0, h, new Color(0x0d0818));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }

        // Overlay gelap semi-transparan agar teks & panel tetap terbaca
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, w, h);

        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        if (pd == null) {
            g2.setColor(new Color(200, 190, 160, 180));
            g2.setFont(new Font("Serif", Font.BOLD, 16));
            g2.drawString("Tidak ada data pemain.", cx - 80, h / 2);
            g2.dispose();
            return;
        }

        // ── TITLE BADGE (kiri atas) ─────────────────────────────────
        g2.setColor(new Color(0x32, 0x41, 0x00, 230));
        g2.fillRoundRect(18, 14, 210, 42, 8, 8);
        g2.setColor(new Color(0x8a, 0xaa, 0x00));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(18, 14, 210, 42, 8, 8);
        g2.setFont(new Font("Serif", Font.BOLD, 20));
        g2.setColor(new Color(0xd4, 0xf5, 0x42));
        g2.drawString("Data Pemain", 36, 43);

        // Logo SYLVAN dihapus

        // ── PANEL KIRI: PLAYER CARD ─────────────────────────────────
        int cardX = 18, cardY = 68, cardW = 330, cardH = 488;
        drawPanel(g2, cardX, cardY, cardW, cardH);

        // Badge "PLAYER" — center tepat, font lebih besar
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fmBadge = g2.getFontMetrics();
        int badgeW = fmBadge.stringWidth("PLAYER") + 28;
        int badgeX = cardX + cardW / 2 - badgeW / 2;
        g2.setColor(new Color(55, 75, 160, 180));
        g2.fillRoundRect(badgeX, cardY + 14, badgeW, 28, 6, 6);
        g2.setColor(new Color(180, 200, 255));
        g2.drawString("PLAYER", badgeX + 14, cardY + 33);

        // Sprite player
        int spriteSize = 150;
        int spriteX = cardX + cardW / 2 - spriteSize / 2;
        int spriteY = cardY + 48;

        // Background kotak sprite
        g2.setColor(new Color(60, 90, 140, 55));
        g2.fillRoundRect(spriteX - 10, spriteY - 8, spriteSize + 20, spriteSize + 20, 10, 10);
        g2.setColor(new Color(100, 140, 210, 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(spriteX - 10, spriteY - 8, spriteSize + 20, spriteSize + 20, 10, 10);

        // Gambar sprite
        java.awt.image.BufferedImage playerSprite = (spriteFrame == 0)
            ? AssetManager.playerBasic
            : AssetManager.playerLari;
        if (playerSprite != null) {
            g2.drawImage(playerSprite, spriteX, spriteY, spriteSize, spriteSize, null);
        } else {
            MainMenuScreen.drawTopDownWarrior(g2, cardX + cardW / 2, spriteY + spriteSize / 2, animPhase);
        }

        // Nama player
        g2.setFont(new Font("Serif", Font.BOLD, 24));
        g2.setColor(COL_GOLD);
        String name = pd.getName();
        FontMetrics fmName = g2.getFontMetrics();
        g2.drawString(name, cardX + cardW / 2 - fmName.stringWidth(name) / 2,
                      spriteY + spriteSize + 38);

        // Garis pemisah
        int divY = spriteY + spriteSize + 50;
        g2.setColor(new Color(180, 140, 40, 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(cardX + 16, divY, cardX + cardW - 16, divY);

        // Stat rows
        int statY = divY + 28;
        drawStatRow(g2, cardX + 16, statY,       "Koin:",         String.valueOf(pd.getGold()),           COL_GOLD);
        drawStatRow(g2, cardX + 16, statY + 32,  "Senjata equip:", String.valueOf(
            pd.getEquippedItems().stream()
              .filter(i -> i.getType() == ShopItem.ItemType.WEAPON).count()),                               COL_RED_LIGHT);
        drawStatRow(g2, cardX + 16, statY + 64,  "ATK Bonus:",    "+" + pd.getTotalAttackBonus(),          new Color(230, 128, 64));
        drawStatRow(g2, cardX + 16, statY + 96,  "SKL Bonus:",    "+" + pd.getTotalSkillBonus(),           new Color(104, 216, 136));

        // ── PANEL KANAN: INVENTORI ──────────────────────────────────
        int invX = 362, invY = 68, invW = w - invX - 18, invH = 488;
        drawPanel(g2, invX, invY, invW, invH);

        g2.setFont(new Font("Serif", Font.BOLD, 15));
        g2.setColor(COL_GOLD);
        g2.drawString("ITEM DIMILIKI (" + pd.getInventory().size() + ")",
                      invX + 18, invY + 30);
        g2.setColor(new Color(180, 140, 40, 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(invX + 18, invY + 38, invX + invW - 18, invY + 38);

        if (pd.getInventory().isEmpty()) {
            g2.setFont(new Font("Serif", Font.ITALIC, 14));
            g2.setColor(COL_TEXT_DIM);
            FontMetrics fmE = g2.getFontMetrics();
            String emptyMsg = "Belum ada item. Kunjungi Toko!";
            g2.drawString(emptyMsg,
                          invX + invW / 2 - fmE.stringWidth(emptyMsg) / 2,
                          invY + invH / 2);
        } else {
            // Grid 3 kolom, max 9 item
            int cols     = 3;
            int itemW    = (invW - 36 - (cols - 1) * 10) / cols;
            int itemH    = 120;
            int startX   = invX + 18;
            int startY   = invY + 50;

            for (int i = 0; i < pd.getInventory().size() && i < 9; i++) {
                ShopItem item = pd.getInventory().get(i);
                int col  = i % cols;
                int row  = i / cols;
                int ix   = startX + col * (itemW + 10);
                int iy   = startY + row * (itemH + 10);
                drawItemCard(g2, ix, iy, itemW, itemH, item);
            }
        }

       // ── TOMBOL BAWAH ────────────────────────────────────────────
        // KEMBALI
        g2.setColor(new Color(0x59, 0x69, 0x00));
        g2.fillRoundRect(30, 572, 130, 38, 8, 8);
        g2.setColor(hoveredBtn == 0
            ? new Color(0x59, 0x69, 0x00).brighter()
            : new Color(0x59, 0x69, 0x00));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(30, 572, 130, 38, 8, 8);
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(new Color(0xd4, 0xf5, 0x42));
        g2.drawString("◄ KEMBALI", 46, 597);
 
        // KELUAR AKUN
        g2.setColor(new Color(75, 15, 15, 230));
        g2.fillRoundRect(740, 572, 130, 38, 8, 8);
        g2.setColor(hoveredBtn == 1
            ? new Color(220, 80, 80)
            : new Color(170, 50, 50));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(740, 572, 130, 38, 8, 8);
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(new Color(255, 180, 180));
        g2.drawString("KELUAR AKUN", 754, 597);
 
        g2.dispose();
    }
 
    // ── Helper: gambar panel semi-transparan ─────────────────────────
    @Override
    protected void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0xFD, 0xEB, 0xD4, 210));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
        g2.setColor(new Color(0xFD, 0xEB, 0xD4));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
    }
 
    // ── Helper: baris statistik ──────────────────────────────────────
    private void drawStatRow(Graphics2D g2, int x, int y,
                              String label, String value, Color valueColor) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(80, 50, 20));
        g2.drawString(label, x, y);
 
        g2.setFont(new Font("Serif", Font.BOLD, 15));
        g2.setColor(valueColor);
        g2.drawString(value, x + 160, y);
    }
 
    // ── Helper: kartu item inventori ─────────────────────────────────
    private void drawItemCard(Graphics2D g2, int x, int y, int w, int h, ShopItem item) {
        // Background kartu
        g2.setColor(new Color(0xFD, 0xEB, 0xD4, 200));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 8, 8));
        g2.setColor(new Color(0xFD, 0xEB, 0xD4));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 8, 8));
 
        // Aksen warna tipe (strip atas)
        Color tc = getTypeColor(item.getType());
        g2.setColor(tc);
        g2.fillRoundRect(x, y, w, 5, 4, 4);
 
        // Nama item
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(item.isEquipped() ? new Color(0x59, 0x69, 0x00) : new Color(60, 35, 10));
        drawStringWrapped(g2, item.getName(), x + 9, y + 25, w - 14);
 
        // Tipe
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(120, 80, 40, 200));
        g2.drawString(item.getType().name(), x + 9, y + 43);
 
        // Bonus
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(new Color(0x59, 0x69, 0x00));
        g2.drawString("+" + item.getStatBonus() + " bonus", x + 9, y + 62);
 
        // Label dipakai
        if (item.isEquipped()) {
            g2.setColor(new Color(80, 200, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("● DIPAKAI", x + 9, y + 82);
        }
    }
 
    // ── Helper: teks dengan ellipsis jika terlalu panjang ───────────
    private void drawStringWrapped(Graphics2D g2, String text, int x, int y, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) {
            g2.drawString(text, x, y);
            return;
        }
        while (text.length() > 0 && fm.stringWidth(text + "...") > maxW)
            text = text.substring(0, text.length() - 1);
        g2.drawString(text + "...", x, y);
    }
 
    // ── Helper: warna berdasarkan tipe item ──────────────────────────
    private Color getTypeColor(ShopItem.ItemType type) {
        switch (type) {
            case WEAPON: return new Color(176, 60, 60);
            case SKILL:  return new Color(60, 80, 180);
            default:     return COL_BORDER;
        }
    }
}