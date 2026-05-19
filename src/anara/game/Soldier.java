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

    // PARAMETER DIKEMBALIKAN KE 4 AGAR @Override TIDAK ERROR
    @Override
    public void update(float playerX, float playerY, int mapW, int mapH) {
        float soldierSpeed = 1.8f;

        // 1. Pergerakan mengejar Player (Bisa menumpuk langsung di Player)
        if (this.x > playerX + 15) {
            this.x -= soldierSpeed;
        } else if (this.x < playerX - 15) {
            this.x += soldierSpeed;
        }

        // Cooldown serangan
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    // METHOD BARU KHUSUS UNTUK MEMISAHKAN SESAMA SOLDIER
    public void handleSoldierCollision(java.util.List<Soldier> allSoldiers) {
        float minDistance = 24f; // Jarak minimal antar soldier agar tidak tumpang tindih

        for (Soldier other : allSoldiers) {
            if (other == this) {
                continue; // Jangan cek dengan diri sendiri
            }
            // Hitung jarak X antara soldier ini dengan soldier lain
            float dx = this.x - other.x;

            // Jika jaraknya terlalu dekat (saling bertumpuk)
            if (Math.abs(dx) < minDistance) {
                // Berikan dorongan menjauh secara halus
                if (dx == 0) {
                    // Jika posisi X sama persis, beri dorongan acak sedikit ke kanan atau kiri
                    this.x += RNG.nextBoolean() ? 0.5f : -0.5f;
                } else if (dx > 0) {
                    this.x += 0.6f; // Geser ke kanan jika berada di kanan soldier lain
                } else {
                    this.x -= 0.6f; // Geser ke kiri jika berada di kiri soldier lain
                }
            }
        }
    }

    public boolean canAttack(float px, float py) {
        float distX = Math.abs(this.x - px);
        float distY = Math.abs(this.y - py);

        // Jarak serang horizontal dekat (45) DAN tinggi sejajar (di bawah 30)
        return distX < 45 && distY < 30 && attackCooldown == 0;
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
        p.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        p.drawLine(cx + 3, cy - 18, cx + 10, cy - 12);

        // HP bar
        drawEntityHP(p, cx, cy, 18, getHpRatio());

        p.dispose();
    }

    private void drawEntityHP(Graphics2D g2, int cx, int cy, int w, float ratio) {
        g2.setColor(new Color(40, 10, 10));
        g2.fillRect(cx - w / 2, cy - 24, w, 4);
        g2.setColor(new Color(200, 40, 40));
        g2.fillRect(cx - w / 2, cy - 24, (int) (w * ratio), 4);
    }
}
