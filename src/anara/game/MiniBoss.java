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
        float miniBossSpeed = 2f;
        int attackRange = 45;
        float distanceX = Math.abs(this.x - playerX);

        if (this.x > playerX + 10) facingLeft = true;
        else if (this.x < playerX - 10) facingLeft = false;

        if (distanceX > attackRange) {
            if (this.x > playerX) this.x -= miniBossSpeed;
            else                  this.x += miniBossSpeed;
        }

        // BUG 1 FIX: Cooldown hanya berkurang di sini, tidak direset otomatis di dalam update
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        
        animPhase += 0.08f;
        animTick++;
    }

    public boolean canAttack(float px, float py) {
        float distX = Math.abs(this.x - px);
        float distY = Math.abs(this.y - py);
        return distX < 45 && distY < 30 && attackCooldown == 0;
    }

    public int doAttack() {
        // Cooldown direset ke 60 tick setelah menyerang berhasil dieksekusi
        attackCooldown = 60;
        return attack;
    }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;

        // BUG 3 FIX: Perbesar ukuran sprite dari 96 → 130
        int spriteW = 130;
        int spriteH = 130;

        // Bayangan lebih besar sesuai ukuran
        p.setColor(new Color(0, 0, 0, 80));
        p.fillOval(cx - 32, cy + 20, 64, 18);

        BufferedImage sprite = (animTick / 15 % 2 == 0)
                ? anara.utils.AssetManager.miniBossAttack1
                : anara.utils.AssetManager.miniBossAttack2;

        if (sprite != null) {
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
            float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));
            p.setColor(new Color(180, 40, 40, (int) (glow * 50)));
            p.fillOval(cx - 40, cy - 40, 80, 80);
            p.setColor(new Color(80, 20, 20));
            p.fillOval(cx - 28, cy - 28, 56, 56);
        }

        // BUG 2 FIX: HP Bar saja, TANPA label "MINI BOSS"
        int hudY = cy - spriteH + 15;

        p.setColor(new Color(40, 10, 10));
        p.fillRect(cx - 36, hudY, 72, 7);
        p.setColor(new Color(200, 40, 40));
        p.fillRect(cx - 36, hudY, (int) (72 * getHpRatio()), 7);
        p.setColor(new Color(80, 20, 20));
        p.setStroke(new BasicStroke(1f));
        p.drawRect(cx - 36, hudY, 72, 7);

        p.dispose();
    }
}