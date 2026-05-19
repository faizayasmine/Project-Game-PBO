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
    public int attackFrame = 0;
    private float animPhase = 0f;
    private boolean mainPlayer = false;
    private boolean jumping = false;
    private boolean canJump = true;
    private boolean onGround = true;
    private boolean facingLeft = false; // ← arah hadap player

    private float velocityY = 0;
    private final float gravity = 0.8f;
    private final float jumpPower = -12f;
    private final int groundY = 420;

    public Player(float x, float y) {
        super(x, y, 200, 35, 5, 3);
    }

    public void setExternalBonuses(int atk) { attackBonusExternal = atk; }
    public void setMainPlayer(boolean mainPlayer) { this.mainPlayer = mainPlayer; }

    public void jump() {
        if (!mainPlayer) return;
        if (onGround) {
            jumping = true;
            onGround = false;
            velocityY = jumpPower;
        }
    }

    public void updateJump() {
        if (!mainPlayer) return;
        if (jumping) {
            y += velocityY;
            velocityY += gravity;
            if (y >= groundY) {
                y = groundY;
                jumping = false;
                onGround = true;
                velocityY = 0;
            }
        }
    }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        updateJump();
        animPhase += 0.08f;

        x = Math.max(30, Math.min(mapW - 30, x));
        y = Math.max(30, Math.min(mapH - 30, y));

        if (attackCooldown > 0) attackCooldown--;
        if (skillCooldown > 0) skillCooldown--;
        if (invincibleFrames > 0) invincibleFrames--;

        if (isAttacking) {
            attackFrame++;
            if (attackFrame > 12) {
                isAttacking = false;
                attackFrame = 0;
            }
        }

        // Update arah hadap berdasarkan gerakan
        if (dx < -0.1f) facingLeft = true;
        else if (dx > 0.1f) facingLeft = false;

        if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
            angle = (float) Math.atan2(dy, dx);
        }
    }

    public void addMovement(float mx, float my) {
        this.x += mx;
        this.y += my;
        // Update arah dari input gerakan
        if (mx < 0) facingLeft = true;
        else if (mx > 0) facingLeft = false;
    }

    public boolean canAttack() { return attackCooldown == 0; }
    public boolean canSkill() { return skillCooldown == 0; }

    public int doAttack() {
        attackCooldown = 25;
        isAttacking = true;
        attackFrame = 0;
        return attack + attackBonusExternal;
    }

    public int doSkill() {
        skillCooldown = 90;
        return (attack + attackBonusExternal) * 2;
    }

    public void hit(int dmg) {
        if (invincibleFrames > 0) return;
        takeDamage(Math.max(1, dmg - defense - defenseBonusExternal));
        invincibleFrames = 40;
    }

    public int getAttackCooldownPct() {
        return attackCooldown == 0 ? 100 : (int) ((1f - attackCooldown / 25f) * 100);
    }

    public int getSkillCooldownPct() {
        return skillCooldown == 0 ? 100 : (int) ((1f - skillCooldown / 90f) * 100);
    }

    public boolean isInvincible() { return invincibleFrames > 0; }
    public float getAngle() { return angle; }
    public float getAnimPhase() { return animPhase; }

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;

        // Kedip saat invincible
        if (isInvincible() && (invincibleFrames / 5) % 2 == 0) {
            p.dispose();
            return;
        }

        // Bayangan
        p.setColor(new Color(0, 0, 0, 80));
        p.fillOval(cx - 18, cy + 18, 36, 12);

        // Pilih sprite sesuai kondisi
        BufferedImage sprite;
        if (!isAlive()) {
            sprite = anara.utils.AssetManager.playerEliminasi;
        } else if (isAttacking) {
            sprite = anara.utils.AssetManager.playerAttack;
        } else if (Math.abs(dx) > 0.1f) {
            sprite = anara.utils.AssetManager.playerLari;
        } else {
            sprite = anara.utils.AssetManager.playerBasic;
        }

        // Gambar sprite dengan flip arah
       // Gambar sprite dengan flip arah
        if (sprite != null) {
            int spriteW = 80;
            int spriteH = 80;
            int drawX = cx - spriteW / 2;
            int drawY = cy - spriteH + 20;

            Graphics2D pg = (Graphics2D) p.create();
            if (!isAlive()) {
                // Saat mati tidak perlu flip
                pg.drawImage(sprite, drawX, drawY, spriteW, 50, null);
            } else if (facingLeft) {
                // Flip horizontal yang benar
                pg.translate(drawX + spriteW, drawY);
                pg.scale(-1, 1);
                pg.drawImage(sprite, 0, 0, spriteW, spriteH, null);
            } else {
                pg.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
            }
            pg.dispose();
        }

        p.dispose();
    }
}