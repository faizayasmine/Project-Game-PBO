package anara.game;

import java.awt.*;

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
    private float velocityY = 0;
    private boolean jumping = false;

    private final float GRAVITY = 0.6f;
    private final float JUMP_POWER = -12f;
    private final float GROUND_Y = 300;

    public Player(float x, float y) {
        super(x, y, 150, 25, 5, 3);
    }

    public void setExternalBonuses(int atk, int def) {
        attackBonusExternal = atk;
        defenseBonusExternal = def;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        animPhase += 0.08f;
//        x += dx;
//        y += dy;
        x = Math.max(30, Math.min(mapW - 30, x));
        y = Math.max(30, Math.min(mapH - 30, y));
        
        velocityY += GRAVITY;
        y += velocityY;

        if (y >= GROUND_Y) {
            y = GROUND_Y;
            velocityY = 0;
            jumping = false;
        }
     x = Math.max(30, Math.min(mapW - 30, x));
        y = Math.max(30, Math.min(mapH - 30, y));

        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (skillCooldown > 0) {
            skillCooldown--;
        }
        if (invincibleFrames > 0) {
            invincibleFrames--;
        }
        if (isAttacking) {
            attackFrame++;
            if (attackFrame > 12) {
                isAttacking = false;
                attackFrame = 0;
            }
        }

        if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
            angle = (float) Math.atan2(dy, dx);
        }
       
    }
    public void addMovement(float mx, float my) {
    this.x += mx;
    this.y += my;
}

    public boolean canAttack() {
        return attackCooldown == 0;
    }

    public boolean canSkill() {
        return skillCooldown == 0;
    }

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
        if (invincibleFrames > 0) {
            return;
        }
        takeDamage(Math.max(1, dmg - defense - defenseBonusExternal));
        invincibleFrames = 40;
    }

    public int getAttackCooldownPct() {
        return attackCooldown == 0 ? 100 : (int) ((1f - attackCooldown / 25f) * 100);
    }

    public int getSkillCooldownPct() {
        return skillCooldown == 0 ? 100 : (int) ((1f - skillCooldown / 90f) * 100);
    }

    public boolean isInvincible() {
        return invincibleFrames > 0;
    }

    public float getAngle() {
        return angle;
    }

    public float getAnimPhase() {
        return animPhase;
    }
//
public void jump() {
      velocityY = JUMP_POWER;
        jumping = true;
  }
   public boolean isJumping() {
       return jumping;
   }

    @Override
    public void draw(Graphics2D g2) {
        float anim = animPhase;
        int cx = (int) x, cy = (int) y;

        if (isInvincible() && (invincibleFrames / 5) % 2 == 0) {
            return;
        }

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(cx - 18, cy + 18, 36, 12);

        g2.setColor(new Color(80, 55, 30));
        g2.fillOval(cx - 16, cy - 16, 32, 32);
        g2.setColor(new Color(110, 80, 45));
        g2.fillOval(cx - 10, cy - 12, 20, 15);

        g2.setColor(new Color(200, 170, 130));
        g2.fillOval(cx - 9, cy - 26, 18, 18);

        g2.setColor(new Color(70, 50, 20));
        float swing = (float) Math.sin(anim) * 5f;
        int[] px = {cx - 3, cx + 3, cx + 2 + (int) swing, cx - 2 + (int) swing};
        int[] py = {cy - 20, cy - 20, cy + 18, cy + 18};
        g2.fillPolygon(px, py, 4);

        g2.rotate(angle + Math.PI / 2, cx, cy);
        if (isAttacking) {
            float swingA = (float) (attackFrame / 12.0 * Math.PI);
            g2.rotate(swingA - Math.PI / 3, cx, cy);
        }
        g2.setColor(new Color(170, 170, 190));
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy - 14, cx, cy + 32);
        g2.setColor(new Color(180, 150, 60));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx - 7, cy + 28, cx + 7, cy + 38);
        g2.rotate(-(angle + Math.PI / 2), cx, cy);
        if (isAttacking) {
            g2.rotate(-(attackFrame / 12.0 * Math.PI - Math.PI / 3), cx, cy);
        }
    }
}
