package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.model.ShopItem;
import anara.utils.AssetManager;
import anara.utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ShopScreen extends BasePanel {

    private enum Tab { SHOP, INVENTORY }
    private Tab activeTab = Tab.SHOP;

    // Slide 0 = WEAPON, Slide 1 = SKILL
    private int currentSlide = 0; // 0=weapon, 1=skill
    private static final int SLIDE_WEAPON = 0;
    private static final int SLIDE_SKILL  = 1;

    private int hoveredItem  = -1;
    private int selectedItem = -1; // index di dalam list slide saat ini
    private String statusMsg  = "";
    private int statusTimer   = 0;
    private javax.swing.Timer uiTimer;

    // Catalog lengkap
    private static final List<ShopItem> CATALOG_WEAPON = new ArrayList<>(Arrays.asList(
        new ShopItem("w1","Sylvan Strike",  "Pedang kayu besi milik Kael (+10 ATK)",           80,  10, ShopItem.ItemType.WEAPON),
        new ShopItem("w2","Iron Starter",   "Pedang besi dasar untuk petualang pemula (+15 ATK)",120, 15, ShopItem.ItemType.WEAPON),
        new ShopItem("w3","Void Breaker",   "Logam kuno bereaksi energi kutukan (+20 ATK)",    200, 20, ShopItem.ItemType.WEAPON),
        new ShopItem("w4","Aruna's Wrath",  "Pedang legendaris diberkati cahaya Aruna (+35 ATK)", 350, 35, ShopItem.ItemType.WEAPON)
    ));
    private static final List<ShopItem> CATALOG_SKILL = new ArrayList<>(Arrays.asList(
        new ShopItem("s1","Quickstrike",  "Kurangi cooldown serangan (+5 SKL)",              100,  5, ShopItem.ItemType.SKILL),
        new ShopItem("s2","Windrage",     "Serangan AoE memutar di sekitar Raka (+15 SKL)",  200, 15, ShopItem.ItemType.SKILL),
        new ShopItem("s3","Sealbreaker",  "Melepaskan energi Pecahan Segel (+25 SKL)",       1000, 25, ShopItem.ItemType.SKILL)
    ));

   public ShopScreen() {
    setName(GameEngine.SCREEN_SHOP); // tambahkan baris ini
    setupMouseListeners();
    uiTimer = new javax.swing.Timer(100, e -> {
        if (statusTimer > 0) { statusTimer--; repaint(); }
    });
    uiTimer.start();
}
//    public ShopScreen() {
//        setupMouseListeners();
//        uiTimer = new javax.swing.Timer(100, e -> {
//            if (statusTimer > 0) { statusTimer--; repaint(); }
//        });
//        uiTimer.start();
//    }

    // ===== HELPERS =====
    private BufferedImage getItemImage(String id) {
        switch (id) {
            case "w1": return AssetManager.senjata2;
            case "w2": return AssetManager.senjata3;
            case "w3": return AssetManager.senjata4;
            case "s1": return AssetManager.skil2;
            case "s2": return AssetManager.skil3;
            case "s3": return AssetManager.skil1;
            default:   return AssetManager.senjata1;
        }
    }

    /** List yang tampil sesuai tab & slide */
    private List<ShopItem> getDisplayList() {
        if (activeTab == Tab.SHOP) {
            return currentSlide == SLIDE_WEAPON ? CATALOG_WEAPON : CATALOG_SKILL;
        } else {
            // Inventori: tampilkan semua item sekaligus (1 slide)
            PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
            if (pd == null) return new ArrayList<>();
            return pd.getInventory();
        }
    }

    // ===== LAYOUT (panel kiri item, panel kanan detail) =====
    // Panel kiri: lebar ~60% layar
    private int itemPanelW() { return (int)(getWidth() * 0.60); }
    // Panel kanan: sisanya
    private int detailPanelX() { return itemPanelW() + 18; }
    private int detailPanelW() { return getWidth() - detailPanelX() - 12; }

    private Rectangle getItemRect(int i) {
        int cols  = 2;
        int col   = i % cols, row = i / cols;
        int cardW = (itemPanelW() - 45) / 2; // 2 kolom pas
        int cardH = 100;
        int gapX  = 10, gapY = 10;
        int startX = 18, startY = 162;
        return new Rectangle(startX + col * (cardW + gapX), startY + row * (cardH + gapY), cardW, cardH);
    }

    // Tombol tab
    private Rectangle getShopTabRect() { return new Rectangle(18, 112, 110, 34); }
    private Rectangle getInvTabRect()  { return new Rectangle(135, 112, 140, 34); }

    // Tombol slide (bawah panel item)
    private Rectangle getPrevSlideRect() { return new Rectangle(18,  getHeight() - 108, 52, 34); }
    private Rectangle getNextSlideRect() { return new Rectangle(itemPanelW() - 60, getHeight() - 108, 52, 34); }

    // Tombol kembali
    private Rectangle getBackRect() { return new Rectangle(18, getHeight() - 58, 130, 36); }

    // Tombol beli (di panel detail, bawah)
    private Rectangle getBuyRect() {
        int dx = detailPanelX();
        int dw = detailPanelW();
        return new Rectangle(dx, getHeight() - 80, dw, 38);
    }
    private Rectangle getEquipRect() {
        int dx = detailPanelX();
        int dw = detailPanelW();
        return new Rectangle(dx, getHeight() - 125, dw, 38);
    }
    private Rectangle getDelRect() {
        int dx = detailPanelX();
        int dw = detailPanelW();
        return new Rectangle(dx, getHeight() - 80, dw, 38);
    }

    // ===== MOUSE =====
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int prev = hoveredItem;
                hoveredItem = getItemAt(e.getX(), e.getY());
                if (prev != hoveredItem) repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();

                // Tab
                if (getShopTabRect().contains(p)) {
                    activeTab = Tab.SHOP; selectedItem = -1; repaint(); return;
                }
                if (getInvTabRect().contains(p)) {
                    activeTab = Tab.INVENTORY; selectedItem = -1; repaint(); return;
                }
                // Kembali
                if (getBackRect().contains(p)) {
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU); return;
                }
                // Slide nav
                if (getPrevSlideRect().contains(p)) {
                    currentSlide = SLIDE_WEAPON; selectedItem = -1; repaint(); return;
                }
                if (getNextSlideRect().contains(p)) {
                    currentSlide = SLIDE_SKILL; selectedItem = -1; repaint(); return;
                }
                // Pilih item
                int idx = getItemAt(p.x, p.y);
                if (idx >= 0) { selectedItem = idx; repaint(); }

                // Tombol aksi
                handleActionButtons(p.x, p.y);
            }
        });
    }

    private int getItemAt(int mx, int my) {
        List<ShopItem> list = getDisplayList();
        for (int i = 0; i < list.size(); i++) {
            if (getItemRect(i).contains(mx, my)) return i;
        }
        return -1;
    }

    private void handleActionButtons(int mx, int my) {
        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        if (pd == null) return;
        List<ShopItem> list = getDisplayList();
        if (selectedItem < 0 || selectedItem >= list.size()) return;
        ShopItem item = list.get(selectedItem);

        if (activeTab == Tab.SHOP) {
            if (getBuyRect().contains(mx, my)) {
                boolean owned = pd.getInventory().stream().anyMatch(x -> x.getId().equals(item.getId()));
                if (owned) { setStatus("Sudah dimiliki!"); return; }
                if (pd.getGold() < item.getPrice()) { setStatus("Koin tidak cukup!"); return; }
                pd.setGold(pd.getGold() - item.getPrice());
                pd.addItem(new ShopItem(item.getId(), item.getName(), item.getDescription(),
                        item.getPrice(), item.getStatBonus(), item.getType()));
                SaveManager.savePlayer(pd);
                setStatus("Berhasil membeli " + item.getName() + "!");
            }
        } else {
            if (getEquipRect().contains(mx, my)) {
                if (item.isEquipped()) { pd.unequipItem(item); setStatus("Dilepas: " + item.getName()); }
                else { pd.equipItem(item); setStatus("Dipakai: " + item.getName()); }
                SaveManager.savePlayer(pd);
            } else if (getDelRect().contains(mx, my)) {
                pd.removeItem(item); selectedItem = -1;
                SaveManager.savePlayer(pd); setStatus("Item dihapus.");
            }
        }
        repaint();
    }

    private void setStatus(String msg) { statusMsg = msg; statusTimer = 25; }

    // ===== PAINT =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        if (AssetManager.Shop != null) g2.drawImage(AssetManager.Shop, 0, 0, w, h, null);
        else drawBackground(g2, w, h);

        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();

//        // ===== HEADER =====
//        g2.setColor(new Color(40, 80, 20, 230));
//        g2.fillRoundRect(12, 52, 310, 44, 12, 12);
//        g2.setColor(new Color(220, 180, 40));
//        g2.setStroke(new BasicStroke(3f));
//        g2.drawRoundRect(12, 52, 310, 44, 12, 12);
//        g2.setFont(new Font("Georgia", Font.BOLD, 21));
//        g2.setColor(Color.WHITE);
//        g2.drawString("TOKO & INVENTORI", 24, 81);

        // ===== KOIN & DIAMOND (lebih besar, lebih ke bawah) =====
        if (pd != null) {
            // Koin
            g2.setColor(new Color(255, 200, 0));
            g2.fillOval(w - 255, 28, 34, 34);
            g2.setColor(new Color(200, 150, 0));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(w - 255, 28, 34, 34);
            g2.setFont(new Font("Georgia", Font.BOLD, 20));
            g2.setColor(new Color(255, 235, 100));
            g2.drawString(String.format("%,d", pd.getGold()), w - 215, 51);

            // Diamond
            int cx = w - 130, cy = 45;
            int[] dx = {cx, cx + 14, cx + 28, cx + 14};
            int[] dy = {cy, cy - 14, cy, cy + 14};
            g2.setColor(new Color(100, 200, 255));
            g2.fillPolygon(dx, dy, 4);
            g2.setColor(new Color(60, 150, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawPolygon(dx, dy, 4);
            g2.setFont(new Font("Georgia", Font.BOLD, 20));
            g2.setColor(new Color(185, 235, 255));
            g2.drawString(String.format("%,d", pd.getDiamond()), cx + 35, 51);
        }

        // ===== TABS =====
        drawGreenTab(g2, 18,  112, 110, 34, "TOKO",         activeTab == Tab.SHOP);
        drawGreenTab(g2, 135, 112, 140, 34, "— INVENTORI —", activeTab == Tab.INVENTORY);

        // ===== PANEL ITEM (kiri, lebih kecil) =====
        int ipw = itemPanelW();
        g2.setColor(new Color(210, 180, 120, 200));
        g2.fill(new RoundRectangle2D.Float(10, 150, ipw, h - 210, 10, 10));
        g2.setColor(new Color(130, 95, 35, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(10, 150, ipw, h - 210, 10, 10));

        // ===== PANEL DETAIL (kanan) =====
        int dpx = detailPanelX(), dpw = detailPanelW();
        int itemPanelH = h - 210; // tinggi panel kiri
        int detailH = itemPanelH - 20; // sedikit lebih kecil dari panel kiri
        int detailY = 160; // sedikit lebih ke bawah dari panel kiri
        g2.setColor(new Color(45, 32, 10, 210));
        g2.fill(new RoundRectangle2D.Float(dpx, 150, dpw, h - 210, 10, 10));
        g2.setColor(new Color(130, 95, 35, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(dpx, 150, dpw, h - 210, 10, 10));

        // ===== ITEM GRID =====
        List<ShopItem> list = getDisplayList();
        for (int i = 0; i < list.size(); i++) {
            drawItemCard(g2, i, list.get(i), i == hoveredItem, i == selectedItem, pd);
        }

        // ===== PANEL DETAIL ISI =====
        drawDetailPanel(g2, dpx, dpw, h, list, pd);

        // ===== NAVIGASI SLIDE (hanya di tab SHOP) =====
        boolean onWeapon = currentSlide == SLIDE_WEAPON;
        if (activeTab == Tab.SHOP) {
            drawNavButton(g2, getPrevSlideRect(), "< ", !onWeapon);
            drawNavButton(g2, getNextSlideRect(), ">",  onWeapon);

            g2.setFont(new Font("Georgia", Font.BOLD, 12));
            g2.setColor(new Color(220, 200, 140));
            String slideLabel = onWeapon ? ". ": ".";
            FontMetrics fm = g2.getFontMetrics();
            int slx = 18 + ipw / 2 - fm.stringWidth(slideLabel) / 2;
            g2.drawString(slideLabel, slx, h - 88);
        }

        // ===== STATUS (dihapus dari sini, dipindah ke detail panel) =====

        // ===== TOMBOL KEMBALI =====
        Rectangle br = getBackRect();
        // Gambar tombol dulu
        GradientPaint bgp = new GradientPaint(br.x, br.y, new Color(50,110,30).brighter(),
                br.x, br.y + br.height, new Color(50,110,30).darker());
        g2.setPaint(bgp);
        g2.fill(new RoundRectangle2D.Float(br.x, br.y, br.width, br.height, 10, 10));
        g2.setColor(new Color(50,110,30).brighter().brighter());
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(br.x, br.y, br.width, br.height, 10, 10));
        g2.setColor(new Color(255,255,255,35));
        g2.fill(new RoundRectangle2D.Float(br.x+4, br.y+3, br.width-8, br.height/2-2, 6, 6));
        // Tanda < lebih besar, sedikit lebih ke bawah
        g2.setFont(new Font("Georgia", Font.BOLD, 18));
        int arrowY = br.y + br.height / 2 + g2.getFontMetrics().getAscent() / 2;
        g2.setColor(new Color(0,0,0,80)); g2.drawString("<", br.x + 11, arrowY + 3);
        g2.setColor(Color.WHITE);         g2.drawString("<", br.x + 10, arrowY + 2);
        // Teks KEMBALI normal
        g2.setFont(new Font("Georgia", Font.BOLD, 13));
        FontMetrics bfm = g2.getFontMetrics();
        int labelY = br.y + br.height / 2 + bfm.getAscent() / 2 - 2;
        g2.setColor(new Color(0,0,0,80)); g2.drawString("KEMBALI", br.x + 31, labelY + 1);
        g2.setColor(Color.WHITE);         g2.drawString("KEMBALI", br.x + 30, labelY);
 
        g2.dispose();
    }
//        // ===== TOMBOL KEMBALI =====
//        drawActionButton(g2, getBackRect(), "< KEMBALI", new Color(50, 110, 30));
//
//        g2.dispose();
//    }

    private void drawDetailPanel(Graphics2D g2, int dpx, int dpw, int h,
                                  List<ShopItem> list, PlayerData pd) {
        int contentY = 162;
        int centerX  = dpx + dpw / 2;

        if (selectedItem < 0 || selectedItem >= list.size()) {
            g2.setFont(new Font("Georgia", Font.ITALIC, 12));
            g2.setColor(new Color(160, 138, 95));
            String hint = "Pilih item untuk detail";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, centerX - fm.stringWidth(hint) / 2, h / 2);
            return;
        }

        ShopItem item = list.get(selectedItem);
        boolean owned = pd != null && pd.getInventory().stream()
                .anyMatch(x -> x.getId().equals(item.getId()));

        // --- Gambar item ---
        int imgS = Math.min(dpw - 28, 88);
        int imgX = centerX - imgS / 2;
        g2.setColor(new Color(42, 28, 8));
        g2.fillRoundRect(imgX, contentY, imgS, imgS, 10, 10);
        g2.setColor(new Color(130, 92, 35));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(imgX, contentY, imgS, imgS, 10, 10);
        BufferedImage img = getItemImage(item.getId());
        if (img != null) g2.drawImage(img, imgX + 6, contentY + 6, imgS - 12, imgS - 12, null);

        int ty = contentY + imgS + 16;

        // --- Nama ---
        g2.setFont(new Font("Georgia", Font.BOLD, 15));
        g2.setColor(new Color(255, 232, 145));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(item.getName(), centerX - fm.stringWidth(item.getName()) / 2, ty);
        ty += 20;

        // --- Badge tipe ---
        Color tc = getTypeColor(item.getType());
        int bw = 64, bh = 17;
        g2.setColor(tc);
        g2.fillRoundRect(centerX - bw / 2, ty, bw, bh, 5, 5);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String tname = item.getType().name();
        g2.drawString(tname, centerX - fm.stringWidth(tname) / 2, ty + 12);
        ty += bh + 14;

        // --- Garis pemisah ---
        g2.setColor(new Color(120, 90, 40, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(dpx + 14, ty - 6, dpx + dpw - 14, ty - 6);

        // --- Deskripsi (wrap, center) ---
        g2.setFont(new Font("Georgia", Font.PLAIN, 11));
        g2.setColor(new Color(205, 188, 152));
        ty = drawWrappedTextCenter(g2, item.getDescription(), centerX, ty, dpw - 24, 15) + 12;

        // --- Garis pemisah ---
        g2.setColor(new Color(120, 90, 40, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(dpx + 14, ty - 4, dpx + dpw - 14, ty - 4);
        ty += 6;

        // --- Harga ---
        g2.setFont(new Font("Georgia", Font.BOLD, 14));
        g2.setColor(new Color(255, 215, 55));
        fm = g2.getFontMetrics();
        String price = item.getPrice() + " KOIN";
        g2.drawString(price, centerX - fm.stringWidth(price) / 2, ty);
        ty += 18;

        // --- Status dimiliki ---
        if (owned) {
            g2.setFont(new Font("Georgia", Font.BOLD, 11));
            g2.setColor(new Color(75, 210, 100));
            fm = g2.getFontMetrics();
            String ow = "Sudah Dimiliki";
            g2.drawString(ow, centerX - fm.stringWidth(ow) / 2, ty);
            ty += 16;
        }

        // --- Notifikasi status (pesan beli/error) di dalam panel detail ---
        if (statusTimer > 0) {
            int alpha = Math.min(255, statusTimer * 10);
            // Kotak notif
            int notifH = 28;
            int notifY = ty + 4;
            g2.setColor(new Color(30, 60, 20, alpha));
            g2.fillRoundRect(dpx + 10, notifY, dpw - 20, notifH, 8, 8);
            g2.setColor(new Color(80, 180, 60, alpha));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(dpx + 10, notifY, dpw - 20, notifH, 8, 8);
            // Teks notif
            g2.setFont(new Font("Georgia", Font.BOLD, 11));
            g2.setColor(new Color(140, 230, 100, alpha));
            fm = g2.getFontMetrics();
            g2.drawString(statusMsg, centerX - fm.stringWidth(statusMsg) / 2, notifY + 18);
        }

        // --- Tombol aksi ---
        if (activeTab == Tab.SHOP) {
            boolean canAfford = pd != null && pd.getGold() >= item.getPrice();
            drawActionButton(g2, getBuyRect(),
                    owned     ? "Sudah Dimiliki"
                  : canAfford ? "BELI  " + item.getPrice() + " KOIN"
                              : "Koin Tidak Cukup",
                    owned ? new Color(65, 115, 45) : new Color(48, 108, 22));
        } else {
            drawActionButton(g2, getEquipRect(),
                    item.isEquipped() ? "LEPAS" : "GUNAKAN", new Color(48, 108, 22));
            drawActionButton(g2, getDelRect(), "HAPUS", new Color(135, 42, 22));
        }
    }

    /** Gambar teks wrap rata tengah */
    private int drawWrappedTextCenter(Graphics2D g2, String text, int cx, int y, int maxW, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) <= maxW) {
                line = new StringBuilder(test);
            } else {
                g2.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, y);
                y += lineH;
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, y);
            y += lineH;
        }
        return y;
    }

    /** Gambar teks dengan word-wrap, return Y akhir */
    private int drawWrappedText(Graphics2D g2, String text, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) <= maxW) {
                line = new StringBuilder(test);
            } else {
                g2.drawString(line.toString(), x, y);
                y += lineH;
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) { g2.drawString(line.toString(), x, y); y += lineH; }
        return y;
    }

    // ===== DRAW HELPERS =====
    private void drawGreenTab(Graphics2D g2, int x, int y, int tw, int th, String label, boolean active) {
        Color fill   = active ? new Color(55, 125, 25)  : new Color(30, 75, 12);
        Color border = active ? new Color(140, 220, 60) : new Color(75, 135, 28);
        GradientPaint gp = new GradientPaint(x, y, active ? new Color(85, 165, 38) : new Color(45, 95, 18),
                                             x, y + th, fill.darker());
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(x, y, tw, th, 8, 8));
        g2.setColor(border);
        g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
        g2.draw(new RoundRectangle2D.Float(x, y, tw, th, 8, 8));
        g2.setFont(new Font("Georgia", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(active ? Color.WHITE : new Color(170, 215, 130));
        g2.drawString(label, x + tw / 2 - fm.stringWidth(label) / 2, y + th / 2 + fm.getAscent() / 2 - 2);
    }

    private void drawActionButton(Graphics2D g2, Rectangle r, String label, Color base) {
        GradientPaint gp = new GradientPaint(r.x, r.y, base.brighter(), r.x, r.y + r.height, base.darker());
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10, 10));
        g2.setColor(base.brighter().brighter());
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10, 10));
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fill(new RoundRectangle2D.Float(r.x + 4, r.y + 3, r.width - 8, r.height / 2 - 2, 6, 6));
        g2.setFont(new Font("Georgia", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + r.width / 2 - fm.stringWidth(label) / 2;
        int ty = r.y + r.height / 2 + fm.getAscent() / 2 - 2;
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawString(label, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(label, tx, ty);
    }

    private void drawNavButton(Graphics2D g2, Rectangle r, String label, boolean enabled) {
        Color c = enabled ? new Color(55, 125, 25) : new Color(70, 60, 40);
        g2.setColor(c);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 8, 8));
        g2.setColor(enabled ? new Color(140, 220, 60) : new Color(95, 85, 55));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 8, 8));
        g2.setFont(new Font("Georgia", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(enabled ? Color.WHITE : new Color(140, 130, 100));
        g2.drawString(label, r.x + r.width / 2 - fm.stringWidth(label) / 2,
                      r.y + r.height / 2 + fm.getAscent() / 2 - 2);
    }

    private void drawItemCard(Graphics2D g2, int i, ShopItem item, boolean hov, boolean sel, PlayerData pd) {
        Rectangle r = getItemRect(i);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRoundRect(r.x + 3, r.y + 3, r.width, r.height, 10, 10);

        // BG
        Color bg = sel ? new Color(135, 92, 28) : hov ? new Color(112, 76, 22) : new Color(82, 55, 16);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10, 10));

        // Shine
        GradientPaint shine = new GradientPaint(r.x, r.y, new Color(255, 220, 140, 45),
                                                r.x, r.y + r.height / 2, new Color(0, 0, 0, 0));
        g2.setPaint(shine);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height / 2, 10, 10));

        // Border
        g2.setColor(sel ? new Color(255, 215, 0) : hov ? new Color(210, 168, 58) : new Color(145, 105, 38));
        g2.setStroke(new BasicStroke(sel ? 3f : 1.5f));
        g2.draw(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10, 10));

        // Gambar
        int imgX = r.x + 7, imgY = r.y + 7, imgW = 72, imgH = 72;
        // Pastikan gambar tidak lebih besar dari kartu
        imgW = Math.min(imgW, r.height - 14);
        imgH = imgW;
        g2.setColor(new Color(42, 28, 8));
        g2.fillRoundRect(imgX, imgY, imgW, imgH, 8, 8);
        g2.setColor(new Color(115, 80, 28));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(imgX, imgY, imgW, imgH, 8, 8);
        BufferedImage img = getItemImage(item.getId());
        if (img != null) g2.drawImage(img, imgX + 4, imgY + 4, imgW - 8, imgH - 8, null);

        // Teks
        int tx  = imgX + imgW + 8;
        int maxW = r.x + r.width - tx - 6;

        // Badge TYPE
        g2.setColor(getTypeColor(item.getType()));
        g2.fillRoundRect(tx, r.y + 7, 55, 16, 5, 5);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        g2.drawString(item.getType().name(), tx + 4, r.y + 18);

        // Badge HARGA
        g2.setColor(new Color(165, 105, 0));
        g2.fillRoundRect(tx + 60, r.y + 7, 72, 16, 5, 5);
        g2.setFont(new Font("Georgia", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        g2.drawString(item.getPrice() + " KOIN", tx + 64, r.y + 18);

        // Nama (clip)
        g2.setFont(new Font("Georgia", Font.BOLD, 13));
        g2.setColor(sel || hov ? new Color(255, 245, 175) : new Color(242, 222, 152));
        g2.setClip(tx, r.y + 26, maxW, 18);
        g2.drawString(item.getName(), tx, r.y + 40);

        // Deskripsi (clip)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(200, 182, 145));
        g2.setClip(tx, r.y + 46, maxW, 15);
        g2.drawString(item.getDescription(), tx, r.y + 58);

        g2.setClip(null);

        // Checkbox dimiliki (hanya kotak, tanpa teks)
        int cbX = tx, cbY = r.y + 72;
        if (activeTab == Tab.SHOP && pd != null) {
            boolean owned = pd.getInventory().stream().anyMatch(x -> x.getId().equals(item.getId()));
            if (owned) {
                // Kotak hijau + centang
                g2.setColor(new Color(55, 175, 75));
                g2.fillRoundRect(cbX, cbY, 13, 13, 3, 3);
                g2.setColor(new Color(18, 95, 38));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(cbX, cbY, 13, 13, 3, 3);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cbX + 2, cbY + 7, cbX + 5, cbY + 10);
                g2.drawLine(cbX + 5, cbY + 10, cbX + 11, cbY + 4);
            } else {
                // Kotak kosong saja
                g2.setColor(new Color(145, 125, 85));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(cbX, cbY, 13, 13, 3, 3);
            }
        } else if (activeTab == Tab.INVENTORY && item.isEquipped()) {
            g2.setColor(new Color(100, 200, 255));
            g2.setFont(new Font("Georgia", Font.BOLD, 10));
            g2.drawString("Dipakai", cbX, cbY + 11);
        }
    }

    private Color getTypeColor(ShopItem.ItemType type) {
        switch (type) {
            case WEAPON: return new Color(172, 52, 52);
            case SKILL:  return new Color(52, 72, 182);
            default:     return COL_BORDER;
        }
    }
}