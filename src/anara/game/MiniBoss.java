package anara.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class MiniBoss extends Entity {
    private int attackCooldown = 40;
    private float animPhase = 0;
    private int pattern = 0;

    public MiniBoss(float x, float y) {
        super(x, y, 200, 20, 4, 2);
    }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        animPhase += 0.06f;
        float dx = targetX - x, dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Pattern: circle strafe then charge
        pattern = (pattern + 1) % 200;
        if (pattern < 100) {
            float strafe = (float) Math.sin(animPhase * 2) * 3;
            float perp = (float) Math.cos(animPhase * 2) * 3;
            this.x += (dx / dist) * 1 + perp;
            this.y += (dy / dist) * 1 + strafe;
        } else {
            this.x += (dx / dist) * 3.5f;
            this.y += (dy / dist) * 3.5f;
        }
        this.x = Math.max(30, Math.min(mapW - 30, this.x));
        this.y = Math.max(30, Math.min(mapH - 30, this.y));
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    public boolean canAttack(float px, float py) {
        float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
        return dist < 35 && attackCooldown == 0;
    }

    public int doAttack() {
        attackCooldown = 50;
        return attack;
    }

    @Override
    public void draw(Graphics2D g2) {
        int cx = (int) x, cy = (int) y;
        float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));

        // Aura
        g2.setColor(new Color(180, 40, 40, (int) (glow * 50)));
        g2.fillOval(cx - 32, cy - 32, 64, 64);

        // Body
        g2.setColor(new Color(80, 20, 20));
        g2.fillOval(cx - 22, cy - 22, 44, 44);
        g2.setColor(new Color(120, 30, 30));
        g2.fillOval(cx - 14, cy - 16, 28, 20);

        // Horns
        g2.setColor(new Color(60, 15, 15));
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx - 12, cy - 22, cx - 18, cy - 38);
        g2.drawLine(cx + 12, cy - 22, cx + 18, cy - 38);

        // Eyes
        g2.setColor(new Color(255, 100, 0));
        g2.fillOval(cx - 8, cy - 18, 7, 7);
        g2.fillOval(cx + 1, cy - 18, 7, 7);

        // HP
        g2.setColor(new Color(40, 10, 10));
        g2.fillRect(cx - 28, cy + 28, 56, 7);
        g2.setColor(new Color(200, 40, 40));
        g2.fillRect(cx - 28, cy + 28, (int) (56 * getHpRatio()), 7);
        g2.setColor(new Color(80, 20, 20));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(cx - 28, cy + 28, 56, 7);

        // Label
        g2.setFont(new Font("Serif", Font.BOLD, 9));
        g2.setColor(COL_RED);
        g2.drawString("MINI BOSS", cx - 22, cy + 26);
    }

    private static final Color COL_RED = new Color(220, 80, 80);
}


