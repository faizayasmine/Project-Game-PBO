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
        // KUNCI UTAMA: Jangan pernah menyamakan this.y = playerY secara instan!
        // Biarkan posisi Y Mini Boss konisten di tempat dia di-spawn (di tanah).
        
        float miniBossSpeed = 2f;
        int attackRange = 45;
        float distanceX = Math.abs(this.x - playerX);

        // Arah hadap
        if (this.x > playerX + 10) facingLeft = true;
        else if (this.x < playerX - 10) facingLeft = false;

        // Pergerakan horizontal (Hanya mengejar secara X)
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
        // Pisahkan pengecekan jarak X dan jarak Y secara ketat
        float distX = Math.abs(this.x - px);
        float distY = Math.abs(this.y - py);
        
        // Hanya bisa menyerang jika jarak X dekat (bawah 45) DAN tinggi sejajar di tanah (bawah 30)
        return distX < 45 && distY < 30 && attackCooldown == 0;
    }

    public int doAttack() {
        attackCooldown = 30;
        return attack;
    }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;
        
        int spriteW = 96;
        int spriteH = 96;

        // Bayangan tetap di bawah kaki
        p.setColor(new Color(0, 0, 0, 80));
        p.fillOval(cx - 24, cy + 20, 48, 14);

        // Pilih sprite: bergantian attack1 & attack2
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
            // Fallback shape
            float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));
            p.setColor(new Color(180, 40, 40, (int) (glow * 50)));
            p.fillOval(cx - 32, cy - 32, 64, 64);
            p.setColor(new Color(80, 20, 20));
            p.fillOval(cx - 22, cy - 22, 44, 44);
        }

        // ==========================================
        // PERBAIKAN HUD: DIPINDAH KE ATAS KEPALA BOS
        // ==========================================
        // Menggunakan koordinat (cy - spriteH + 10) supaya posisinya berada di atas kepala karakter ksatria kamu
        int hudY = cy - spriteH + 15; 

        // 1. HP Bar (Atas Kepala)
        p.setColor(new Color(40, 10, 10));
        p.fillRect(cx - 36, hudY, 72, 7);
        p.setColor(new Color(200, 40, 40));
        p.fillRect(cx - 36, hudY, (int) (72 * getHpRatio()), 7);
        p.setColor(new Color(80, 20, 20));
        p.setStroke(new BasicStroke(1f));
        p.drawRect(cx - 36, hudY, 72, 7);

        // 2. Label "MINI BOSS" (Di atas HP Bar-nya lagi)
        p.setFont(new Font("Serif", Font.BOLD, 10));
        p.setColor(new Color(240, 50, 50));
        p.drawString("MINI BOSS", cx - 24, hudY - 5);

        p.dispose();
    }
}