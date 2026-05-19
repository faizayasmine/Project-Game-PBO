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
        super(x, y, 500, 35, 4, 2);
    }

    @Override
    public void update(float playerX, float playerY, int mapW, int mapH) {
        this.y = playerY;

        float miniBossSpeed = 2f;
        int attackRange = 45;
        float distanceX = Math.abs(this.x - playerX);

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
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
    }

    public boolean canAttack(float px, float py) {
        // 1. Hitung jarak HORIZONTAL (Kanan-Kiri) saja
        float distX = Math.abs(this.x - px);

        // 2. Hitung selisih TINGGI VERTIKAL (Atas-Bawah)
        float distY = Math.abs(this.y - py);

        // 3. Mini boss HANYA BISA MENYERANG jika:
        //    - Jarak horizontal dekat (di bawah 45 piksel)
        //    - Dan posisi tinggi badan mereka sejajar di tanah (selisih Y di bawah 20 piksel)
        //    - Cooldown serangan sudah siap (0)
        return distX < 45 && distY < 20 && attackCooldown == 0;
    }

    public int doAttack() {
        attackCooldown = 30;
        return attack;
    }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();

        int cx = (int) x, cy = (int) y;

        float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));

        // Aura
        p.setColor(new Color(180, 40, 40, (int) (glow * 50)));
        p.fillOval(cx - 32, cy - 32, 64, 64);

        // Body
        p.setColor(new Color(80, 20, 20));
        p.fillOval(cx - 22, cy - 22, 44, 44);

        p.setColor(new Color(120, 30, 30));
        p.fillOval(cx - 14, cy - 16, 28, 20);

        // Horns
        p.setColor(new Color(60, 15, 15));

        p.setStroke(new BasicStroke(
                4f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        p.drawLine(cx - 12, cy - 22, cx - 18, cy - 38);
        p.drawLine(cx + 12, cy - 22, cx + 18, cy - 38);

        // Eyes
        p.setColor(new Color(255, 100, 0));

        p.fillOval(cx - 8, cy - 18, 7, 7);
        p.fillOval(cx + 1, cy - 18, 7, 7);

        // HP
        p.setColor(new Color(40, 10, 10));
        p.fillRect(cx - 28, cy - 45, 56, 7); // Ubah cy + 28 menjadi cy - 45

        p.setColor(new Color(200, 40, 40));
        p.fillRect(
                cx - 28,
                cy - 45, // Ubah cy + 28 menjadi cy - 45
                (int) (56 * getHpRatio()),
                7
        );

        p.setColor(new Color(80, 20, 20));
        p.setStroke(new BasicStroke(1f));
        p.drawRect(cx - 28, cy - 45, 56, 7); // Ubah cy + 28 menjadi cy - 45

        // Label
        p.setFont(new Font("Serif", Font.BOLD, 9));
        p.setColor(COL_RED);
        p.drawString("MINI BOSS", cx - 22, cy - 50); // Ubah cy + 26 menjadi cy - 50

        p.dispose();
    }

    private static final Color COL_RED = new Color(220, 80, 80);
}
