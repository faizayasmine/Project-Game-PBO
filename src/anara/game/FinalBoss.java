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
    private int specialVisualTimer = 0;
    private int meleeVisualTimer = 0;

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
    if (specialVisualTimer > 0) specialVisualTimer--;
    if (meleeVisualTimer > 0) meleeVisualTimer--;

    // Hanya kejar X, Y dikunci di tanah
    float dx = targetX - x;
    float dist = Math.abs(dx);

    facingLeft = (targetX < this.x);

    float spd;
    if (isEnraged()) spd = 3.5f;
    else if (phase == 2) spd = 2.5f;
    else spd = 1.5f;

    if (dist > 1f) {
        this.x += (dx / dist) * spd;
    }

    // Kunci Y di tanah
    this.y = 460f;
    this.x = Math.max(50, Math.min(mapW - 50, this.x));

    if (attackCooldown > 0) attackCooldown--;
    if (specialCooldown > 0) specialCooldown--;
}

public boolean canMeleeAttack(float px, float py) {
    float distX = Math.abs(px - x);
    return distX < 60 && attackCooldown == 0;
}

public int doMeleeAttack() {
    attackCooldown = 70;
    meleeVisualTimer = 20;
    return attack; // damage penuh
}

    public boolean isEnraged() { return hp < maxHp * 0.3f; }
    
    public boolean canRangedAttack(float px, float py) {
    float dist = (float) Math.sqrt(
        Math.pow(px - x, 2) + Math.pow(py - y, 2)
    );
    // Jarak jauh: lebih dari 60px tapi kurang dari 350px
    return dist >= 60 && dist < 350 && specialCooldown == 0;
}

public int doRangedAttack() {
    specialCooldown = 200;
    specialVisualTimer = 80;
    // Damage kecil: hanya 30% dari attack normal
    return (int)(attack * 0.3f);
}

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;
        boolean enraged = isEnraged();

        // Bayangan lebih besar
        p.setColor(new Color(0, 0, 0, 100));
        p.fillOval(cx - 50, cy + 40, 100, 22);

//        // Aura efek lebih besar
//        float glow = (float) (0.5 + 0.5 * Math.sin(animPhase));
//        Color auraColor = enraged
//                ? new Color(200, 50, 200, (int) (glow * 80))
//                : new Color(80, 50, 180, (int) (glow * 60));
//        p.setColor(auraColor);
//        p.fillOval(cx - 50, cy - 40, 100, 80);

        // Sprite: serangan jauh (finalBossAttack1), serangan dekat (finalBossAttack2), atau idle
        BufferedImage sprite;
        if (meleeVisualTimer > 0) {
            sprite = anara.utils.AssetManager.finalBossAttack2;
        } else if (specialVisualTimer > 0 || enraged || attackCooldown < 40) {
            sprite = anara.utils.AssetManager.finalBossAttack1;
        } else {
            sprite = anara.utils.AssetManager.finalBossBasic;
        }

        int spriteW = 200;
        int spriteH = 200;
        int drawX = cx - spriteW / 2;
        int drawY = cy - spriteH + 40;

        if (sprite != null) {
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
    // Fallback lebih besar dan jelas
    p.setColor(enraged ? new Color(180, 0, 180) : new Color(80, 0, 180));
    p.fillRect(cx - 60, cy - 80, 120, 120);
    p.setColor(Color.WHITE);
    p.setFont(new Font("Arial", Font.BOLD, 11));
    p.drawString("FB NULL", cx - 25, cy - 20);
}
        p.dispose();
    }
}