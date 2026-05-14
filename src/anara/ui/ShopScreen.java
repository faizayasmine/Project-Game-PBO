package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.model.ShopItem;
import anara.utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ShopScreen extends BasePanel {

    private enum Tab { SHOP, INVENTORY }
    private Tab activeTab = Tab.SHOP;
    private int hoveredItem = -1;
    private int selectedItem = -1;
    private String statusMsg = "";
    private int statusTimer = 0;
    private javax.swing.Timer uiTimer;

    // Shop catalog
    private static final List<ShopItem> CATALOG = new ArrayList<>(Arrays.asList(
        // Weapons
        new ShopItem("w1", "Pedang Besi",      "Pedang standar prajurit +10 ATK",  80,  10, ShopItem.ItemType.WEAPON),
        new ShopItem("w2", "Pedang Baja",       "Baja tempa terbaik +20 ATK",       160, 20, ShopItem.ItemType.WEAPON),
        new ShopItem("w3", "Pedang Kristal",    "Diberkati cahaya +35 ATK",         300, 35, ShopItem.ItemType.WEAPON),
        // Skills
        new ShopItem("s1", "Serangan Kilat",    "Kurangi cooldown serangan +5 SKL", 100, 5,  ShopItem.ItemType.SKILL),
        new ShopItem("s2", "Ledakan Angin",     "AoE skill lebih kuat +15 SKL",     200, 15, ShopItem.ItemType.SKILL),
        // Defense
        new ShopItem("d1", "Tameng Kulit",      "Armor dasar +8 DEF",               90,  8,  ShopItem.ItemType.DEFENSE),
        new ShopItem("d2", "Tameng Baja",       "Ketahanan tinggi +18 DEF",         180, 18, ShopItem.ItemType.DEFENSE),
        // Items
        new ShopItem("i1", "Ramuan Merah Kecil","Pulihkan 30 HP (sekali pakai)",    50,  30, ShopItem.ItemType.ITEM),
        new ShopItem("i2", "Ramuan Merah Besar","Pulihkan 80 HP (sekali pakai)",    110, 80, ShopItem.ItemType.ITEM)
    ));

    public ShopScreen() {
        setupMouseListeners();
        uiTimer = new javax.swing.Timer(100, e -> {
    if (statusTimer > 0) {
        statusTimer--;
        repaint();
    }
});
        uiTimer.start();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredItem;
                hoveredItem = getItemAt(e.getX(), e.getY());
                if (prev != hoveredItem) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Tab switching
                if (getShopTabRect().contains(e.getPoint())) { activeTab = Tab.SHOP; selectedItem = -1; repaint(); return; }
                if (getInvTabRect().contains(e.getPoint())) { activeTab = Tab.INVENTORY; selectedItem = -1; repaint(); return; }

                // Back
                if (getBackRect().contains(e.getPoint())) {
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
                    return;
                }

                int idx = getItemAt(e.getX(), e.getY());
                if (idx >= 0) { selectedItem = idx; repaint(); }

                // Action buttons
                handleActionButtons(e.getX(), e.getY());
            }
        });
    }

    private int getItemAt(int mx, int my) {
        List<?> list = activeTab == Tab.SHOP ? CATALOG : GameEngine.getInstance().getCurrentPlayer().getInventory();
        for (int i = 0; i < list.size(); i++) {
            Rectangle r = getItemRect(i);
            if (r.contains(mx, my)) return i;
        }
        return -1;
    }

    private Rectangle getItemRect(int i) {
        int cols = 3;
        int col = i % cols, row = i / cols;
        // Jarak X (samping) dijauhkan menjadi 290, Jarak Y (bawah) dijauhkan menjadi 100
        return new Rectangle(30 + col * 290, 160 + row * 100, 265, 80);
    }

    private Rectangle getShopTabRect() { return new Rectangle(30, 100, 120, 36); }
    private Rectangle getInvTabRect() { return new Rectangle(160, 100, 140, 36); }
    private Rectangle getBackRect() { return new Rectangle(30, 570, 110, 36); }

    private void handleActionButtons(int mx, int my) {
        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        if (selectedItem < 0) return;

        if (activeTab == Tab.SHOP) {
            Rectangle buyBtn = new Rectangle(630, 200, 220, 44);
            if (buyBtn.contains(mx, my)) {
                ShopItem item = CATALOG.get(selectedItem);
                // Check already owned
                boolean owned = pd.getInventory().stream().anyMatch(i -> i.getId().equals(item.getId()));
                if (owned) { setStatus("Sudah dimiliki!", true); return; }
                if (pd.getGold() < item.getPrice()) { setStatus("Koin tidak cukup!", true); return; }
                pd.setGold(pd.getGold() - item.getPrice());
                // Clone item for inventory
                ShopItem bought = new ShopItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getStatBonus(), item.getType());
                pd.addItem(bought);
                SaveManager.savePlayer(pd);
                setStatus("Berhasil membeli " + item.getName() + "!", false);
            }
        } else {
            // Inventory actions
            List<ShopItem> inv = pd.getInventory();
            if (selectedItem >= inv.size()) return;
            ShopItem item = inv.get(selectedItem);

            Rectangle equipBtn = new Rectangle(630, 200, 220, 44);
            Rectangle delBtn = new Rectangle(630, 256, 220, 44);

            if (equipBtn.contains(mx, my)) {
                if (item.isEquipped()) { pd.unequipItem(item); setStatus("Dilepas: " + item.getName(), false); }
                else { pd.equipItem(item); setStatus("Dipakai: " + item.getName(), false); }
                SaveManager.savePlayer(pd);
            } else if (delBtn.contains(mx, my)) {
                pd.removeItem(item);
                selectedItem = -1;
                SaveManager.savePlayer(pd);
                setStatus("Item dihapus.", false);
            }
        }
        repaint();
    }

    private void setStatus(String msg, boolean isError) {
        statusMsg = msg;
        statusTimer = 30;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        drawBackground(g2, w, h);

        drawTitle(g2, "TOKO & INVENTORI", cx, 75, 34);

        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        if (pd != null) {
            g2.setFont(new Font("Serif", Font.BOLD, 14));
            g2.setColor(COL_GOLD);
            g2.drawString("Koin: " + pd.getGold(), w - 150, 70);
        }

        // Tabs
        boolean shopActive = activeTab == Tab.SHOP;
        drawButton(g2, 30, 100, 120, 36, "TOKO", !shopActive, shopActive);
        drawButton(g2, 160, 100, 140, 36, "INVENTORI", shopActive, !shopActive);

        // Item grid
        List<?> list = shopActive ? CATALOG : (pd != null ? pd.getInventory() : new ArrayList<>());
        for (int i = 0; i < list.size(); i++) {
            ShopItem item = (ShopItem) list.get(i);
            drawItemCard(g2, i, item, i == hoveredItem, i == selectedItem, pd);
        }

        // Detail panel on right
        if (selectedItem >= 0 && selectedItem < list.size()) {
            drawDetailPanel(g2, (ShopItem) list.get(selectedItem), pd);
        } else {
            drawPanel(g2, 620, 155, 260, 380);
            g2.setFont(new Font("Serif", Font.ITALIC, 13));
            g2.setColor(COL_TEXT_DIM);
            g2.drawString("Pilih item untuk detail", 635, 350);
        }

        // Status
        if (statusTimer > 0) {
            g2.setFont(new Font("Serif", Font.BOLD, 14));
            g2.setColor(COL_GOLD_LIGHT);
            g2.drawString(statusMsg, 30, h - 50);
        }

        drawButton(g2, 30, 570, 110, 36, "◄ KEMBALI", false, false);

        g2.dispose();
    }

    private void drawItemCard(Graphics2D g2, int i, ShopItem item, boolean hov, boolean sel, PlayerData pd) {
        Rectangle r = getItemRect(i);

        Color bg = sel ? new Color(50, 38, 10, 220) : hov ? new Color(30, 22, 6, 180) : new Color(15, 12, 20, 160);
        g2.setColor(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);

        Color border = sel ? COL_GOLD_LIGHT : hov ? COL_GOLD : COL_BORDER;
        g2.setColor(border);
        g2.setStroke(new BasicStroke(sel ? 2f : 1f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);

        // Type badge
        Color typeColor = getTypeColor(item.getType());
        g2.setColor(typeColor);
        g2.fillRoundRect(r.x + 6, r.y + 6, 54, 16, 4, 4);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        g2.drawString(item.getType().name(), r.x + 9, r.y + 18);

        // Name
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(sel || hov ? COL_GOLD_LIGHT : COL_TEXT);
        g2.drawString(item.getName(), r.x + 68, r.y + 22);

        // Desc
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString(item.getDescription(), r.x + 10, r.y + 46);

        // Price or equipped badge
        if (activeTab == Tab.SHOP) {
            g2.setFont(new Font("Serif", Font.BOLD, 13));
            g2.setColor(COL_GOLD);
            g2.drawString(item.getPrice() + " koin", r.x + r.width - 80, r.y + 22);
            boolean owned = pd != null && pd.getInventory().stream().anyMatch(x -> x.getId().equals(item.getId()));
            if (owned) {
                g2.setColor(COL_GREEN);
                g2.setFont(new Font("Serif", Font.BOLD, 11));
                g2.drawString("✓ Dimiliki", r.x + r.width - 80, r.y + 40);
            }
        } else {
            if (item.isEquipped()) {
                g2.setColor(COL_GREEN);
                g2.setFont(new Font("Serif", Font.BOLD, 11));
                g2.drawString("● Dipakai", r.x + r.width - 80, r.y + 22);
            }
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(COL_GOLD);
            g2.drawString("+" + item.getStatBonus(), r.x + r.width - 50, r.y + 60);
        }
    }

    private void drawDetailPanel(Graphics2D g2, ShopItem item, PlayerData pd) {
        drawPanel(g2, 620, 155, 260, 380);

        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.setColor(COL_GOLD);
        g2.drawString(item.getName(), 635, 185);

        g2.setColor(getTypeColor(item.getType()));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("[" + item.getType().name() + "]", 635, 202);

        g2.setColor(COL_BORDER);
        g2.drawLine(635, 210, 865, 210);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(COL_TEXT);
        drawWrapped(g2, item.getDescription(), 635, 228, 230, 16);

        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(COL_GOLD_LIGHT);
        g2.drawString("Bonus: +" + item.getStatBonus(), 635, 285);

        if (activeTab == Tab.SHOP) {
            g2.setFont(new Font("Serif", Font.BOLD, 14));
            g2.setColor(COL_GOLD);
            g2.drawString("Harga: " + item.getPrice() + " koin", 635, 310);

            boolean canAfford = pd != null && pd.getGold() >= item.getPrice();
            boolean owned = pd != null && pd.getInventory().stream().anyMatch(x -> x.getId().equals(item.getId()));
            drawButton(g2, 630, 200 + 110, 220, 44, owned ? "✓ SUDAH DIMILIKI" : (canAfford ? "BELI" : "KOIN KURANG"), false, owned);
        } else {
            drawButton(g2, 630, 200, 220, 44, item.isEquipped() ? "✗ LEPAS" : "✓ GUNAKAN", false, item.isEquipped());
            drawButton(g2, 630, 256, 220, 44, "HAPUS", false, false);
        }
    }

    private void drawWrapped(Graphics2D g2, String text, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int cy = y;
        for (String word : text.split(" ")) {
            if (fm.stringWidth(line + word) > maxW) {
                g2.drawString(line.toString(), x, cy);
                cy += lineH;
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, cy);
    }

    private Color getTypeColor(ShopItem.ItemType type) {
        switch (type) {
            case WEAPON: return new Color(180, 60, 60);
            case SKILL: return new Color(60, 80, 180);
            case DEFENSE: return new Color(60, 140, 80);
            case ITEM: return new Color(140, 100, 40);
            default: return COL_BORDER;
        }
    }
}