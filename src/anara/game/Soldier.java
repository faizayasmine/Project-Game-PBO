
package anara.game;

import static anara.game.Entity.RNG;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Soldier extends Entity {
        private int attackCooldown = RNG.nextInt(60);
        private Color bodyColor;

        public Soldier(float x, float y, int tier) {
            super(x, y, 40 + tier * 10, 8 + tier * 2, 1, 2);
            bodyColor = tier == 1 ? new Color(60, 80, 60) : new Color(80, 50, 50);
        }

        @Override
        public void update(float targetX, float targetY, int mapW, int mapH) {
            float dx = targetX - x, dy = targetY - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 5) {
                this.x += (dx / dist) * speed;
                this.y += (dy / dist) * speed;
            }
            if (attackCooldown > 0) attackCooldown--;
        }

        public boolean canAttack(float px, float py) {
            float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
            return dist < 28 && attackCooldown == 0;
        }

        public int doAttack() { attackCooldown = 60; return attack; }

        @Override
        public void draw(Graphics2D g2) {
            Graphics2D p = (Graphics2D) g2.create();
            p.setRenderingHint(
    java.awt.RenderingHints.KEY_ANTIALIASING,
    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
);

    int cx = (int) x, cy = (int) y;

    p.setColor(new Color(0, 0, 0, 60));
    p.fillOval(cx - 14, cy + 12, 28, 10);

    p.setColor(bodyColor);
   p.fillRoundRect(cx - 11, cy - 13, 22, 26, 8, 8);

    p.setColor(new Color(100, 90, 80));
    p.fillOval(cx - 7, cy - 20, 14, 14);

    // Spear
    p.setColor(new Color(120, 100, 60));
    p.setStroke(new BasicStroke(1.5f));
   p.drawLine(cx + 4, cy - 18, cx + 12, cy + 10);

    p.setColor(new Color(160, 160, 180));

    p.setStroke(new BasicStroke(
            3f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
    ));

   p.drawLine(cx + 3, cy - 18, cx + 10, cy - 12);
   
    // HP bar
   drawEntityHP(p, cx, cy, 18, getHpRatio());

    p.dispose();
}

        private void drawEntityHP(Graphics2D g2, int cx, int cy, int w, float ratio) {
            g2.setColor(new Color(40, 10, 10));
          g2.fillRect(cx - w / 2, cy - 24, w, 4);
            g2.setColor(new Color(200, 40, 40));
   g2.fillRect(cx - w / 2, cy -24, (int)(w * ratio), 4);
        }
    }

  

