package anara.ui;

import anara.core.GameEngine;
import anara.audio.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingScreen extends BasePanel {

    private static final long serialVersionUID = 1L;
    private int hoveredBtn = -1;

    public SettingScreen() {
        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredBtn;
                hoveredBtn = -1;
                if (getSoundToggleRect().contains(e.getPoint())) hoveredBtn = 0;
                if (getQuitRect().contains(e.getPoint())) hoveredBtn = 1;
                if (getBackRect().contains(e.getPoint())) hoveredBtn = 2;
                if (prev != hoveredBtn) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getSoundToggleRect().contains(e.getPoint())) {
                    SoundManager.getInstance().toggleSound();
                    repaint();
                } else if (getQuitRect().contains(e.getPoint())) {
                    int c = JOptionPane.showConfirmDialog(null, "Keluar dari game?", "Konfirmasi Quit", JOptionPane.YES_NO_OPTION);
                    if (c == JOptionPane.YES_OPTION) System.exit(0);
                } else if (getBackRect().contains(e.getPoint())) {
                    GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
                }
            }
        });
    }

    private Rectangle getSoundToggleRect() { return new Rectangle(310, 220, 280, 55); }
    private Rectangle getQuitRect()        { return new Rectangle(310, 310, 280, 55); }
    private Rectangle getBackRect()        { return new Rectangle(30, 570, 110, 36); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        drawBackground(g2, w, h);

        drawTitle(g2, "PENGATURAN", cx, 110, 42);

        // Settings panel
        drawPanel(g2, 260, 155, 380, 240);

        // Sound toggle
        boolean soundOn = SoundManager.getInstance().isSoundEnabled();
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("SUARA GAME", 320, 210);

        String soundLabel = soundOn ? "🔊  SUARA: NYALA" : "🔇  SUARA: MATI";
        Color soundColor = soundOn ? COL_GREEN : COL_RED_LIGHT;

        // Sound button
        g2.setColor(new Color(15, 12, 20, 180));
        g2.fillRoundRect(310, 220, 280, 55, 8, 8);
        g2.setColor(hoveredBtn == 0 ? soundColor.brighter() : soundColor);
        g2.setStroke(new BasicStroke(hoveredBtn == 0 ? 2.5f : 1.5f));
        g2.drawRoundRect(310, 220, 280, 55, 8, 8);
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.setColor(hoveredBtn == 0 ? COL_GOLD_LIGHT : COL_TEXT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(soundLabel, 310 + (280 - fm.stringWidth(soundLabel)) / 2, 255);

        // Visual indicator
        if (soundOn) {
            for (int i = 1; i <= 3; i++) {
                g2.setColor(new Color(60, 180, 80, 80 + i * 40));
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(620, 230, i * 12, 35, -60, 120);
            }
        }

        // Divider
        g2.setColor(new Color(60, 50, 30, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(320, 288, 570, 288);

        // Quit button
        drawButton(g2, 310, 310, 280, 55, "KELUAR GAME", hoveredBtn == 1, false);

        // Back
        drawButton(g2, 30, 570, 110, 36, "◄ KEMBALI", hoveredBtn == 2, false);

        // Version info
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(60, 50, 35));
        g2.drawString("ANARA v1.0 — Prototype Build", cx - 80, h - 15);

        g2.dispose();
    }
}