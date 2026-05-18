package anara.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class FinalBoss extends Entity {

    private int attackCooldown = 60;
    private float animPhase = 0;
    private int phase = 1; // 1 = normal, 2 = enraged at 50% HP
    private int specialCooldown = 180;

    public FinalBoss(float x, float y) {
        super(x, y, 600, 35, 8, 1);
    }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        animPhase += 0.05f;

        // Phase 2 aktif saat HP < 50%: boss bergerak & menyerang lebih kencang
        if (getHpRatio() < 0.5f && phase == 1) {
            phase = 2;
            attack = 50;
        }

        // BUG FIX #5: Blok isEnraged() di bawah ini sebelumnya ada dan
        // meng-override attack dari 50 kembali ke 30 saat HP < 30%,
        // sehingga boss justru LEBIH LEMAH di fase kritis. Blok itu dihapus.
        // isEnraged() (HP < 30%) sekarang hanya dipakai untuk efek visual
        // dan label "MENGAMUK!" di draw(), bukan untuk mengubah stats.

        float dx = targetX - x, dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Speed pergerakan meningkat di phase 2; lebih cepat lagi saat enraged
        float spd;
        if (isEnraged()) {
            spd = 3.5f;  // HP < 30%: paling cepat
        } else if (phase == 2) {
            spd = 2.5f;  // HP 30-50%: cepat
        } else {
            spd = 1.5f;  // HP > 50%: normal
        }

        this.x += (dx / dist) * spd;
        this.y += (dy / dist) * spd;
        this.x = Math.max(50, Math.min(mapW - 50, this.x));
        this.y = Math.max(50, Math.min(mapH - 50, this.y));

        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (specialCooldown > 0) {
            specialCooldown--;
        }
    }

    public boolean canAttack(float px, float py) {
        float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
        return dist < 50 && attackCooldown == 0;
    }

    public boolean canSpecial() {
        return specialCooldown == 0;
    }

    public int doAttack() {
        attackCooldown = 70;
        return attack;
    }

    public int doSpecial() {
        specialCooldown = 200;
        return attack * 2;
    }

    // isEnraged: HP < 30% — digunakan untuk efek visual dan kecepatan
    public boolean isEnraged() {
        return hp < maxHp * 0.3f;
    }

    @Override
    public void draw(Graphics2D g2) {
         Graphics2D p = (Graphics2D) g2.create();

    int cx = (int) x, cy = (int) y;

    float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));

    boolean enraged = isEnraged();

    // Outer aura
    Color auraColor = enraged
            ? new Color(200, 50, 200, (int) (glow * 60))
            : new Color(50, 50, 180, (int) (glow * 50));

    p.setColor(auraColor);
    p.fillOval(cx - 55, cy - 55, 110, 110);

    // Body
    Color bodyCol = enraged
            ? new Color(100, 20, 100)
            : new Color(20, 20, 80);

    p.setColor(bodyCol);
    p.fillOval(cx - 38, cy - 38, 76, 76);

    p.setColor(
            enraged
                    ? new Color(140, 40, 140)
                    : new Color(40, 40, 120)
    );

    p.fillOval(cx - 24, cy - 28, 48, 35);

    // Crown / spikes
    p.setColor(
            enraged
                    ? new Color(180, 60, 0)
                    : new Color(100, 90, 50)
    );

    p.setStroke(new BasicStroke(
            5f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
    ));

    for (int i = -2; i <= 2; i++) {

        int sx = cx + i * 12;

        p.drawLine(
                sx,
                cy - 38,
                sx + i * 3,
                cy - 55 - Math.abs(i) * 5
        );
    }

    // Eyes
    Color eyeCol = enraged
            ? new Color(255, 0, 255)
            : new Color(100, 100, 255);

    p.setColor(eyeCol);

    p.fillOval(cx - 14, cy - 24, 10, 10);
    p.fillOval(cx + 4, cy - 24, 10, 10);

    p.setColor(Color.WHITE);

    p.fillOval(cx - 11, cy - 21, 4, 4);
    p.fillOval(cx + 7, cy - 21, 4, 4);

    // Weapon
    p.setColor(new Color(80, 80, 100));

    p.setStroke(new BasicStroke(
            4f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
    ));

    double wAngle = animPhase;

    int wx1 = cx + (int) (Math.cos(wAngle) * 30);
    int wy1 = cy + (int) (Math.sin(wAngle) * 30);

    int wx2 = cx + (int) (Math.cos(wAngle) * 60);
    int wy2 = cy + (int) (Math.sin(wAngle) * 60);

    p.drawLine(wx1, wy1, wx2, wy2);

    p.setColor(new Color(160, 160, 190));

    p.setStroke(new BasicStroke(
            3f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
    ));

    p.drawArc(
            wx2 - 18,
            wy2 - 18,
            36,
            36,
            (int) Math.toDegrees(wAngle),
            120
    );

    // Enraged text
    if (enraged) {

        p.setFont(new Font(
                "Serif",
                Font.BOLD | Font.ITALIC,
                10
        ));

        p.setColor(new Color(255, 100, 255));

        p.drawString(
                "MENGAMUK!",
                cx - 26,
                cy - 60
        );
    }

    p.dispose();
}
}