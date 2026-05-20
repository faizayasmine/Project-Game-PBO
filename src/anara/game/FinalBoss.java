package anara.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FinalBoss extends Entity {

    private int attackCooldown = 60;
    private float animPhase = 0;
    private int phase = 1;
    private int specialCooldown = 180;
    private int animTick = 0;
    private boolean facingLeft = false;

    public FinalBoss(float x, float y) {
        super(x, y, 1000, 45, 8, 1);
    }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        animPhase += 0.05f;
        animTick++;

        if (getHpRatio() < 0.5f && phase == 1) {
            phase = 2;
            attack = 50;
        }

        float dx = targetX - x, dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Arah hadap
        if (targetX < this.x - 20) facingLeft = true;
        else if (targetX > this.x + 20) facingLeft = false;

        float spd;
        if (isEnraged()) spd = 3.5f;
        else if (phase == 2) spd = 2.5f;
        else spd = 1.5f;

        this.x += (dx / dist) * spd;
        this.y += (dy / dist) * spd;
        this.x = Math.max(50, Math.min(mapW - 50, this.x));
        this.y = Math.max(50, Math.min(mapH - 50, this.y));

        if (attackCooldown > 0) attackCooldown--;
        if (specialCooldown > 0) specialCooldown--;
    }

    public boolean canAttack(float px, float py) {
        float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
        return dist < 50 && attackCooldown == 0;
    }

    public boolean canSpecial() { return specialCooldown == 0; }

    public int doAttack() {
        attackCooldown = 70;
        return attack;
    }

    public int doSpecial() {
        specialCooldown = 200;
        return attack * 2;
    }

    public boolean isEnraged() { return hp < maxHp * 0.3f; }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;
        boolean enraged = isEnraged();

        // Bayangan
        p.setColor(new Color(0, 0, 0, 100));
        p.fillOval(cx - 36, cy + 30, 72, 18);

        // Aura efek tetap ditampilkan di belakang sprite
        float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));
        Color auraColor = enraged
                ? new Color(200, 50, 200, (int) (glow * 80))
                : new Color(80, 50, 180, (int) (glow * 60));
        p.setColor(auraColor);
        p.fillOval(cx - 60, cy - 60, 120, 120);

        // Pilih sprite: basic saat normal, attack saat enraged/phase2
        BufferedImage sprite = (enraged || attackCooldown < 40)
                ? anara.utils.AssetManager.finalBossAttack1
                : anara.utils.AssetManager.finalBossBasic;

        if (sprite != null) {
            int spriteW = 120;
            int spriteH = 120;
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
            p.setColor(enraged ? new Color(100, 20, 100) : new Color(20, 20, 80));
            p.fillOval(cx - 38, cy - 38, 76, 76);
        }

        // Enraged text
        if (enraged) {
            p.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 12));
            p.setColor(new Color(255, 100, 255));
            p.drawString("MENGAMUK!", cx - 30, cy - 65);
        }

        p.dispose();
    }
}