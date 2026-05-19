package anara.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MiniBoss extends Entity {

    private int attackCooldown = 40;
    private float animPhase = 0;
    private int animTick = 0;
    private boolean facingLeft = false;

    public MiniBoss(float x, float y) {
        super(x, y, 500, 35, 4, 2);
    }

    @Override
    public void update(float playerX, float playerY, int mapW, int mapH) {
        this.y = playerY;
        float miniBossSpeed = 2f;
        int attackRange = 45;
        float distanceX = Math.abs(this.x - playerX);

        // Arah hadap
        if (this.x > playerX + 10) facingLeft = true;
        else if (this.x < playerX - 10) facingLeft = false;

        if (distanceX > attackRange) {
            if (this.x > playerX) {
                this.x -= miniBossSpeed;
            } else {
                this.x += miniBossSpeed;
            }
        } else {
            if (this.attackCooldown <= 0) {
                this.attackCooldown = 60;
            }
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        animPhase += 0.08f;
        animTick++;
    }

    public boolean canAttack(float px, float py) {
        float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
        return dist < 65 && attackCooldown == 0;
    }

    public int doAttack() {
        attackCooldown = 30;
        return attack;
    }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;

        // Bayangan
        p.setColor(new Color(0, 0, 0, 80));
        p.fillOval(cx - 24, cy + 20, 48, 14);

        // Pilih sprite: bergantian attack1 & attack2
        BufferedImage sprite = (animTick / 15 % 2 == 0)
                ? anara.utils.AssetManager.miniBossAttack1
                : anara.utils.AssetManager.miniBossAttack2;

        if (sprite != null) {
            int spriteW = 96;
            int spriteH = 96;
            int drawX = cx - spriteW / 2;
            int drawY = cy - spriteH + 20;

            Graphics2D sg = (Graphics2D) p.create();
            if (facingLeft) {
                sg.translate(drawX + spriteW, drawY);
                sg.scale(-1, 1);
                sg.drawImage(sprite, 0, 0, spriteW, spriteH, null);
            } else {
                sg.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
            }
            sg.dispose();
        } else {
            // Fallback shape
            float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));
            p.setColor(new Color(180, 40, 40, (int) (glow * 50)));
            p.fillOval(cx - 32, cy - 32, 64, 64);
            p.setColor(new Color(80, 20, 20));
            p.fillOval(cx - 22, cy - 22, 44, 44);
        }

        // HP bar
        p.setColor(new Color(40, 10, 10));
        p.fillRect(cx - 36, cy + 28, 72, 7);
        p.setColor(new Color(200, 40, 40));
        p.fillRect(cx - 36, cy + 28, (int) (72 * getHpRatio()), 7);
        p.setColor(new Color(80, 20, 20));
        p.setStroke(new BasicStroke(1f));
        p.drawRect(cx - 36, cy + 28, 72, 7);

        // Label
        p.setFont(new Font("Serif", Font.BOLD, 9));
        p.setColor(new Color(220, 80, 80));
        p.drawString("MINI BOSS", cx - 22, cy + 26);

        p.dispose();
    }
}