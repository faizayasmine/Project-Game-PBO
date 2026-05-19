package anara.game;

import static anara.game.Entity.RNG;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Soldier extends Entity {

    private int attackCooldown = RNG.nextInt(60);
    private Color bodyColor;
    private int animTick = 0;          // ← untuk animasi jalan
    private boolean facingLeft = false; // ← untuk balik arah

    public Soldier(float x, float y, int tier) {
        super(x, y, 40 + tier * 10, 8 + tier * 2, 1, 2);
        bodyColor = tier == 1 ? new Color(60, 80, 60) : new Color(80, 50, 50);
    }

    @Override
    public void update(float playerX, float playerY, int mapW, int mapH) {
        float soldierSpeed = 1.8f;

        // Tentukan arah hadap
        facingLeft = (this.x > playerX);

        if (this.x > playerX + 15) {
            this.x -= soldierSpeed;
        } else if (this.x < playerX - 15) {
            this.x += soldierSpeed;
        }

        if (attackCooldown > 0) attackCooldown--;
        animTick++; // ← update animasi
    }

    public boolean canAttack(float px, float py) {
        float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
        return dist < 110 && attackCooldown == 0;
    }

    public int doAttack() {
        attackCooldown = 60;
        return attack;
    }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        p.setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        int cx = (int) x, cy = (int) y;

        // Bayangan
        p.setColor(new Color(0, 0, 0, 60));
        p.fillOval(cx - 14, cy + 12, 28, 10);

        // Pilih sprite sesuai kondisi
        BufferedImage sprite;
        if (!isAlive()) {
            sprite = anara.utils.AssetManager.soldierMati;
        } else if (attackCooldown < 30 && attackCooldown > 0) {
            sprite = (attackCooldown % 2 == 0)
                    ? anara.utils.AssetManager.soldierAttack1
                    : anara.utils.AssetManager.soldierAttack2;
        } else {
            sprite = (animTick / 15 % 2 == 0)
                    ? anara.utils.AssetManager.soldierJalan1
                    : anara.utils.AssetManager.soldierJalan2;
        }

        // Gambar sprite dengan flip arah
        if (sprite != null) {
            int spriteW = 72;
            int spriteH = 72;
            int drawX = cx - spriteW / 2;
            int drawY = cy - spriteH + 20;

            Graphics2D sg = (Graphics2D) p.create();

            if (facingLeft && isAlive()) {
                // Flip hanya saat hidup
                sg.translate(drawX + spriteW, drawY);
                sg.scale(-1, 1);
                sg.drawImage(sprite, 0, 0, spriteW, spriteH, null);
            } else {
                // Mati atau menghadap kanan → gambar normal
                sg.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
            }

            sg.dispose();
        } else {
            // Fallback
            p.setColor(bodyColor);
            p.fillRoundRect(cx - 11, cy - 13, 22, 26, 8, 8);
        }

        // HP bar

        // HP bar
        drawEntityHP(p, cx, cy - 20, 36, getHpRatio());
        p.dispose();
    }

    private void drawEntityHP(Graphics2D g2, int cx, int cy, int w, float ratio) {
        g2.setColor(new Color(40, 10, 10));
        g2.fillRect(cx - w / 2, cy - 24, w, 4);
        g2.setColor(new Color(200, 40, 40));
        g2.fillRect(cx - w / 2, cy - 24, (int) (w * ratio), 4);
    }
}