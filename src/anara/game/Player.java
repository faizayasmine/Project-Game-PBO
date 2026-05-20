package anara.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity {

    private float angle = 0;
    private int attackCooldown = 12;
    private int skillCooldown = 0;
    private int invincibleFrames = 0;
    private int attackBonusExternal = 0;
    private int defenseBonusExternal = 0;
    public boolean isAttacking = false;
    public boolean isUsingSkill = false;
    public int attackFrame = 0;
    private int skillFrame = 0;
    private float animPhase = 0f;
    private boolean mainPlayer = false;
    private boolean jumping = false;
    private boolean onGround = true;
    private boolean facingLeft = false;

    private float velocityY = 0;
    private final float gravity = 0.5f;
    private final float jumpPower = -14f;
    private final int groundY = 460;

    // ===== Platform data (bisa diset dari luar untuk map berbeda) =====
    // Format: {x_start, x_end, y} untuk setiap platform
    private int[][] platforms = {};

    public Player(float x, float y) {
        super(x, y, 200, 35, 5, 3);
    }

    /** Set platform aktif sesuai map */
    public void setPlatforms(int[][] platforms) {
        this.platforms = platforms;
    }

    public void setExternalBonuses(int atk) { attackBonusExternal = atk; }
    public void setMainPlayer(boolean b)    { this.mainPlayer = b; }

    public void jump() {
        if (!mainPlayer || !onGround) return;
        jumping   = true;
        onGround  = false;
        velocityY = jumpPower;
    }

 public void updateJump() {
    if (!mainPlayer) return;

    if (!onGround) {
        y += velocityY;
        velocityY += gravity;
    }

    // ===== DEFINISI PLATFORM (sesuaikan dengan posisi visual di background) =====
    int[][] platforms = {
        // {xStart, xEnd, y}
        {0,   220, 270},   // platform batu KIRI
        {880, 1100, 270},  // platform batu KANAN
    };

    // Cek landing di setiap platform
    for (int[] plat : platforms) {
        int pxStart = plat[0];
        int pxEnd   = plat[1];
        int platY   = plat[2];

        if (x >= pxStart && x <= pxEnd) {
            if (velocityY > 0 && y >= platY - 5 && y <= platY + 15) {
                y = platY;
                jumping = false;
                onGround = true;
                velocityY = 0;
                return;
            }
            // Jatuh dari platform
            if (onGround && y == platY) {
                if (x < pxStart || x > pxEnd) {
                    onGround = false;
                    jumping = true;
                    velocityY = 0;
                }
            }
        }
    }

    // Tanah dasar
    if (y >= groundY) {
        y = groundY;
        jumping = false;
        onGround = true;
        velocityY = 0;
    }
}

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public float getX()       { return x; }
    public float getY()       { return y; }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        dx = 0;
        updateJump();
        animPhase += 0.08f;

        x = Math.max(30, Math.min(mapW - 30, x));

        if (attackCooldown > 0)   attackCooldown--;
        if (skillCooldown > 0)    skillCooldown--;
        if (invincibleFrames > 0) invincibleFrames--;

        if (isAttacking) {
            attackFrame++;
            if (attackFrame > 12) { isAttacking = false; attackFrame = 0; }
        }

        if (isUsingSkill) {
            skillFrame++;
            if (skillFrame > 20) { isUsingSkill = false; skillFrame = 0; }
        }

        if (dx < -0.1f)      facingLeft = true;
        else if (dx > 0.1f)  facingLeft = false;
    }

    public void addMovement(float mx, float my) {
        this.x  += mx;
        this.dx  = mx;
        if (mx < 0)      facingLeft = true;
        else if (mx > 0) facingLeft = false;
    }

    public boolean canAttack() { return attackCooldown == 0; }
    public boolean canSkill()  { return skillCooldown == 0; }

    public int doAttack() {
        attackCooldown = 25;
        isAttacking    = true;
        attackFrame    = 0;
        return attack + attackBonusExternal;
    }

    public int doSkill() {
        skillCooldown  = 90;
        isUsingSkill   = true;
        skillFrame     = 0;
        return (attack + attackBonusExternal) * 2;
    }

    public void hit(int dmg) {
        if (invincibleFrames > 0) return;
        takeDamage(Math.max(1, dmg - defense - defenseBonusExternal));
        invincibleFrames = 40;
    }

    public int getAttackCooldownPct() {
        return attackCooldown == 0 ? 100 : (int)((1f - attackCooldown / 25f) * 100);
    }
    public int getSkillCooldownPct() {
        return skillCooldown == 0 ? 100 : (int)((1f - skillCooldown / 90f) * 100);
    }

    public boolean isInvincible() { return invincibleFrames > 0; }
    public float getAngle()       { return angle; }
    public float getAnimPhase()   { return animPhase; }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;

        boolean blinkHide = isInvincible() && (invincibleFrames / 5) % 2 == 0;

        // Bayangan selalu digambar
        p.setColor(new Color(0, 0, 0, 80));
        p.fillOval(cx - 18, cy + 18, 36, 12);

        if (!blinkHide) {
            BufferedImage sprite;
            if (!isAlive()) {
                sprite = anara.utils.AssetManager.playerEliminasi;
            } else if (isUsingSkill || isAttacking) {
                sprite = anara.utils.AssetManager.playerAttack;
            } else if (Math.abs(dx) > 0.1f) {
                sprite = anara.utils.AssetManager.playerLari;
            } else {
                sprite = anara.utils.AssetManager.playerBasic;
            }

            if (sprite != null) {
                int spriteW = 80, spriteH = 80;
                int drawX = cx - spriteW / 2;
                int drawY = cy - spriteH + 20;

                Graphics2D pg = (Graphics2D) p.create();
                if (!isAlive()) {
                    pg.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
                } else if (facingLeft) {
                    pg.translate(drawX + spriteW, drawY);
                    pg.scale(-1, 1);
                    pg.drawImage(sprite, 0, 0, spriteW, spriteH, null);
                } else {
                    pg.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
                }
                pg.dispose();
            }
        }

        p.dispose();
    }
}