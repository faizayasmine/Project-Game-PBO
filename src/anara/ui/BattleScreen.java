package anara.ui;

import anara.core.GameEngine;
import anara.game.Player;
import anara.game.Soldier;
import anara.game.MiniBoss;
import anara.game.FinalBoss;
import anara.model.PlayerData;
import anara.utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BattleScreen extends BasePanel {

    private int bossSpawnCooldown = 0;
    private boolean finalBossAppeared = false;

    // ===== Map Config =====
    private int mapId = 1;
    private static final int MAP_W = 900, MAP_H = 580;

    // ===== Game State =====
    private enum State {
        PLAYING, WIN, LOSE
    }
    private State state = State.PLAYING;

    // ===== Entities =====
    private Player player;
    private List<Soldier> soldiers = new CopyOnWriteArrayList<>();
    private List<MiniBoss> miniBosses = new CopyOnWriteArrayList<>();
    private FinalBoss finalBoss;

    // ===== Map 1 Timer =====
    private int soldierSpawnTimer = 0;
    private int remainingSoldiersToSpawn = 0;
    private boolean miniBossSpawned = false;

    // ===== Map 2 Timer =====
    private int survivalTicks = 0;
    private static final int SURVIVAL_TARGET = 15 * 60;
    private int spawnCooldown = 0;

    // ===== MAP 3 =====
    private static final int WORLD_W = 3000;
    private int cameraX = 0;
    private Rectangle finishArea;
    private boolean reachedFinish = false;

    // ===== Input =====
    private boolean keyLeft, keyRight;
    private Point mousePos = new Point(450, 300);

    // ===== Game loop =====
    private javax.swing.Timer gameLoop;
    private int gameTick = 0;

    // ===== HUD =====
    private List<DamageText> damageTexts = new CopyOnWriteArrayList<>();
    private String notifText = "";
    private int notifTimer = 0;

    public BattleScreen() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setupInput();
        requestFocusInWindow();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    public void setMapId(int id) {
        this.mapId = id;
    }

    public void startBattle() {
        state = State.PLAYING;
        gameTick = 0;
        survivalTicks = 0;
        soldiers.clear();
        miniBosses.clear();
        finalBoss = null;
        damageTexts.clear();
        miniBossSpawned = false;
        bossSpawnCooldown = 300;
        finalBossAppeared = false;

        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
        player = new Player(MAP_W / 2f, 460f);
        player.setMainPlayer(true);

        if (mapId == 2) {
            player.setPlatforms(new int[][]{
                {30, 190, 350},
                {710, 870, 310}
            });
        }

        player.setExternalBonuses(pd.getTotalAttackBonus());
        initMap();

        gameLoop = new javax.swing.Timer(16, e -> tick());
        gameLoop.start();
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            requestFocus();
        });
    }

    private void initMap() {
        cameraX = 0;
        switch (mapId) {
            case 1:
                soldiers.clear();
                miniBosses.clear();
                remainingSoldiersToSpawn = 5;
                soldierSpawnTimer = 60;
                miniBossSpawned = false;
                showNotif("MAP 1 — DIMULAI!", 150);
                break;
            case 2:
                spawnSoldiers(4, 1);
                showNotif("BERTAHAN 15 DETIK!", 150);
                break;
            case 3:
                player.setX(80);
                player.setY(460f);
                finishArea = new Rectangle(WORLD_W - 120, MAP_H / 2 - 60, 80, 120);
                miniBossSpawned = false;
                reachedFinish = false;
                showNotif("CAPAI GARIS AKHIR!", 150);
                break;
            case 4:
                spawnSoldiers(6, 2);
                finalBoss = null;
                finalBossAppeared = false;
                bossSpawnCooldown = 300;
                showNotif("FINAL BOSS — HADAPI TAKDIRMU!", 180);
                break;
        }
    }

    private void spawnSoldiers(int count, int tier) {
        int groundY = 460;
        for (int i = 0; i < count; i++) {
            int jarakSpawn = 250;
            int offsetX = (Math.random() < 0.5) ? -jarakSpawn : jarakSpawn;
            int spawnX = (int) player.getX() + offsetX + (i * 40);
            int currentMaxW = (mapId == 3) ? WORLD_W : MAP_W;
            spawnX = Math.max(40, Math.min(spawnX, currentMaxW - 60));
            soldiers.add(new Soldier(spawnX, groundY, tier));
        }
    }

    private void spawnMiniBosses(int count) {
        miniBosses.clear();
        float baseX, baseY0, baseY1;
        if (finishArea != null) {
            baseX = finishArea.x - 200;
            baseY0 = 460f;
            baseY1 = 460f;
        } else {
            baseX = MAP_W * 0.65f;
            baseY0 = 460f;
            baseY1 = 460f;
        }
        float[] xs = {baseX, baseX + 120};
        float[] ys = {baseY0, baseY1};
        for (int i = 0; i < count; i++) {
            miniBosses.add(new MiniBoss(xs[i % xs.length], ys[i % ys.length]));
        }
        miniBossSpawned = true;
        showNotif("PERINGATAN: MINI BOSS TELAH MUNCUL!", 180);
    }

    // ===========================
    // GAME LOOP TICK
    // ===========================
    private void tick() {
        if (state != State.PLAYING) {
            return;
        }

        float playerSpeed = 4.0f;
        float moveX = 0;
        if (keyLeft) {
            moveX -= playerSpeed;
        }
        if (keyRight) {
            moveX += playerSpeed;
        }
        if (moveX != 0) {
            player.addMovement(moveX, 0);
        }

        // MAP 1 Spawning
        if (mapId == 1 && remainingSoldiersToSpawn > 0) {
            soldierSpawnTimer--;
            if (soldierSpawnTimer <= 0) {
                spawnSoldiers(1, 1);
                remainingSoldiersToSpawn--;
                soldierSpawnTimer = 180;
            }
        }
        if (mapId == 1 && remainingSoldiersToSpawn == 0
                && soldiers.stream().noneMatch(Soldier::isAlive)
                && !miniBossSpawned) {
            miniBosses.clear();
            miniBosses.add(new MiniBoss((int) (player.getX() + 300), 460));
            miniBossSpawned = true;
            showNotif("PERINGATAN: MINI BOSS TELAH MUNCUL!", 180);
        }

        if (mapId == 3) {
            player.update(mousePos.x + cameraX, mousePos.y, WORLD_W, MAP_H);
            spawnCooldown--;
            cameraX = (int) (player.getX() - MAP_W / 2);
            cameraX = Math.max(0, Math.min(cameraX, WORLD_W - MAP_W));
            if (!miniBossSpawned && spawnCooldown <= 0) {
                float x = player.getX() + 300;
                if (x < WORLD_W - 600) {
                    soldiers.add(new Soldier((int) x, 460, 2));
                }
                spawnCooldown = 120;
            }
            if (!miniBossSpawned && player.getX() >= WORLD_W - 600) {
                soldiers.clear();
                spawnMiniBosses(2);
            }
        } else {
            player.update(mousePos.x, mousePos.y, MAP_W, MAP_H);
        }
        player.updateJump();

        // MAP 4 PHASE SYSTEM
        if (mapId == 4) {
            if (!finalBossAppeared && soldiers.stream().noneMatch(Soldier::isAlive)) {
                // ==========================================
                // PERBAIKAN TYPO SPAWN: Menggunakan perkalian (*) bukan pembagian (/)
                // ==========================================
                finalBoss = new FinalBoss(MAP_W * 0.8f, 460f);
                finalBossAppeared = true;
                showNotif("FINAL BOSS MUNCUL!", 180);
            }
            if (finalBoss != null && finalBoss.isAlive()) {
                bossSpawnCooldown--;
                if (bossSpawnCooldown <= 0) {
                    soldiers.add(new Soldier(
                            (int) (finalBoss.getX() + new Random().nextInt(200) - 100), 460, 2
                    ));
                    showNotif("FINAL BOSS MEMANGGIL PASUKAN!", 90);
                    bossSpawnCooldown = 300;
                }
            }
        }

        // Update Soldier & Hit Player
        for (Soldier s : soldiers) {
            if (s.isAlive()) {
                s.update(player.getX(), player.getY(), MAP_W, MAP_H);
                if (s.canAttack(player.getX(), player.getY())) {
                    int dmg = s.doAttack();
                    player.hit(dmg);
                    spawnDamageText(
                            player.getX() + (float) (Math.random() * 60 - 30),
                            player.getY() - 30, dmg, Color.RED
                    );
                }
            }
        }

        // Update Mini Boss & Hit Player
        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive()) {
                mb.update(player.getX(), player.getY(), MAP_W, MAP_H);
                float selisihY = Math.abs(mb.getY() - player.getY());
                if (mb.canAttack(player.getX(), player.getY()) && selisihY < 40) {
                    int dmg = mb.doAttack();
                    player.hit(dmg);
                    spawnDamageText(
                            player.getX() + (float) (Math.random() * 50 - 25),
                            (player.getY() - 30) + (float) (Math.random() * 20 - 10),
                            dmg, new Color(220, 100, 0)
                    );
                }
            }
        }

        // Update Final Boss & Hit Player
        if (finalBoss != null && finalBoss.isAlive()) {
            // 1. Simpan posisi boss sebelum dia melakukan update AI pergerakan
            float oldBossX = finalBoss.getX();

            finalBoss.update(player.getX(), player.getY(), MAP_W, MAP_H);

            // 2. PENGECUALIAN KHUSUS: Boss tidak boleh menempel secara aktif (Jarak minimal 115px)
            // Namun jika player yang bergerak maju mendekati boss, koordinat boss tidak akan mendorong player
            float finalBossMinDist = 115f; 
            float distBefore = Math.abs(oldBossX - player.getX());
            float distAfter = Math.abs(finalBoss.getX() - player.getX());
            
            if (distAfter < finalBossMinDist && distAfter < distBefore) {
                // Kembalikan posisi X boss ke semula jika pergerakannya sendiri membuat jarak terlalu dekat dengan player
                finalBoss.setX(oldBossX); 
            }

            // Serangan jarak dekat — damage penuh
            if (finalBoss.canMeleeAttack(player.getX(), player.getY())) {
                int dmg = finalBoss.doMeleeAttack();
                player.hit(dmg);
                spawnDamageText(player.getX(), player.getY() - 30, dmg, new Color(200, 0, 200));
            }

            // Serangan jarak jauh — damage kecil (30%)
            if (finalBoss.canRangedAttack(player.getX(), player.getY())) {
                int dmg = finalBoss.doRangedAttack();
                player.hit(dmg);
                spawnDamageText(player.getX(), player.getY() - 30, dmg, new Color(200, 100, 255));
                showNotif("SERANGAN JARAK JAUH!", 60);
            }
        }

        applyEnemySteeringAndSeparation();

        // Map 2: Survival
        if (mapId == 2) {
            if (survivalTicks < SURVIVAL_TARGET) {
                survivalTicks++;
                spawnCooldown--;
                if (spawnCooldown <= 0) {
                    long jumlahSoldierHidup = soldiers.stream().filter(Soldier::isAlive).count();
                    if (jumlahSoldierHidup < 8) {
                        spawnSoldiers(2, survivalTicks > 500 ? 2 : 1);
                    }
                    spawnCooldown = 120 - Math.min(90, survivalTicks / 20);
                }
            } else if (miniBosses.isEmpty()) {
                spawnMiniBosses(1);
            }
        }

        // Update damage texts
        damageTexts.removeIf(d -> d.life <= 0);
        for (DamageText d : damageTexts) {
            d.update();
        }

        if (notifTimer > 0) {
            notifTimer--;
        }

        checkWinLose();
        repaint();
    }

    private void checkWinLose() {
        if (!player.isAlive()) {
            endBattle(false);
            return;
        }
        switch (mapId) {
            case 1:
                if (!miniBosses.isEmpty() && miniBosses.stream().noneMatch(MiniBoss::isAlive)) {
                    endBattle(true);
                }
                break;
            case 2:
                if (survivalTicks >= SURVIVAL_TARGET && !miniBosses.isEmpty()
                        && miniBosses.stream().noneMatch(MiniBoss::isAlive)) {
                    endBattle(true);
                }
                break;
            case 3:
                if (miniBossSpawned && miniBosses.stream().noneMatch(MiniBoss::isAlive)) {
                    endBattle(true);
                }
                break;
            case 4:
                if (finalBossAppeared && finalBoss != null && !finalBoss.isAlive()) {
                    endBattle(true);
                }
                break;
        }
    }

    private void endBattle(boolean won) {
        state = won ? State.WIN : State.LOSE;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (won) {
            PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
            int reward = 50 + mapId * 30;
            pd.setGold(pd.getGold() + reward);
            SaveManager.savePlayer(pd);
        }
        repaint();
    }

    // ========================================================
    // PLAYER ATTACK (FIXED UNIFIED LOGIC)
    // ========================================================
    private void doPlayerAttack() {
        if (!player.isAlive() || state != State.PLAYING || !player.canAttack()) {
            return;
        }
        int dmg = player.doAttack();
        float px = player.getX(), py = player.getY();
        float range = 350f; 

        // Cek arah hadap player berdasarkan posisi mouse
        float targetMouseX = (mapId == 3) ? (mousePos.x + cameraX) : mousePos.x;
        boolean facingRight = targetMouseX >= px;

        // ----------------------------------------------------
        // 1. HIT SOLDIER
        // ----------------------------------------------------
        for (Soldier s : soldiers) {
            if (s.isAlive() && dist(px, py, s.getX(), s.getY()) < range) {
                float dx = s.getX() - px;
                
                // Logika Normal: Hadap kanan artinya dx >= 0, hadap kiri artinya dx <= 0
                boolean isEnemyInFront = (facingRight && dx >= 0) || (!facingRight && dx <= 0);
                boolean isOverlapping = Math.abs(dx) <= 0; // Toleransi kalau bertumpuk dekat

                // JALUR SERANG: Jika tidak di depan AND tidak bertumpuk, maka SKIP (tidak kena hit)
                if (!isEnemyInFront && !isOverlapping) {
                    continue;
                }

                s.takeDamage(dmg);
                spawnDamageText(s.getX(), s.getY() - 20, dmg, COL_GOLD);
            }
        }

        // ----------------------------------------------------
        // 2. HIT MINI BOSS
        // ----------------------------------------------------
        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive() && dist(px, py, mb.getX(), mb.getY()) < range + 20f) {
                float dx = mb.getX() - px;
                
                boolean isEnemyInFront = (facingRight && dx >= 0) || (!facingRight && dx <= 0);
                boolean isOverlapping = Math.abs(dx) <= 30;

                if (!isEnemyInFront && !isOverlapping) {
                    continue;
                }

                mb.takeDamage(dmg);
                spawnDamageText(mb.getX(), mb.getY() - 20, dmg, COL_GOLD);
            }
        }

        // ----------------------------------------------------
        // 3. HIT FINAL BOSS
        // ----------------------------------------------------
        if (finalBoss != null && finalBoss.isAlive()
                && dist(px, py, finalBoss.getX(), finalBoss.getY()) < range + 40f) {
            float dx = finalBoss.getX() - px;
            
            boolean isEnemyInFront = (facingRight && dx >= 0) || (!facingRight && dx <= 0);
            boolean isOverlapping = Math.abs(dx) <= 40;

            // Menggunakan struktur yang sama dengan Soldier/MiniBoss agar tidak kebalik
            if (isEnemyInFront || isOverlapping) {
                finalBoss.takeDamage(dmg);
                spawnDamageText(finalBoss.getX(), finalBoss.getY() - 40, dmg, COL_GOLD);
            }
        }
    }

    private void doPlayerSkill() {
        if (!player.isAlive() || state != State.PLAYING || !player.canSkill()) {
            return;
        }
        int dmg = player.doSkill();
        float px = player.getX(), py = player.getY();
        float range = 140f;
        showNotif("SKILL AKTIF!", 60);

        soldiers.stream()
                .filter(s -> s.isAlive() && dist(px, py, s.getX(), s.getY()) < range)
                .forEach(s -> {
                    s.takeDamage(dmg);
                    spawnDamageText(s.getX(), s.getY() - 20, dmg, COL_GOLD_LIGHT);
                });
        miniBosses.stream()
                .filter(m -> m.isAlive() && dist(px, py, m.getX(), m.getY()) < range)
                .forEach(m -> {
                    m.takeDamage(dmg);
                    spawnDamageText(m.getX(), m.getY() - 20, dmg, COL_GOLD_LIGHT);
                });
        if (finalBoss != null && finalBoss.isAlive()
                && dist(px, py, finalBoss.getX(), finalBoss.getY()) < range) {
            finalBoss.takeDamage(dmg);
            spawnDamageText(finalBoss.getX(), finalBoss.getY() - 40, dmg, COL_GOLD_LIGHT);
        }
    }

    // ===========================
    // RENDER
    // ===========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Graphics2D world = (Graphics2D) g2.create();
        if (mapId == 3) {
            world.translate(-cameraX, 0);
        }
        drawBattleBackground(world);

        if (player != null) {
            for (Soldier s : soldiers) {
                if (s.isAlive()) {
                    s.draw(world);
                }
            }
            for (MiniBoss mb : miniBosses) {
                if (mb.isAlive()) {
                    mb.draw(world);
                }
            }
            if (finalBoss != null && finalBoss.isAlive()) {
                finalBoss.draw(world);
            }
            player.draw(world);

            // 🔥 PINDAHKAN KE SINI: Gambar damage text di dalam context 'world' agar ikut bergeser bersama kamera
            for (DamageText d : damageTexts) {
                d.draw(world);
            }

            if (mapId == 2 && anara.utils.AssetManager.map2Foreground != null) {
                int bW = 180, bH = 120;
                world.drawImage(anara.utils.AssetManager.map2Foreground, -20, MAP_H - 200, bW, bH, null);
                world.drawImage(anara.utils.AssetManager.map2Foreground, MAP_W - 160, MAP_H - 200, bW, bH, null);
                world.drawImage(anara.utils.AssetManager.map2Foreground, -20, MAP_H - 380, bW, bH, null);
                world.drawImage(anara.utils.AssetManager.map2Foreground, MAP_W - 160, MAP_H - 380, bW, bH, null);
            }
        }
        world.dispose(); // Di-dispose setelah objek & damage text selesai digambar

        // ❌ HAPUS BARIS INI (karena sudah dipindahkan ke atas):
        // for (DamageText d : damageTexts) d.draw(g2); 
        drawHUD(g2);
        if (state == State.WIN) {
            drawEndOverlay(g2, true);
        } else if (state == State.LOSE) {
            drawEndOverlay(g2, false);
        }
        if (notifTimer > 0) {
            drawNotif(g2);
        }
        g2.dispose();
    }

    private void drawBattleBackground(Graphics2D g2) {
        int currentWidth = (mapId == 3) ? WORLD_W : MAP_W;

        if (mapId == 1 && anara.utils.AssetManager.map1Background != null) {
            g2.drawImage(anara.utils.AssetManager.map1Background, 0, 0, currentWidth, MAP_H, null);
        } else if (mapId == 2 && anara.utils.AssetManager.map2Background != null) {
            g2.drawImage(anara.utils.AssetManager.map2Background, 0, 0, MAP_W, MAP_H, null);
        } else if (mapId == 3 && anara.utils.AssetManager.map3Background != null) {
            int imgW = anara.utils.AssetManager.map3Background.getWidth();
            for (int x = 0; x < WORLD_W; x += imgW) {
                g2.drawImage(anara.utils.AssetManager.map3Background, x, 0, imgW, MAP_H, null);
            }
        } else if (mapId == 4 && anara.utils.AssetManager.map4Background != null) {
            g2.drawImage(anara.utils.AssetManager.map4Background, 0, 0, currentWidth, MAP_H, null);
        } else {
            Color groundColor;
            switch (mapId) {
                case 1:
                    groundColor = new Color(25, 35, 20);
                    break;
                case 2:
                    groundColor = new Color(30, 20, 15);
                    break;
                case 3:
                    groundColor = new Color(20, 15, 30);
                    break;
                default:
                    groundColor = new Color(15, 10, 20);
                    break;
            }
            g2.setColor(groundColor);
            g2.fillRect(0, 0, currentWidth, MAP_H);
            g2.setColor(new Color(255, 255, 255, 10));
            for (int x = 0; x < currentWidth; x += 60) {
                g2.drawLine(x, 0, x, MAP_H);
            }
            for (int y = 0; y < MAP_H; y += 60) {
                g2.drawLine(0, y, currentWidth, y);
            }
        }

        g2.setColor(new Color(60, 50, 30));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(2, 2, MAP_W - 4, MAP_H - 4);
        g2.setColor(new Color(40, 32, 16));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(8, 8, MAP_W - 16, MAP_H - 16);

        g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 13));
        g2.setColor(new Color(100, 80, 40, 140));
        String[] mapNames = {"", "MAP I — PASUKAN PENJAGA", "MAP II — BERTAHAN HIDUP",
            "MAP III — DUA MINI BOSS", "MAP IV — FINAL BOSS"};
        g2.drawString(mapNames[mapId], 20, MAP_H - 12);
    }

    private void drawHUD(Graphics2D g2) {
        int fixedHudH = 90;
        int startY = getHeight() - fixedHudH;

        g2.setColor(new Color(8, 6, 14));
        g2.fillRect(0, startY, MAP_W, fixedHudH);
        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, startY, MAP_W, startY);

        if (player == null) {
            return;
        }

        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("HP", 15, startY + 20);
        drawHPBar(g2, 40, startY + 8, 200, 12, player.getHpRatio(), player.hp + "/" + player.maxHp);

        g2.setColor(COL_TEXT_DIM);
        g2.drawString("ATK", 15, startY + 40);
        int cdA = player.getAttackCooldownPct();
        g2.setColor(new Color(20, 16, 8));
        g2.fillRect(40, startY + 30, 80, 8);
        g2.setColor(COL_GOLD);
        g2.fillRect(40, startY + 30, (int) (80 * cdA / 100f), 8);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("[KLIK KIRI]", 125, startY + 37);

        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("SKL", 15, startY + 58);
        int cdS = player.getSkillCooldownPct();
        g2.setColor(new Color(10, 10, 25));
        g2.fillRect(40, startY + 48, 80, 8);
        g2.setColor(new Color(80, 120, 200));
        g2.fillRect(40, startY + 48, (int) (80 * cdS / 100f), 8);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("[KLIK KANAN]", 125, startY + 55);

        long alive = soldiers.stream().filter(Soldier::isAlive).count();
        long aliveM = miniBosses.stream().filter(MiniBoss::isAlive).count();
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.setColor(COL_RED_LIGHT);
        g2.drawString("Musuh: " + alive + " prajurit  |  " + aliveM + " mini boss", 270, startY + 22);

        if (mapId == 2) {
            int secLeft = Math.max(0, (SURVIVAL_TARGET - survivalTicks) / 60);
            boolean urgent = secLeft < 5;
            int boxW = 150, boxH = 38;
            int boxX = MAP_W - boxW - 15, boxY = 10;

            g2.setColor(new Color(8, 6, 14, 220));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
            g2.setColor(urgent ? new Color(200, 50, 50) : new Color(180, 140, 40));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

            int iconCX = boxX + 18, iconCY = boxY + boxH / 2;
            g2.setColor(urgent ? new Color(220, 80, 80) : COL_GOLD);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(iconCX - 8, iconCY - 8, 16, 16);
            g2.drawLine(iconCX, iconCY, iconCX + 4, iconCY - 5);
            g2.drawLine(iconCX, iconCY, iconCX + 5, iconCY + 2);

            if (secLeft > 0) {
                g2.setFont(new Font("Serif", Font.BOLD, 22));
                g2.setColor(urgent ? new Color(255, 80, 80) : COL_GOLD_LIGHT);
                g2.drawString(secLeft + "s", boxX + 34, boxY + 26);
                int barY = boxY + boxH + 3;
                g2.setColor(new Color(40, 35, 20));
                g2.fillRoundRect(boxX, barY, boxW, 4, 3, 3);
                float ratio = (float) survivalTicks / SURVIVAL_TARGET;
                g2.setColor(urgent ? new Color(200, 50, 50) : COL_GOLD);
                g2.fillRoundRect(boxX, barY, (int) (boxW * ratio), 4, 3, 3);
            } else {
                g2.setFont(new Font("Serif", Font.BOLD, 11));
                g2.setColor(new Color(255, 80, 80));
                g2.drawString("KALAHKAN BOSS!", boxX + 34, boxY + 24);
            }
        }

        if (finalBoss != null && finalBoss.isAlive()) {
            int barW = 80, barH = 8;
            int bx = (int) finalBoss.getX() - (barW / 2);
            int by = (int) finalBoss.getY() - 25;
            drawHPBar(g2, bx, by, barW, barH, finalBoss.getHpRatio(), "");
            g2.setFont(new Font("Serif", Font.BOLD, 10));
            if (finalBoss.isEnraged()) {
                g2.setColor(new Color(220, 100, 220));
                g2.drawString("MENGAMUK", bx, by - 5);
            } else {
                g2.setColor(new Color(200, 0, 200));
                g2.drawString("FINAL BOSS", bx + 10, by - 5);
            }
        }
    }

    private void drawEndOverlay(Graphics2D g2, boolean won) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, MAP_W, MAP_H);
        int cx = MAP_W / 2, cy = MAP_H / 2;
        String title = won ? "KEMENANGAN!" : "KALAH...";
        drawPanel(g2, cx - 220, cy - 100, 440, 200);
        g2.setFont(new Font("Serif", Font.BOLD, 36));
        g2.setColor(won ? COL_GOLD : COL_RED_LIGHT);
        g2.drawString(title, cx - g2.getFontMetrics().stringWidth(title) / 2, cy - 40);
        if (won) {
            int reward = 50 + mapId * 30;
            g2.setFont(new Font("Serif", Font.PLAIN, 16));
            g2.setColor(COL_GOLD);
            g2.drawString("+" + reward + " Koin", cx - 40, cy - 10);
        }
        drawButton(g2, cx - 180, cy + 30, 160, 40, won ? "LANJUT" : "COBA LAGI", false, false);
        drawButton(g2, cx + 20, cy + 30, 160, 40, "MENU UTAMA", false, false);
    }

    private void drawNotif(Graphics2D g2) {
        float alpha = Math.min(1f, notifTimer / 30f);
        g2.setFont(new Font("Serif", Font.BOLD, 20));
        g2.setColor(new Color(220, 175, 60, (int) (alpha * 230)));
        int nx = MAP_W / 2 - g2.getFontMetrics().stringWidth(notifText) / 2;
        g2.drawString(notifText, nx, 80);
    }

    private void applyEnemySteeringAndSeparation() {
        interface GameEntity {
            float getX();
            float getY();
            void setX(float x);
            void setY(float y);
            float getRadius();
        }
        List<GameEntity> activeEnemies = new ArrayList<>();

        for (Soldier s : soldiers) {
            if (s.isAlive()) {
                activeEnemies.add(new GameEntity() {
                    public float getX() { return s.getX(); }
                    public float getY() { return s.getY(); }
                    public void setX(float x) { s.setX(x); }
                    public void setY(float y) { s.setY(y); }
                    public float getRadius() { return 22f; }
                });
            }
        }
        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive()) {
                activeEnemies.add(new GameEntity() {
                    public float getX() { return mb.getX(); }
                    public float getY() { return mb.getY(); }
                    public void setX(float x) { mb.setX(x); }
                    public void setY(float y) { mb.setY(y); }
                    public float getRadius() { return 35f; }
                });
            }
        }
        if (finalBoss != null && finalBoss.isAlive()) {
            activeEnemies.add(new GameEntity() {
                public float getX() { return finalBoss.getX(); }
                public float getY() { return finalBoss.getY(); }
                public void setX(float x) { finalBoss.setX(x); }
                public void setY(float y) { finalBoss.setY(y); }
                public float getRadius() { return 55f; }
            });
        }

        for (GameEntity e : activeEnemies) {
            e.setY(460f);
        }

        // ===== 1. LOGIKA BAWAAN: Saling dorong antar sesama musuh (TETAP DIKUNCI AGAR TIDAK MENUMPUK) =====
        for (int i = 0; i < activeEnemies.size(); i++) {
            GameEntity e1 = activeEnemies.get(i);
            for (int j = i + 1; j < activeEnemies.size(); j++) {
                GameEntity e2 = activeEnemies.get(j);
                float dx = e1.getX() - e2.getX();
                float dist = Math.abs(dx);
                float minDist = e1.getRadius() + e2.getRadius() + 10f;
                if (dist < minDist) {
                    float overlap = minDist - dist;
                    if (dx == 0) {
                        dx = (Math.random() < 0.5) ? 1f : -1f;
                    }
                    float pushX = (dx / Math.abs(dx)) * (overlap / 2f);
                    e1.setX(e1.getX() + pushX);
                    e2.setX(e2.getX() - pushX);
                }
            }
        }

        // ===== 2. LOGIKA COLLISION PLAYER KINI DIHAPUS TOTAL =====
        // Selesai. Player sekarang bebas menembus/bertumpuk di atas Mini Boss maupun Final Boss,
        // dan posisi rendering Player otomatis berada paling depan (paling atas).
    }

    // ===========================
    // INPUT
    // ===========================
    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                        keyLeft = true;
                        break;
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        keyRight = true;
                        break;
                    case KeyEvent.VK_W:
                        player.jump();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (gameLoop != null) {
                            gameLoop.stop();
                        }
                        GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                        keyLeft = false;
                        break;
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        keyRight = false;
                        break;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mousePos = e.getPoint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                if (state == State.PLAYING) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        doPlayerAttack();
                    }
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        doPlayerSkill();
                    }
                } else {
                    handleEndClick(e.getX(), e.getY());
                }
            }
        });
    }

    private void handleEndClick(int mx, int my) {
        int cx = MAP_W / 2, cy = MAP_H / 2;
        Rectangle btn1 = new Rectangle(cx - 180, cy + 30, 160, 40);
        Rectangle btn2 = new Rectangle(cx + 20, cy + 30, 160, 40);
        if (btn1.contains(mx, my)) {
            if (state == State.WIN) {
                GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAP_SELECT);
            } else {
                startBattle();
            }
        } else if (btn2.contains(mx, my)) {
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        }
    }

    // ===========================
    // HELPERS
    // ===========================
    private float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private void spawnDamageText(float x, float y, int dmg, Color color) {
        damageTexts.add(new DamageText(x, y, dmg, color));
    }

    private void showNotif(String text, int duration) {
        notifText = text;
        notifTimer = duration;
    }

    // ===========================
    // DAMAGE TEXT (inner class)
    // ===========================
    static class DamageText {

        float x, y;
        int dmg, life = 45;
        Color color;
        float vy = -1.5f;

        DamageText(float x, float y, int dmg, Color color) {
            // 🔥 Tambahkan sedikit randomisasi X dan Y agar teks tidak menumpuk kaku di satu titik
            this.x = x + (float) (Math.random() * 30 - 15);
            this.y = y - 20 + (float) (Math.random() * 16 - 8);
            this.dmg = dmg;
            this.color = color;
        }

        void update() {
            y += vy;
            vy *= 0.95f;
            life--;
        }

        void draw(Graphics2D g2) {
            float alpha = Math.min(1f, life / 20f);
            g2.setFont(new Font("Serif", Font.BOLD, 14 + (life > 35 ? 4 : 0)));
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g2.drawString("-" + dmg, (int) x, (int) y);
        }
    }
}
