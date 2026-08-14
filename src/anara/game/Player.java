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
    private int skillBonusExternal = 0;
    public boolean isAttacking = false;
    public boolean isUsingSkill = false;
    public int attackFrame = 0;
    public boolean isSkilling = false;
    public int skillFrame = 0;

    private float animPhase = 0f;
    private boolean mainPlayer = false;
    private boolean jumping = false;
    private boolean onGround = true;
    private boolean facingLeft = false;

    private float velocityY = 0;
    private final float gravity = 0.5f;
    private final float jumpPower = -14f;
    private final int groundY = 460;

    // Platform data (diset dari luar sesuai map)
    private int[][] platforms = {};

    public Player(float x, float y) {
        super(x, y, 200, 35, 5, 3);
    }

    public void setPlatforms(int[][] platforms) {
        this.platforms = platforms;
    }

    public void setExternalBonuses(int atk, int skillBonus) {
        attackBonusExternal = atk;
        skillBonusExternal = skillBonus;
        // Catatan: defenseBonusExternal sengaja tidak diisi di sini karena
        // belum ada kategori item DEFENSE di shop. Kalau nanti ditambahkan,
        // sambungkan lewat parameter baru di sini, jangan biarkan menggantung.
    }

    public void setMainPlayer(boolean b) {
        this.mainPlayer = b;
    }

    public void jump() {
        if (!mainPlayer || !onGround) {
            return;
        }
        jumping = true;
        onGround = false;
        velocityY = jumpPower;
    }

    public void updateJump() {
        if (!mainPlayer) {
            return;
        }

        if (!onGround) {
            y += velocityY;
            velocityY += gravity;
        }

        for (int[] plat : platforms) {
            int pxStart = plat[0];
            int pxEnd = plat[1];
            int platY = plat[2];

            if (x >= pxStart && x <= pxEnd) {
                // Mendarat di platform dari atas
                if (velocityY > 0 && y >= platY - 5 && y <= platY + 15) {
                    y = platY;
                    jumping = false;
                    onGround = true;
                    velocityY = 0;
                    return;
                }
                // Jatuh dari tepi platform
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

    /**
     * Reset kecepatan horizontal. Harus dipanggil di AWAL setiap tick,
     * SEBELUM addMovement() dipanggil — supaya dx yang baru diisi oleh
     * addMovement() tidak langsung tertimpa nol lagi oleh update().
     */
    public void resetHorizontalVelocity() {
        dx = 0;
    }

    @Override
    public void update(float targetX, float targetY, int mapW, int mapH) {
        // Catatan: Player tidak mengejar target manapun (kontrol via keyboard,
        // bukan AI-chase seperti Soldier/MiniBoss/FinalBoss), jadi targetX/targetY
        // sengaja tidak dipakai di sini — parameter dipertahankan agar tanda
        // tangan method tetap konsisten dengan override Entity lainnya.
        updateJump();
        animPhase += 0.08f;

        x = Math.max(30, Math.min(mapW - 30, x));

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

        if (isSkilling) {
            skillFrame++;
            if (skillFrame > 20) {
                isSkilling = false;
                skillFrame = 0;
            }
        }

        if (dx < -0.1f) {
            facingLeft = true;
        } else if (dx > 0.1f) {
            facingLeft = false;
        }

        if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
            angle = (float) Math.atan2(dy, dx);
            if (dx < -0.1f) {
                facingLeft = true;
            } else if (dx > 0.1f) {
                facingLeft = false;
            }
        }
    }

    public void addMovement(float mx, float my) {
        this.x += mx;
        this.dx = mx;
        if (mx < 0) {
            facingLeft = true;
        } else if (mx > 0) {
            facingLeft = false;
        }
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
        isSkilling = false;
        skillFrame = 0;
        return attack + attackBonusExternal;
    }

    public int doSkill() {
        // Makin besar bonus SKL dari item yang di-equip, makin cepat cooldown-nya
        skillCooldown = Math.max(30, 90 - skillBonusExternal);
        isSkilling = true;
        skillFrame = 0;
        isAttacking = false;
        attackFrame = 0;
        return (attack + attackBonusExternal) * 2 + skillBonusExternal;
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

    @Override
    public void draw(Graphics2D g2) {
        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;

        // Bayangan di lantai dasar
        p.setColor(new Color(0, 0, 0, 80));
        p.fillOval(cx - 18, cy + 18, 36, 12);

        BufferedImage sprite;

        // --- FIXED LOGIC PEMILIHAN SPRITE ---
        if (!isAlive()) {
            sprite = anara.utils.AssetManager.playerEliminasi; // Mati pakai sprite eliminasi
        } else if (isSkilling) {
            sprite = anara.utils.AssetManager.playerSkill;      // Skill pakai sprite skill (bukan sprite mati)
        } else if (isAttacking) {
            sprite = anara.utils.AssetManager.playerAttack;
        } else if (Math.abs(dx) > 0.1f || jumping) {
            sprite = anara.utils.AssetManager.playerLari;       // Bergerak/lompat pakai sprite lari yang benar
        } else {
            sprite = anara.utils.AssetManager.playerBasic;
        }

        if (sprite != null) {
            // HITUNG ASPECT RATIO ASLI AGAR TIDAK GEPENG
            double aspect = (double) sprite.getWidth() / sprite.getHeight();

            int spriteW = 80;
            int spriteH = 80;
            int drawX = -40;
            int drawY = -60;

            // --- FIXED LOGIC PENENTUAN UKURAN DIMENSI ---
            if (sprite == anara.utils.AssetManager.playerBasic) {
                spriteH = 80;
                spriteW = (int) (spriteH * aspect);
                drawX = -spriteW / 2;
                drawY = 20 - spriteH;
            } else if (sprite == anara.utils.AssetManager.playerAttack) {
                spriteH = 80;
                spriteW = (int) (spriteH * aspect);

                int basicDrawW = 80;
                if (anara.utils.AssetManager.playerBasic != null) {
                    double basicAspect = (double) anara.utils.AssetManager.playerBasic.getWidth() / anara.utils.AssetManager.playerBasic.getHeight();
                    basicDrawW = (int) (80 * basicAspect);
                }
                drawX = -basicDrawW / 2;
                drawY = 20 - spriteH;
            } else if (sprite == anara.utils.AssetManager.playerLari) {
                spriteH = 95;
                spriteW = (int) (spriteH * aspect);
                drawX = -spriteW / 2;
                drawY = 35 - spriteH;
            } else if (sprite == anara.utils.AssetManager.playerSkill) { // SEKARANG BERDIRI SENDIRI
                spriteH = 85;
                spriteW = (int) (spriteH * aspect);
                drawX = -spriteW / 2;
                drawY = 20 - spriteH;
            } else if (sprite == anara.utils.AssetManager.playerEliminasi) {
                spriteH = 110;
                spriteW = (int) (spriteH * aspect);
                drawX = -spriteW / 2;
                drawY = 50 - spriteH;
            }

            // Penyesuaian khusus jika mati sesuai kode lamamu
            if (!isAlive()) {
                spriteH = 50;
                spriteW = (int) (spriteH * aspect);
                drawX = -spriteW / 2;
                drawY = 20 - spriteH;
            }

            // TRANSLASI GRAFIK KE TITIK PUSAT PEMAIN (cx, cy)
            Graphics2D pg = (Graphics2D) p.create();
            pg.translate(cx, cy);

            // LOGIKA FLIPPING/NYERANG KIRI-KANAN YANG AMAN
            if (facingLeft) {
                pg.scale(-1, 1);
            }

            // Gambar sprite pada koordinat lokal
            pg.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
            pg.dispose();
        }

        p.dispose();
    }
}
