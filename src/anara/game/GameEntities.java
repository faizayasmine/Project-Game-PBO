//package anara.game;
//
//import java.awt.*;
//import java.util.Random;
//
//// ===========================
//// BASE ENTITY
//// ===========================
//abstract class Entity {
//    protected float x, y;
//    public int maxHp, hp;
//    public int attack, defense, speed;
//    protected boolean alive = true;
//    public float dx, dy; // velocity
//    protected static final Random RNG = new Random();
//
//    public Entity(float x, float y, int hp, int attack, int defense, int speed) {
//        this.x = x; this.y = y;
//        this.maxHp = hp; this.hp = hp;
//        this.attack = attack; this.defense = defense; this.speed = speed;
//    }
//
//    public void takeDamage(int dmg) {
//        int actual = Math.max(1, dmg - defense);
//        hp = Math.max(0, hp - actual);
//        if (hp <= 0) alive = false;
//    }
//
//    public float getHpRatio() { return (float) hp / maxHp; }
//    public boolean isAlive() { return alive; }
//    public float getX() { return x; }
//    public float getY() { return y; }
//    public Rectangle getBounds(int r) { return new Rectangle((int)x - r, (int)y - r, r * 2, r * 2); }
//
//    public abstract void update(float targetX, float targetY, int mapW, int mapH);
//    public abstract void draw(Graphics2D g2);
//}
//
//// ===========================
//// PLAYER — ANARA
//// ===========================
//public class GameEntities {
//
//    public static class Player extends Entity {
//        private float angle = 0;
//        private int attackCooldown = 12;
//        private int skillCooldown = 0;
//        private int invincibleFrames = 0;
//        private int attackBonusExternal = 0;
//        private int defenseBonusExternal = 0;
//        public boolean isAttacking = false;
//        public int attackFrame = 0;
//        private float animPhase = 0f;
//
//        public Player(float x, float y) {
//            super(x, y, 150, 25, 5, 3);
//        }
//
//        public void setExternalBonuses(int atk, int def) {
//            attackBonusExternal = atk;
//            defenseBonusExternal = def;
//        }
//
//        @Override
//        public void update(float targetX, float targetY, int mapW, int mapH) {
//            animPhase += 0.08f;
//            x += dx;
//            y += dy;
//            // Clamp to map
//            x = Math.max(30, Math.min(mapW - 30, x));
//            y = Math.max(30, Math.min(mapH - 30, y));
//
//            if (attackCooldown > 0) attackCooldown--;
//            if (skillCooldown > 0) skillCooldown--;
//            if (invincibleFrames > 0) invincibleFrames--;
//            if (isAttacking) { attackFrame++; if (attackFrame > 12) { isAttacking = false; attackFrame = 0; } }
//
//            // Face movement direction
//            if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
//                angle = (float) Math.atan2(dy, dx);
//            }
//        }
//
//        public boolean canAttack() { return attackCooldown == 0; }
//        public boolean canSkill() { return skillCooldown == 0; }
//
//        public int doAttack() {
//            attackCooldown = 25;
//            isAttacking = true;
//            attackFrame = 0;
//            return attack + attackBonusExternal;
//        }
//
//        public int doSkill() {
//            skillCooldown = 90;
//            return (attack + attackBonusExternal) * 2;
//        }
//
//        public void hit(int dmg) {
//            if (invincibleFrames > 0) return;
//            takeDamage(Math.max(1, dmg - defense - defenseBonusExternal));
//            invincibleFrames = 40;
//        }
//
//        public int getAttackCooldownPct() { return attackCooldown == 0 ? 100 : (int)((1f - attackCooldown / 25f) * 100); }
//        public int getSkillCooldownPct() { return skillCooldown == 0 ? 100 : (int)((1f - skillCooldown / 90f) * 100); }
//        public boolean isInvincible() { return invincibleFrames > 0; }
//        public float getAngle() { return angle; }
//        public float getAnimPhase() { return animPhase; }
//
//        @Override
//        public void draw(Graphics2D g2) {
//            float anim = animPhase;
//            int cx = (int) x, cy = (int) y;
//
//            // Invincible flash
//            if (isInvincible() && (invincibleFrames / 5) % 2 == 0) return;
//
//            // Shadow
//            g2.setColor(new Color(0, 0, 0, 80));
//            g2.fillOval(cx - 18, cy + 18, 36, 12);
//
//            // Body
//            g2.setColor(new Color(80, 55, 30));
//            g2.fillOval(cx - 16, cy - 16, 32, 32);
//            g2.setColor(new Color(110, 80, 45));
//            g2.fillOval(cx - 10, cy - 12, 20, 15);
//
//            // Head
//            g2.setColor(new Color(200, 170, 130));
//            g2.fillOval(cx - 9, cy - 26, 18, 18);
//
//            // Ponytail
//            g2.setColor(new Color(70, 50, 20));
//            float swing = (float) Math.sin(anim) * 5f;
//            int[] px = {cx - 3, cx + 3, cx + 2 + (int)swing, cx - 2 + (int)swing};
//            int[] py = {cy - 20, cy - 20, cy + 18, cy + 18};
//            g2.fillPolygon(px, py, 4);
//
//            // Sword (rotated to face direction)
//            g2.rotate(angle + Math.PI / 2, cx, cy);
//            if (isAttacking) {
//                float swingA = (float)(attackFrame / 12.0 * Math.PI);
//                g2.rotate(swingA - Math.PI / 3, cx, cy);
//            }
//            g2.setColor(new Color(170, 170, 190));
//            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            g2.drawLine(cx, cy - 14, cx, cy + 32);
//            g2.setColor(new Color(180, 150, 60));
//            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            g2.drawLine(cx - 7, cy + 28, cx + 7, cy + 38);
//            g2.rotate(-(angle + Math.PI / 2), cx, cy);
//            if (isAttacking) g2.rotate(-(attackFrame / 12.0 * Math.PI - Math.PI / 3), cx, cy);
//        }
//    }
//
//    // ===========================
//    // SOLDIER ENEMY
//    // ===========================
//    public static class Soldier extends Entity {
//        private int attackCooldown = RNG.nextInt(60);
//        private Color bodyColor;
//
//        public Soldier(float x, float y, int tier) {
//            super(x, y, 40 + tier * 10, 8 + tier * 2, 1, 2);
//            bodyColor = tier == 1 ? new Color(60, 80, 60) : new Color(80, 50, 50);
//        }
//
//        @Override
//        public void update(float targetX, float targetY, int mapW, int mapH) {
//            float dx = targetX - x, dy = targetY - y;
//            float dist = (float) Math.sqrt(dx * dx + dy * dy);
//            if (dist > 5) {
//                this.x += (dx / dist) * speed;
//                this.y += (dy / dist) * speed;
//            }
//            if (attackCooldown > 0) attackCooldown--;
//        }
//
//        public boolean canAttack(float px, float py) {
//            float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
//            return dist < 28 && attackCooldown == 0;
//        }
//
//        public int doAttack() { attackCooldown = 60; return attack; }
//
//        @Override
//        public void draw(Graphics2D g2) {
//            int cx = (int) x, cy = (int) y;
//            g2.setColor(new Color(0, 0, 0, 60));
//            g2.fillOval(cx - 14, cy + 12, 28, 10);
//            g2.setColor(bodyColor);
//            g2.fillOval(cx - 13, cy - 13, 26, 26);
//            g2.setColor(new Color(100, 90, 80));
//            g2.fillOval(cx - 7, cy - 20, 14, 14);
//            // Spear
//            g2.setColor(new Color(120, 100, 60));
//            g2.setStroke(new BasicStroke(2f));
//            g2.drawLine(cx + 10, cy - 28, cx + 10, cy + 20);
//            g2.setColor(new Color(160, 160, 180));
//            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            g2.drawLine(cx + 8, cy - 28, cx + 12, cy - 20);
//            // HP bar
//            drawEntityHP(g2, cx, cy, 28, getHpRatio());
//        }
//
//        private void drawEntityHP(Graphics2D g2, int cx, int cy, int w, float ratio) {
//            g2.setColor(new Color(40, 10, 10));
//            g2.fillRect(cx - w / 2, cy - 32, w, 5);
//            g2.setColor(new Color(200, 40, 40));
//            g2.fillRect(cx - w / 2, cy -32, (int)(w * ratio), 5);
//        }
//    }
//
//    // ===========================
//    // MINI BOSS
//    // ===========================
//    public static class MiniBoss extends Entity {
//        private int attackCooldown = 40;
//        private float animPhase = 0;
//        private int pattern = 0;
//
//        public MiniBoss(float x, float y) {
//            super(x, y, 200, 20, 4, 2);
//        }
//
//        @Override
//        public void update(float targetX, float targetY, int mapW, int mapH) {
//            animPhase += 0.06f;
//            float dx = targetX - x, dy = targetY - y;
//            float dist = (float) Math.sqrt(dx * dx + dy * dy);
//
//            // Pattern: circle strafe then charge
//            pattern = (pattern + 1) % 200;
//            if (pattern < 100) {
//                float strafe = (float) Math.sin(animPhase * 2) * 3;
//                float perp = (float) Math.cos(animPhase * 2) * 3;
//                this.x += (dx / dist) * 1 + perp;
//                this.y += (dy / dist) * 1 + strafe;
//            } else {
//                this.x += (dx / dist) * 3.5f;
//                this.y += (dy / dist) * 3.5f;
//            }
//            this.x = Math.max(30, Math.min(mapW - 30, this.x));
//            this.y = Math.max(30, Math.min(mapH - 30, this.y));
//            if (attackCooldown > 0) attackCooldown--;
//        }
//
//        public boolean canAttack(float px, float py) {
//            float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
//            return dist < 35 && attackCooldown == 0;
//        }
//
//        public int doAttack() { attackCooldown = 50; return attack; }
//
//        @Override
//        public void draw(Graphics2D g2) {
//            int cx = (int) x, cy = (int) y;
//            float glow = (float)(0.5 + 0.5 * Math.sin(animPhase));
//
//            // Aura
//            g2.setColor(new Color(180, 40, 40, (int)(glow * 50)));
//            g2.fillOval(cx - 32, cy - 32, 64, 64);
//
//            // Body
//            g2.setColor(new Color(80, 20, 20));
//            g2.fillOval(cx - 22, cy - 22, 44, 44);
//            g2.setColor(new Color(120, 30, 30));
//            g2.fillOval(cx - 14, cy - 16, 28, 20);
//
//            // Horns
//            g2.setColor(new Color(60, 15, 15));
//            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            g2.drawLine(cx - 12, cy - 22, cx - 18, cy - 38);
//            g2.drawLine(cx + 12, cy - 22, cx + 18, cy - 38);
//
//            // Eyes
//            g2.setColor(new Color(255, 100, 0));
//            g2.fillOval(cx - 8, cy - 18, 7, 7);
//            g2.fillOval(cx + 1, cy - 18, 7, 7);
//
//            // HP
//            g2.setColor(new Color(40, 10, 10));
//            g2.fillRect(cx - 28, cy + 28, 56, 7);
//            g2.setColor(new Color(200, 40, 40));
//            g2.fillRect(cx - 28, cy + 28, (int)(56 * getHpRatio()), 7);
//            g2.setColor(new Color(80, 20, 20));
//            g2.setStroke(new BasicStroke(1f));
//            g2.drawRect(cx - 28, cy + 28, 56, 7);
//
//            // Label
//            g2.setFont(new Font("Serif", Font.BOLD, 9));
//            g2.setColor(COL_RED);
//            g2.drawString("MINI BOSS", cx - 22, cy + 26);
//        }
//
//        private static final Color COL_RED = new Color(220, 80, 80);
//    }
//
//    // ===========================
//    // FINAL BOSS
//    // ===========================
//    public static class FinalBoss extends Entity {
//        private int attackCooldown = 60;
//        private float animPhase = 0;
//        private int phase = 1; // 1 = normal, 2 = enraged at 50% HP
//        private int specialCooldown = 180;
//
//        public FinalBoss(float x, float y) {
//            super(x, y, 600, 35, 8, 1);
//        }
//
//        @Override
//        public void update(float targetX, float targetY, int mapW, int mapH) {
//            animPhase += 0.05f;
//            if (getHpRatio() < 0.5f && phase == 1) {
//                phase = 2;
//                speed = 2;
//                attack = 50;
//            }
//            float dx = targetX - x, dy = targetY - y;
//            float dist = (float) Math.sqrt(dx * dx + dy * dy);
//            float spd = phase == 2 ? 2.5f : 1.5f;
//            this.x += (dx / dist) * spd;
//            this.y += (dy / dist) * spd;
//            this.x = Math.max(50, Math.min(mapW - 50, this.x));
//            this.y = Math.max(50, Math.min(mapH - 50, this.y));
//            if (attackCooldown > 0) attackCooldown--;
//            if (specialCooldown > 0) specialCooldown--;
//        }
//
//        public boolean canAttack(float px, float py) {
//            float dist = (float) Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
//            return dist < 50 && attackCooldown == 0;
//        }
//
//        public boolean canSpecial() { return specialCooldown == 0; }
//        public int doAttack() { attackCooldown = 70; return attack; }
//        public int doSpecial() { specialCooldown = 200; return attack * 2; }
//        public boolean isEnraged() { return phase == 2; }
//
//        @Override
//        public void draw(Graphics2D g2) {
//            int cx = (int) x, cy = (int) y;
//            float glow = (float)(0.5 + 0.5 * Math.sin(animPhase));
//            boolean enraged = isEnraged();
//
//            // Outer aura
//            Color auraColor = enraged ? new Color(200, 50, 200, (int)(glow * 60)) : new Color(50, 50, 180, (int)(glow * 50));
//            g2.setColor(auraColor);
//            g2.fillOval(cx - 55, cy - 55, 110, 110);
//
//            // Body
//            Color bodyCol = enraged ? new Color(100, 20, 100) : new Color(20, 20, 80);
//            g2.setColor(bodyCol);
//            g2.fillOval(cx - 38, cy - 38, 76, 76);
//            g2.setColor(enraged ? new Color(140, 40, 140) : new Color(40, 40, 120));
//            g2.fillOval(cx - 24, cy - 28, 48, 35);
//
//            // Crown/Helmet spikes
//            g2.setColor(enraged ? new Color(180, 60, 0) : new Color(100, 90, 50));
//            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            for (int i = -2; i <= 2; i++) {
//                int sx = cx + i * 12;
//                g2.drawLine(sx, cy - 38, sx + i * 3, cy - 55 - Math.abs(i) * 5);
//            }
//
//            // Eyes (glowing)
//            Color eyeCol = enraged ? new Color(255, 0, 255) : new Color(100, 100, 255);
//            g2.setColor(eyeCol);
//            g2.fillOval(cx - 14, cy - 24, 10, 10);
//            g2.fillOval(cx + 4, cy - 24, 10, 10);
//            g2.setColor(Color.WHITE);
//            g2.fillOval(cx - 11, cy - 21, 4, 4);
//            g2.fillOval(cx + 7, cy - 21, 4, 4);
//
//            // Weapon — dark scythe
//            g2.setColor(new Color(80, 80, 100));
//            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            double wAngle = animPhase;
//            int wx1 = cx + (int)(Math.cos(wAngle) * 30), wy1 = cy + (int)(Math.sin(wAngle) * 30);
//            int wx2 = cx + (int)(Math.cos(wAngle) * 60), wy2 = cy + (int)(Math.sin(wAngle) * 60);
//            g2.drawLine(wx1, wy1, wx2, wy2);
//            g2.setColor(new Color(160, 160, 190));
//            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            g2.drawArc(wx2 - 18, wy2 - 18, 36, 36, (int)Math.toDegrees(wAngle), 120);
//
//            // Enraged label
//            if (enraged) {
//                g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 10));
//                g2.setColor(new Color(255, 100, 255));
//                g2.drawString("MENGAMUK!", cx - 26, cy - 60);
//            }
//        }
//    }
//}