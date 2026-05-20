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

    // ===== Map 2 timer =====
    private int survivalTicks = 0;
    private static final int SURVIVAL_TARGET = 15 * 60; // 15 sec * 60 fps
    private int spawnCooldown = 0;

    // ===== MAP 3 =====
    // World map lebih panjang dari layar
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
        // BUG FIX #3: Reset bossSpawnCooldown saat battle dimulai / retry
        // Sebelumnya tidak direset, sehingga saat retry nilai bisa negatif
        // dan boss langsung memanggil pasukan begitu muncul.
        bossSpawnCooldown = 300;
        finalBossAppeared = false;

        PlayerData pd = GameEngine.getInstance().getCurrentPlayer();

        // MAP_H itu 580. Kalau dibagi 2 = 290 (tengah). 
        // Kita tambah 130f agar posisinya turun menjadi 420 lebih rendah ke tanah
        player = new Player(MAP_W / 2f, 460f);
        player.setMainPlayer(true);
        
        // Set platform sesuai map
if (mapId == 2) {
    // {x_start, x_end, y} — posisi batu kiri dan kanan
    
    player.setPlatforms(new int[][]{
        {30,  190, 350},  // batu kiri
        {710, 870, 310}   // batu kanan
    });
}

        player.setExternalBonuses(
                pd.getTotalAttackBonus()
        );

        initMap();
        gameLoop = new javax.swing.Timer(16, e -> tick());
        gameLoop.start();
        showNotif("MAP " + mapId + " — DIMULAI!", 120);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
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
            int jarakSpawn = 250; // Sedikit diperpendek agar cepat masuk layar
            int offsetX = (Math.random() < 0.5) ? -jarakSpawn : jarakSpawn;

            int spawnX = (int) player.getX() + offsetX + (i * 40);
            int spawnY = groundY;

            // Batasi koordinat X agar tidak menembus dinding luar hitam map (Min: 40, Max: MAP_W - 60)
            int currentMaxW = (mapId == 3) ? WORLD_W : MAP_W;
            spawnX = Math.max(40, Math.min(spawnX, currentMaxW - 60));

            soldiers.add(new Soldier(spawnX, spawnY, tier));
        }
    }

    private void spawnMiniBosses(int count) {
        miniBosses.clear();
        float baseX, baseY0, baseY1;

        if (finishArea != null) {
            baseX = finishArea.x - 200;
            // Map 3: kunci di posisi bawah
            baseY0 = 460f;
            baseY1 = 460f;
        } else {
            baseX = MAP_W * 0.65f;
            // Map 1 & 2: kunci di posisi bawah
            baseY0 = 460f;
            baseY1 = 460f;
        }

        float[] xs = {baseX, baseX + 120};
        float[] ys = {baseY0, baseY1};

        for (int i = 0; i < count; i++) {
            miniBosses.add(
                    new MiniBoss(
                            xs[i % xs.length],
                            ys[i % ys.length]
                    )
            );
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
        if (mapId == 1 && remainingSoldiersToSpawn == 0 && soldiers.stream().noneMatch(s -> s.isAlive()) && !miniBossSpawned) {
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
            // Tambahkan ini! Panggil update untuk map 1, 2, 4
            player.update(mousePos.x, mousePos.y, MAP_W, MAP_H);
        }
        player.updateJump();

        // MAP 4 PHASE SYSTEM
        if (mapId == 4) {
            if (!finalBossAppeared && soldiers.stream().noneMatch(s -> s.isAlive())) {
                finalBoss = new FinalBoss(MAP_W / 2f, 120);
                finalBossAppeared = true;
                showNotif("FINAL BOSS MUNCUL!", 180);
            }
            if (finalBoss != null && finalBoss.isAlive()) {
                bossSpawnCooldown--;
                if (bossSpawnCooldown <= 0) {

                    int spawnAtGroundY = 460;
                    soldiers.add(new Soldier(
                            (int) (finalBoss.getX() + new Random().nextInt(200) - 100),
                            spawnAtGroundY,
                            2
                    ));
                    showNotif("FINAL BOSS MEMANGGIL PASUKAN!", 90);
                    bossSpawnCooldown = 300;
                }
            }
        }

        // Update para Soldier & Hit Player
        for (Soldier s : soldiers) {
            if (s.isAlive()) {
                s.update(player.getX(), player.getY(), MAP_W, MAP_H);

                if (s.canAttack(player.getX(), player.getY())) {
                    int dmg = s.doAttack();
                    player.hit(dmg);

                    float randomX = player.getX() + (float) (Math.random() * 60 - 30);
                    float fixedY = player.getY() - 30;
                    spawnDamageText(randomX, fixedY, dmg, Color.RED);
                }
            }
        }

        // Update para Mini Boss & Hit Player
        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive()) {
                mb.update(player.getX(), player.getY(), MAP_W, MAP_H);

                // 1. HITUNG SELISIH VERTIKAL (Y) ANTARA MINI BOSS DAN PLAYER SECARA LANGSUNG
                float selisihY = Math.abs(mb.getY() - player.getY());

                // 2. TAMBAHKAN SYARAT: selisihY harus kurang dari 40 piksel (artinya harus sejajar di tanah)
                if (mb.canAttack(player.getX(), player.getY()) && selisihY < 40) {
                    int dmg = mb.doAttack();
                    player.hit(dmg);

                    float randomX = player.getX() + (float) (Math.random() * 50 - 25);
                    float randomY = (player.getY() - 30) + (float) (Math.random() * 20 - 10);
                    spawnDamageText(randomX, randomY, dmg, new Color(220, 100, 0));
                }
            }
        }

        // Update Final Boss & Hit Player
        if (finalBoss != null && finalBoss.isAlive()) {
            finalBoss.update(player.getX(), player.getY(), MAP_W, MAP_H);

            if (finalBoss.canAttack(player.getX(), player.getY())) {
                player.hit(finalBoss.doAttack());
            }
            if (finalBoss.canSpecial()) {
                float dist = dist(player.getX(), player.getY(), finalBoss.getX(), finalBoss.getY());
                if (dist < 120) {
                    player.hit(finalBoss.doSpecial());
                    spawnDamageText(player.getX(), player.getY() - 30, finalBoss.attack * 2, new Color(200, 0, 200));
                    showNotif("SERANGAN KHUSUS!", 60);
                }
            }
        }

        // Pisahkan posisi antar musuh agar tidak menumpuk
        applyEnemySteeringAndSeparation();

        // [DI SINI SEBELUMNYA ADA LOOP DUPLIKAT YANG SUDAH DIHAPUS]
        // Map 2: Kondisi Survival
        if (mapId == 2) {
            if (survivalTicks < SURVIVAL_TARGET) {
                survivalTicks++;
                spawnCooldown--;
                if (spawnCooldown <= 0) {
                    // Hitung berapa jumlah prajurit yang saat ini masih hidup di map
                    long jumlahSoldierHidup = soldiers.stream().filter(Soldier::isAlive).count();
                    int maksimalSoldier = 8; // <-- SIlakan ganti angka ini sesuai selera tingkat kesulitanmu

                    // Hanya spawn jika jumlah musuh di layar masih di bawah batas maksimal
                    if (jumlahSoldierHidup < maksimalSoldier) {
                        spawnSoldiers(2, survivalTicks > 500 ? 2 : 1);
                    }

                    // Cooldown tetap direset agar tidak terjadi sisa lag timer
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
                // Menang kalau mini boss mati
                boolean bossDead = miniBosses.stream().noneMatch(mb -> mb.isAlive());
                if (!miniBosses.isEmpty() && bossDead) {
                    endBattle(true);
                }
                break;

            case 2:
                // Menang JIKA waktu sudah habis, Mini Boss sudah muncul, dan semua Mini Boss sudah mati
                if (survivalTicks >= SURVIVAL_TARGET && !miniBosses.isEmpty()) {
                    boolean allBossesDead = miniBosses.stream().noneMatch(mb -> mb.isAlive());
                    if (allBossesDead) {
                        endBattle(true); // Pemicu Kemenangan!
                    }
                }
                break;

            case 3:

                if (miniBossSpawned && miniBosses.stream().noneMatch(mb -> mb.isAlive())) {
                    endBattle(true); // ← langsung menang tanpa perlu ke ujung
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

        // Reward gold on win
        if (won) {
            PlayerData pd = GameEngine.getInstance().getCurrentPlayer();
            int reward = 50 + mapId * 30;
            pd.setGold(pd.getGold() + reward);
            SaveManager.savePlayer(pd);
        }
        repaint();
    }

    // ===== Attack (mouse click) =====
    private void doPlayerAttack() {
        if (!player.isAlive() || state != State.PLAYING) {
            return;
        }
        if (!player.canAttack()) {
            return;
        }
        int dmg = player.doAttack();
        float px = player.getX(), py = player.getY();
        float range = 65f;

        for (Soldier s : soldiers) {

            if (s.isAlive() && dist(px, py, s.getX(), s.getY()) < range) {
                s.takeDamage(dmg);
                spawnDamageText(
                        s.getX(),
                        s.getY() - 20,
                        dmg,
                        COL_GOLD
                );
            }
        }

        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive() && dist(px, py, mb.getX(), mb.getY()) < range) {
                mb.takeDamage(dmg);
                spawnDamageText(
                        mb.getX(),
                        mb.getY() - 20,
                        dmg,
                        COL_GOLD
                );
            }
        }
        if (finalBoss != null && finalBoss.isAlive() && dist(px, py, finalBoss.getX(), finalBoss.getY()) < range + 20) {
            finalBoss.takeDamage(dmg);
            spawnDamageText(finalBoss.getX(), finalBoss.getY() - 40, dmg, COL_GOLD);
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

        // AoE untuk Soldier 
        soldiers.stream().filter(s -> s.isAlive() && dist(px, py, s.getX(), s.getY()) < range)
                .forEach(s -> {
                    s.takeDamage(dmg);
                    spawnDamageText(s.getX(), s.getY() - 20, dmg, COL_GOLD_LIGHT);
                });

        // AoE untuk Mini Boss
        miniBosses.stream().filter(m -> m.isAlive() && dist(px, py, m.getX(), m.getY()) < range)
                .forEach(m -> {
                    m.takeDamage(dmg);
                    spawnDamageText(m.getX(), m.getY() - 20, dmg, COL_GOLD_LIGHT);
                });

        // AoE untuk Final Boss ( FIX SINKRONISASI DAMAGE )
        if (finalBoss != null && finalBoss.isAlive() && dist(px, py, finalBoss.getX(), finalBoss.getY()) < range) {
            finalBoss.takeDamage(dmg);
            // Diganti jadi 'dmg' biasa tanpa bagi 3 agar teks sesuai dengan darah boss yang berkurang
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

        // ===== WORLD LAYER =====
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
            // Foreground layer (digambar SETELAH player agar terlihat di depan)
            if (mapId == 2 && anara.utils.AssetManager.map2Foreground != null) {
                int bW = 180, bH = 120;
                // Batu kiri bawah
                world.drawImage(anara.utils.AssetManager.map2Foreground,
                        -20, MAP_H - 200, bW, bH, null);
                // Batu kanan bawah
                world.drawImage(anara.utils.AssetManager.map2Foreground,
                        MAP_W - 160, MAP_H - 200, bW, bH, null);
                // Batu kiri atas (platform)
                world.drawImage(anara.utils.AssetManager.map2Foreground,
                        -20, MAP_H - 380, bW, bH, null);
                // Batu kanan atas (platform)
                world.drawImage(anara.utils.AssetManager.map2Foreground,
                        MAP_W - 160, MAP_H - 380, bW, bH, null);
            }
        }

        world.dispose();

        // ===== UI LAYER =====
        for (DamageText d : damageTexts) {
            d.draw(g2);
        }

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
            g2.drawImage(anara.utils.AssetManager.map1Background,
                    0, 0, currentWidth, MAP_H, null);
        } else if (mapId == 2 && anara.utils.AssetManager.map2Background != null) {
            g2.drawImage(anara.utils.AssetManager.map2Background,
                    0, 0, MAP_W, MAP_H, null);
        } else if (mapId == 3 && anara.utils.AssetManager.map3Background != null) {
            int imgW = anara.utils.AssetManager.map3Background.getWidth();
            for (int x = 0; x < WORLD_W; x += imgW) {
                g2.drawImage(anara.utils.AssetManager.map3Background,
                        x, 0, imgW, MAP_H, null);
            }

        } else if (mapId == 4 && anara.utils.AssetManager.map4Background != null) { // ← tambah ini
            g2.drawImage(anara.utils.AssetManager.map4Background,
                    0, 0, currentWidth, MAP_H, null);

        } else {
            // Fallback warna solid (map 2 masuk sini)
            Color groundColor;
            switch (mapId) {
                case 1:
                    groundColor = new Color(25, 35, 20);
                    break;
                case 2:
                    groundColor = new Color(30, 20, 15);
                    break; // ← map 2 fallback merah gelap
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

        // Border & map name tetap
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
        // Kunci tinggi area HUD hitam
        int fixedHudH = 90;
        int startY = getHeight() - fixedHudH;
//
//        // --- PIJAKAN / GROUND BAWAH ---
//        int groundHeight = 80;
//        int groundY = startY - groundHeight;
//
//        g2.setColor(new Color(45, 40, 35));
//        g2.fillRect(0, groundY, MAP_W, groundHeight);
//
//        g2.setColor(new Color(75, 65, 55));
//        g2.setStroke(new BasicStroke(2f));
//        g2.drawLine(0, groundY, MAP_W, groundY);

        // --- BACKGROUND HUD HITAM ---
        g2.setColor(new Color(8, 6, 14));
        g2.fillRect(0, startY, MAP_W, fixedHudH);

        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, startY, MAP_W, startY);

        if (player == null) {
            return;
        }

        // --- PLAYER HP ---
        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("HP", 15, startY + 20);
        drawHPBar(g2, 40, startY + 8, 200, 12, player.getHpRatio(), player.hp + "/" + player.maxHp);

        // --- ATTACK COOLDOWN ---
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

        // --- SKILL COOLDOWN ---
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

        // --- ENEMY COUNT ---
        long alive = soldiers.stream().filter(Soldier::isAlive).count();
        long aliveM = miniBosses.stream().filter(MiniBoss::isAlive).count();
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.setColor(COL_RED_LIGHT);
        g2.drawString("Musuh: " + alive + " prajurit  |  " + aliveM + " mini boss", 270, startY + 22);

        // --- MAP 2 TIMER ---
        if (mapId == 2) {
            int secLeft = Math.max(0, (SURVIVAL_TARGET - survivalTicks) / 60);
            g2.setFont(new Font("Serif", Font.BOLD, 18));
            if (secLeft > 0) {
                g2.setColor(secLeft < 5 ? COL_RED_LIGHT : COL_GOLD);
                g2.drawString("WAKTU: " + secLeft + "s", 270, startY + 46);
            } else {
                g2.setColor(COL_RED_LIGHT);
                g2.drawString("PERINGATAN: KALAHKAN MINI BOSS!", 270, startY + 46);
            }
        }

        // --- BOSS HP (DIPINDAH KE ATAS BOSS) ---
        if (finalBoss != null && finalBoss.isAlive()) {
            int barW = 80; // Lebar bar HP (bisa kamu perbesar kalau kurang panjang)
            int barH = 8;  // Tinggi bar HP

            // Asumsi posisi boss menggunakan variabel .x dan .y
            int bx = (int) finalBoss.getX() - (barW / 2);
            int by = (int) finalBoss.getY() - 25;

            // Gambar HP tanpa memunculkan teks angka (agar terlihat rapi mirip musuh biasa)
            drawHPBar(g2, bx, by, barW, barH, finalBoss.getHpRatio(), "");

            g2.setFont(new Font("Serif", Font.BOLD, 10));
            if (finalBoss.isEnraged()) {
                g2.setColor(new Color(220, 100, 220));
                g2.drawString("⚠ MENGAMUK", bx, by - 5);
            } else {
                g2.setColor(new Color(200, 0, 200));
                g2.drawString("FINAL BOSS", bx + 10, by - 5);
            }
        }

        // --- CONTROLS HINT ---
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(140, 130, 110));
        g2.drawString("WASD: Gerak  |  ESC: Menu", MAP_W - 180, startY + 22);
    }

    private void drawEndOverlay(Graphics2D g2, boolean won) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, MAP_W, MAP_H);

        int cx = MAP_W / 2, cy = MAP_H / 2;
        String title = won ? "KEMENANGAN!" : "KALAH...";
        Color titleColor = won ? COL_GOLD : COL_RED_LIGHT;

        drawPanel(g2, cx - 220, cy - 100, 440, 200);

        g2.setFont(new Font("Serif", Font.BOLD, 36));
        g2.setColor(titleColor);
        g2.drawString(title, cx - g2.getFontMetrics().stringWidth(title) / 2, cy - 40);

        if (won) {
            int reward = 50 + mapId * 30;
            g2.setFont(new Font("Serif", Font.PLAIN, 16));
            g2.setColor(COL_GOLD);
            g2.drawString("+" + reward + " Koin", cx - 40, cy - 10);
        }

        // Buttons
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
        // Interface lokal untuk menyatukan akses posisi tanpa mengubah class asli musuh
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
                    public float getX() {
                        return s.getX();
                    }

                    public float getY() {
                        return s.getY();
                    }

                    public void setX(float x) {
                        s.setX(x);
                    }

                    public void setY(float y) {
                        s.setY(y);
                    }

                    public float getRadius() {
                        return 22f;
                    }
                });
            }
        }

        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive()) {
                activeEnemies.add(new GameEntity() {
                    public float getX() {
                        return mb.getX();
                    }

                    public float getY() {
                        return mb.getY();
                    }

                    public void setX(float x) {
                        mb.setX(x);
                    }

                    public void setY(float y) {
                        mb.setY(y);
                    }

                    public float getRadius() {
                        return 35f;
                    }
                });
            }
        }
        if (finalBoss != null && finalBoss.isAlive()) {
            activeEnemies.add(new GameEntity() {
                public float getX() {
                    return finalBoss.getX();
                }

                public float getY() {
                    return finalBoss.getY();
                }

                public void setX(float x) {
                    finalBoss.setX(x);
                }

                public void setY(float y) {
                    finalBoss.setY(y);
                }

                public float getRadius() {
                    return 55f;
                }
            });
        }

        // ===== TAHAP 1: AMANKAN TINGGI TANAH (LOGIKA DORONG PLAYER DIHAPUS) =====
        for (GameEntity e : activeEnemies) {
            // Mengunci posisi Y musuh agar selalu menapak tanah kaku,
            // tetapi membebaskan posisi X agar musuh bisa menumpuk masuk ke tubuh Player tanpa mendorongnya.
            float groundY = 460f;
            e.setY(groundY);
        }

        // ===== TAHAP 2: SEPARATION (SESAMA MUSUH TETAP SALING BERJEJER) =====
        for (int i = 0; i < activeEnemies.size(); i++) {
            GameEntity e1 = activeEnemies.get(i);
            for (int j = i + 1; j < activeEnemies.size(); j++) {
                GameEntity e2 = activeEnemies.get(j);

                float dx = e1.getX() - e2.getX();
                float dist = (float) Math.abs(dx);

                // Batas jarak minimal antar musuh agar tidak bertumpuk
                float minDist = e1.getRadius() + e2.getRadius() + 10f;

                if (dist < minDist) {
                    float overlap = minDist - dist;
                    // Jika koordinatnya persis sama, beri dorongan acak sedikit agar tidak stuck
                    if (dx == 0) {
                        dx = (Math.random() < 0.5) ? 1f : -1f;
                    }

                    float pushX = (dx / Math.abs(dx)) * (overlap / 2f);

                    // Dorong kiri-kanan antar sesama musuh secara halus
                    e1.setX(e1.getX() + pushX);
                    e2.setX(e2.getX() - pushX);
                }
            }
        }
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

                    // JUMP
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
                startBattle(); // retry
            }
        } else if (btn2.contains(mx, my)) {
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        }
    }

    // ===========================
    // HELPERS
    // ===========================
    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1, dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void spawnDamageText(float x, float y, int dmg, Color color) {
        damageTexts.add(new DamageText(x, y, dmg, color));
    }

    private void showNotif(String text, int duration) {
        notifText = text;
        notifTimer = duration;
    }

    // ===========================
    // DAMAGE TEXT
    // ===========================
    static class DamageText {

        float x, y;
        int dmg, life = 45;
        Color color;
        float vy = -1.5f;

        DamageText(float x, float y, int dmg, Color color) {
            this.x = x;
            this.y = y;
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
