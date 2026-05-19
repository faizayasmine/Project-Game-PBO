package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.model.ShopItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PlayerDataScreen extends BasePanel {
    private int hoveredBtn = -1;
    private Timer animTimer;
    private float animPhase = 0f;

    public PlayerDataScreen() {
        setupMouseListeners();
        animTimer = new Timer(30, e -> { animPhase += 0.05f; repaint(); });
        animTimer.start();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredBtn;
                hoveredBtn = -1;
                if (getBackRect().contains(e.getPoint())) hoveredBtn = 0;
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
                    int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "Keluar dari akun " + getName() + "?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        GameEngine.getInstance().setCurrentPlayer(null);
                        GameEngine.getInstance().showScreen(GameEngine.SCREEN_LOGIN);
                    }
                }
            }
        });
    }

    private String getPlayerName() {
        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        return pd != null ? pd.getName() : "Unknown";
    }

    private Rectangle getBackRect() { return new Rectangle(30, 570, 110, 36); }
    private Rectangle getLogoutRect() { return new Rectangle(760, 570, 110, 36); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        drawBackground(g2, w, h);

        drawTitle(g2, "DATA PEMAIN", cx, 80, 38);

        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        if (pd == null) {
            g2.setColor(COL_TEXT_DIM);
            g2.drawString("Tidak ada data", cx - 60, h / 2);
            return;
        }

        // Left - warrior display
        drawPanel(g2, 40, 110, 280, 430);
        MainMenuScreen.drawTopDownWarrior(g2, 180, 270, animPhase);
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.setColor(COL_GOLD);
        g2.drawString(pd.getName(), 180 - g2.getFontMetrics().stringWidth(pd.getName()) / 2, 160);
        g2.setFont(new Font("Serif", Font.ITALIC, 13));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("Prajurit Pengembara", 180 - 55, 180);

        // Stats
        drawStatRow(g2, 55, 390, "Koin", String.valueOf(pd.getGold()), COL_GOLD);
        drawStatRow(g2, 55, 415, "Senjata equip", String.valueOf(
            pd.getEquippedItems().stream().filter(i -> i.getType() == ShopItem.ItemType.WEAPON).count()), COL_RED_LIGHT);
        drawStatRow(g2, 55, 440, "ATK Bonus", "+" + pd.getTotalAttackBonus(), new Color(220, 120, 80));
        drawStatRow(g2, 55, 480, "SKL Bonus", "+" + pd.getTotalSkillBonus(), new Color(100, 200, 120));

        // Right - inventory list
        drawPanel(g2, 340, 110, 520, 430);
        g2.setFont(new Font("Serif", Font.BOLD, 15));
        g2.setColor(COL_GOLD);
        g2.drawString("ITEM DIMILIKI (" + pd.getInventory().size() + ")", 360, 138);

        g2.setColor(COL_BORDER);
        g2.drawLine(360, 146, 840, 146);

        if (pd.getInventory().isEmpty()) {
            g2.setFont(new Font("Serif", Font.ITALIC, 13));
            g2.setColor(COL_TEXT_DIM);
            g2.drawString("Belum ada item. Kunjungi Toko!", 460, 320);
        } else {
            for (int i = 0; i < pd.getInventory().size() && i < 9; i++) {
                ShopItem item = pd.getInventory().get(i);
                int col = i % 3, row = i / 3;
                int ix = 355 + col * 170, iy = 158 + row * 118;
                drawMiniItemCard(g2, ix, iy, item);
            }
        }

        // Equipped items summary
        drawPanel(g2, 40, 550, 820, 6);

        // Buttons
        drawButton(g2, 30, 570, 110, 36, "◄ KEMBALI", hoveredBtn == 0, false);
        drawButton(g2, 760, 570, 110, 36, "KELUAR AKUN", hoveredBtn == 1, false);

        g2.dispose();
    }

    private void drawStatRow(Graphics2D g2, int x, int y, String label, String value, Color valueColor) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString(label + ":", x, y);
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(valueColor);
        g2.drawString(value, x + 120, y);
    }

    private void drawMiniItemCard(Graphics2D g2, int x, int y, ShopItem item) {
        drawPanel(g2, x, y, 155, 100);
        // Type accent
        Color tc = getTypeColor(item.getType());
        g2.setColor(tc);
        g2.fillRect(x, y, 155, 5);
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.setColor(item.isEquipped() ? COL_GOLD_LIGHT : COL_TEXT);
        drawStringWrapped(g2, item.getName(), x + 8, y + 22, 140);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString(item.getType().name(), x + 8, y + 40);
        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(COL_GOLD);
        g2.drawString("+" + item.getStatBonus() + " bonus", x + 8, y + 58);
        if (item.isEquipped()) {
            g2.setColor(COL_GREEN);
            g2.setFont(new Font("Serif", Font.BOLD, 10));
            g2.drawString("● DIPAKAI", x + 8, y + 78);
        }
    }

    private void drawStringWrapped(Graphics2D g2, String text, int x, int y, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) { g2.drawString(text, x, y); return; }
        // Truncate
        while (text.length() > 0 && fm.stringWidth(text + "...") > maxW) text = text.substring(0, text.length() - 1);
        g2.drawString(text + "...", x, y);
    }

    private Color getTypeColor(ShopItem.ItemType type) {
        switch (type) {
            case WEAPON: return new Color(180, 60, 60);
            case SKILL: return new Color(60, 80, 180);
            default: return COL_BORDER;
        }
    }
}