package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuScreen extends BasePanel {
    private int hoveredBtn = -1;
    private Timer animTimer;
    private float animPhase = 0f;

    private static final String[] MENU_LABELS = {"MULAI PETUALANGAN", "TOKO", "DATA PEMAIN", "PENGATURAN"};
    private static final int BTN_W = 280, BTN_H = 48, BTN_X = 310, BTN_Y_START = 230;

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
                    Rectangle r = getBtnRect(i);
                    if (r.contains(e.getPoint())) { hoveredBtn = i; break; }
                }
                if (prev != hoveredBtn) repaint();
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
        int y = BTN_Y_START + index * (BTN_H + 14);
        return new Rectangle(BTN_X, y, BTN_W, BTN_H);
    }

    private void handleMenuClick(int index) {
        switch (index) {
            case 0: GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAP_SELECT); break;
            case 1: GameEngine.getInstance().showScreen(GameEngine.SCREEN_SHOP); break;
            case 2: GameEngine.getInstance().showScreen(GameEngine.SCREEN_PLAYER_DATA); break;
            case 3: GameEngine.getInstance().showScreen(GameEngine.SCREEN_SETTING); break;
        }
    }

    private void startAnimation() {
        animTimer = new Timer(30, e -> {
            animPhase += 0.04f;
            if (animPhase > Math.PI * 2) animPhase = 0;
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        drawBackground(g2, w, h);

        // Animated particles
        drawParticles(g2, w, h);

        // Left panel - Anara warrior art (top-down)
        drawWarriorPanel(g2, 80, 150, 220, 360);

        // Right panel - menu
        drawPanel(g2, 290, 190, 330, 310);

        // Title
        drawTitle(g2, "ANARA", cx, 110, 64);

        // Player name greeting
        PlayerData player = GameEngine.getInstance().getCurrentPlayer();
        if (player != null) {
            g2.setFont(new Font("Serif", Font.ITALIC, 15));
            g2.setColor(COL_TEXT_DIM);
            String greet = "Selamat datang, " + player.getName();
            g2.drawString(greet, cx - g2.getFontMetrics().stringWidth(greet) / 2, 135);

            // Gold display
            g2.setFont(new Font("Serif", Font.BOLD, 13));
            g2.setColor(COL_GOLD);
            String goldStr = "⚙ " + player.getGold() + " Koin";
            g2.drawString(goldStr, cx - g2.getFontMetrics().stringWidth(goldStr) / 2, 158);
        }

        // Draw menu buttons
        for (int i = 0; i < MENU_LABELS.length; i++) {
            Rectangle r = getBtnRect(i);
            drawButton(g2, r.x, r.y, r.width, r.height, MENU_LABELS[i], hoveredBtn == i, false);
        }

        // Footer hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("Takdir menantimu, prajurit...", cx - 80, h - 20);

        g2.dispose();
    }

    private void drawWarriorPanel(Graphics2D g2, int x, int y, int w, int h) {
        drawPanel(g2, x, y, w, h);

        // Title
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(COL_GOLD);
        g2.drawString("ANARA", x + w / 2 - 25, y + 22);

        int cx = x + w / 2;
        int cy = y + h / 2 + 20;

        // Draw top-down warrior
        drawTopDownWarrior(g2, cx, cy, animPhase);

        // Stats below
        PlayerData p = GameEngine.getInstance().getCurrentPlayer();
        if (p != null) {
            int statY = y + h - 60;
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(COL_TEXT_DIM);
            g2.drawString("ATK +" + p.getTotalAttackBonus(), x + 15, statY);
            g2.drawString("DEF +" + p.getTotalDefenseBonus(), x + 15, statY + 16);
            g2.drawString("SKL +" + p.getTotalSkillBonus(), x + 90, statY);
        }
    }

    static void drawTopDownWarrior(Graphics2D g2, int cx, int cy, float phase) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(cx - 30, cy + 30, 60, 20);

        // Body armor (leather, brown/dark)
        g2.setColor(new Color(80, 55, 30));
        g2.fillOval(cx - 22, cy - 22, 44, 44);

        // Armor highlight
        g2.setColor(new Color(110, 80, 45));
        g2.fillOval(cx - 14, cy - 16, 28, 20);

        // Head
        g2.setColor(new Color(200, 170, 130));
        g2.fillOval(cx - 12, cy - 36, 24, 24);

        // Ponytail (long, flowing)
        g2.setColor(new Color(60, 40, 15));
        int tailX = cx;
        int tailY = cy - 28;
        float swing = (float) Math.sin(phase) * 6f;
        int[] tailXPts = {tailX - 4, tailX + 4, tailX + 2 + (int)swing, tailX - 2 + (int)swing};
        int[] tailYPts = {tailY - 8, tailY - 8, tailY + 35, tailY + 35};
        g2.fillPolygon(tailXPts, tailYPts, 4);

        // Hair top
        g2.setColor(new Color(70, 50, 20));
        g2.fillOval(cx - 10, cy - 42, 20, 14);

        // Arms
        g2.setColor(new Color(80, 55, 30));
        g2.fillOval(cx - 32, cy - 10, 14, 20);
        g2.fillOval(cx + 18, cy - 10, 14, 20);

        // Sword on back (diagonal)
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

        // Glow aura (subtle)
        float glow = (float)(0.5 + 0.5 * Math.sin(phase * 1.3));
        g2.setColor(new Color(220, 175, 60, (int)(glow * 40)));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(cx - 28, cy - 40, 56, 80);
    }

    private void drawParticles(Graphics2D g2, int w, int h) {
        // Floating dust particles
        for (int i = 0; i < 12; i++) {
            float x = (float)((i * 137.5 * animPhase * 8) % w);
            float y = (float)(h - ((animPhase * 30 + i * 70) % h));
            float alpha = (float)(0.2 + 0.15 * Math.sin(animPhase + i));
            g2.setColor(new Color(220, 175, 60, (int)(alpha * 255)));
            g2.fillOval((int)x, (int)y, 3, 3);
        }
    }
}