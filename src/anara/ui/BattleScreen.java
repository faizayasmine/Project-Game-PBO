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
    private List<Soldier> soldiers = new ArrayList<>();
    private List<MiniBoss> miniBosses = new ArrayList<>();
    private FinalBoss finalBoss;

    // ===== Map 2 timer =====
    private int survivalTicks = 0;
    private static final int SURVIVAL_TARGET = 15 * 60; // 15 sec * 60 fps
    private int spawnCooldown = 0;

    // ===== MAP 3 =====
    // World map lebih panjang dari layar
    private static final int WORLD_W = 3000;
    private int cameraX = 0;
    private Rectangle finishArea;
    private boolean miniBossSpawned = false;
    private boolean reachedFinish = false;

    // ===== Input =====
    private boolean keyUp, keyDown, keyLeft, keyRight;
    private Point mousePos = new Point(450, 300);

    // ===== Game loop =====
    private javax.swing.Timer gameLoop;
    private int gameTick = 0;

    // ===== HUD =====
    private List<DamageText> damageTexts = new ArrayList<>();
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

player = new Player(MAP_W / 2f, MAP_H / 2f);
player.setExternalBonuses(
    pd.getTotalAttackBonus(),
    pd.getTotalDefenseBonus()
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
                // 5 soldiers + 1 mini boss
                spawnSoldiers(5, 1);
                // Mini boss akan muncul via checkWinLose() setelah semua soldier mati
                break;
            case 2:
                // Endless spawn, survival 15 sec
                spawnSoldiers(4, 1);
                spawnMiniBosses(1);
                showNotif("BERTAHAN 15 DETIK!", 150);
                break;
            case 3:

                player.setX(80);
                player.setY(MAP_H / 2f);

                // finish dulu
                finishArea = new Rectangle(
                        WORLD_W - 120,
                        MAP_H / 2 - 60,
                        80,
                        120
                );

                miniBossSpawned = false;
                reachedFinish = false;

                showNotif("CAPAI GARIS AKHIR!", 150);
                break;

            case 4:
                // Final Boss — mulai dengan pasukan penjaga
                spawnSoldiers(6, 2);
                finalBoss = null;
                finalBossAppeared = false;
                // BUG FIX #3 (bagian initMap): Pastikan cooldown boss spawn dimulai dari 300
                bossSpawnCooldown = 300;
                showNotif("FINAL BOSS — HADAPI TAKDIRMU!", 180);
                break;
        }
    }

    private void spawnSoldiers(int count, int tier) {

    for (int i = 0; i < count; i++) {

        float sx = 200 + (i * 80);
        float sy = 120;

        soldiers.add(new Soldier(sx, sy, tier));
    }
}

    // BUG FIX #1: spawnMiniBosses() sebelumnya selalu return dini karena
    // guard "if (finishArea == null) return;" — padahal finishArea hanya diset
    // di Map 3, sehingga mini boss TIDAK PERNAH muncul di Map 1 dan Map 2.
    // Fix: gunakan posisi fallback berbasis MAP_W/MAP_H jika finishArea null.
    private void spawnMiniBosses(int count) {
        miniBosses.clear();

        float baseX, baseY0, baseY1;

        if (finishArea != null) {
            // Map 3: spawn di depan garis finish
            baseX  = finishArea.x - 200;
            baseY0 = 180;
            baseY1 = MAP_H - 180;
        } else {
            // Map 1 & Map 2: spawn di tengah-kanan arena
            baseX  = MAP_W * 0.65f;
            baseY0 = MAP_H * 0.30f;
            baseY1 = MAP_H * 0.70f;
        }

        float[] xs = { baseX, baseX + 120 };
        float[] ys = { baseY0, baseY1 };

        for (int i = 0; i < count; i++) {
            miniBosses.add(
                    new MiniBoss(
                            xs[i % xs.length],
                            ys[i % ys.length]
                    )
            );
        }

        miniBossSpawned = true;
        showNotif("MINI BOSS MUNCUL!", 120);
    }

    // ===========================
    // GAME LOOP TICK
    // ===========================
    private void tick() {
        if (state != State.PLAYING) {return;
        }
       float moveX = 0;
float moveY = 0;

if (keyUp) moveY -= 1;
if (keyDown) moveY += 1;
if (keyLeft) moveX -= 1;
if (keyRight) moveX += 1;

float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);

if (len > 0) {
    moveX = (moveX / len) * 3.5f;
    moveY = (moveY / len) * 3.5f;

    player.addMovement(moveX, moveY);
}
if (mapId == 3) {
        player.update(mousePos.x, mousePos.y, WORLD_W, MAP_H);
    } else {
        player.update(mousePos.x, mousePos.y, MAP_W, MAP_H);
repaint ();
        
}
        // MAP 3 : spawn musuh saat berjalan
        if (mapId == 3) {
            spawnCooldown--;
            // Kamera mengikuti player
            cameraX = (int) (player.getX() - MAP_W / 2);

// batas kamera
            cameraX = Math.max(
                    0,
                    Math.min(cameraX, WORLD_W - MAP_W)
            );

            if (spawnCooldown <= 0) {
                float x = player.getX() + 300;

                if (x < WORLD_W - 150) {
                    soldiers.add(new Soldier(
                            x,
                            100 + new Random().nextInt(300),
                            1
                    ));
                }

                spawnCooldown = 120; // tiap ±2 detik
            }

            // Sampai finish → munculkan 2 mini boss
            // BUG FIX #4: Sebelumnya hanya spawn 1 MiniBoss, padahal nama map
            // dan deskripsi menyebut "DUA MINI BOSS". Sekarang spawn 2.
            if (player.getX() >= WORLD_W - 200
                    && !miniBossSpawned) {

                miniBosses.clear();

                miniBosses.add(
                        new MiniBoss(
                                WORLD_W - 100,
                                MAP_H / 2f - 80   // Mini Boss pertama, atas tengah
                        )
                );

                miniBosses.add(
                        new MiniBoss(
                                WORLD_W - 100,
                                MAP_H / 2f + 80   // Mini Boss kedua, bawah tengah
                        )
                );

                miniBossSpawned = true;

                showNotif("2 MINI BOSS MUNCUL!", 120);
            }
        }
        // ===== MAP 4 PHASE SYSTEM =====
if (mapId == 4) {

    // kalau semua monster awal mati
   if (!finalBossAppeared &&
    soldiers.stream().noneMatch(s -> s.isAlive())) {

    finalBoss = new FinalBoss(MAP_W / 2f, 120);

    finalBossAppeared = true;

    showNotif("FINAL BOSS MUNCUL!", 180);
}

    // boss summon monster perlahan
    if (finalBoss != null && finalBoss.isAlive()) {

        bossSpawnCooldown--;

        if (bossSpawnCooldown <= 0) {

            soldiers.add(
                new Soldier(
                    finalBoss.getX() + new Random().nextInt(200) - 100,
                    finalBoss.getY() + new Random().nextInt(200) - 100,
                    2
                )
            );

            showNotif("FINAL BOSS MEMANGGIL PASUKAN!", 90);

            bossSpawnCooldown = 300; // 5 detik
        }
    }
}

        // Update enemies
        for (Soldier s : soldiers) {
            if (s.isAlive()) {

                if (mapId == 3) {
                    s.update(player.getX(), player.getY(), WORLD_W, MAP_H);
                } else {
                    s.update(player.getX(), player.getY(), MAP_W, MAP_H);
                }
            }
        }

        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive()) {

                if (mapId == 3) {
                    mb.update(player.getX(), player.getY(), WORLD_W, MAP_H);
                } else {
                    mb.update(player.getX(), player.getY(), MAP_W, MAP_H);
                }
            }
        }
        if (finalBoss != null && finalBoss.isAlive()) {
            finalBoss.update(player.getX(), player.getY(), MAP_W, MAP_H);
        }

        // Enemy attacks player
        for (Soldier s : soldiers) {
            if (s.isAlive() && s.canAttack(player.getX(), player.getY())) {
                int dmg = s.doAttack();
                player.hit(dmg);
                spawnDamageText(player.getX(), player.getY() - 30, dmg, Color.RED);
            }
        }
        for (MiniBoss mb : miniBosses) {
            if (mb.isAlive() && mb.canAttack(player.getX(), player.getY())) {
                int dmg = mb.doAttack();
                player.hit(dmg);
                spawnDamageText(player.getX(), player.getY() - 30, dmg, new Color(220, 100, 0));
            }
        }
        if (finalBoss != null && finalBoss.isAlive()) {
            if (finalBoss.canAttack(player.getX(), player.getY())) {
                player.hit(finalBoss.doAttack());
            }
            if (finalBoss.canSpecial()) {
                // AoE - hit if within 120px
                float dist = dist(player.getX(), player.getY(), finalBoss.getX(), finalBoss.getY());
                if (dist < 120) {
                    player.hit(finalBoss.doSpecial());
                    spawnDamageText(player.getX(), player.getY() - 30, finalBoss.attack * 2, new Color(200, 0, 200));
                    showNotif("SERANGAN KHUSUS!", 60);
                }
            }
        }

        // Map 2: spawn more soldiers
        if (mapId == 2) {
            survivalTicks++;
            spawnCooldown--;
            if (spawnCooldown <= 0) {
                spawnSoldiers(2, survivalTicks > 500 ? 2 : 1);
                spawnCooldown = 120 - Math.min(90, survivalTicks / 20);
            }
        }

        // Update damage texts
        damageTexts.removeIf(d -> d.life <= 0);
        for (DamageText d : damageTexts) {
            d.update();
        }

        // Notif timer
        if (notifTimer > 0) {
            notifTimer--;
        }

        // Win/Lose check
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
                // Semua soldier mati → spawn mini boss
                if (soldiers.stream().noneMatch(s -> s.isAlive()) && miniBosses.isEmpty()) {
                    spawnMiniBosses(1);
                }

                // Menang kalau mini boss mati
                boolean bossDead = miniBosses.stream().noneMatch(mb -> mb.isAlive());
                if (!miniBosses.isEmpty() && bossDead) {
                    endBattle(true);
                }
                break;

            case 2:
                if (survivalTicks >= SURVIVAL_TARGET) {
                    endBattle(true);
                }
                break;

            case 3:
                boolean miniBossDead
                        = miniBosses.stream()
                                .noneMatch(mb -> mb.isAlive());

                if (miniBossDead && player.getX() >= WORLD_W - 150) {
                    endBattle(true);
                }
                break;

            // BUG FIX #2: Case 4 sebelumnya TIDAK memiliki kondisi apapun —
            // seluruh isi case langsung dieksekusi setiap tick (±60x/detik),
            // yang menyebabkan: posisi player direset, 6 soldier baru di-spawn,
            // finalBoss dihapus, dan flag direset — setiap frame.
            // Map 4 jadi mustahil dimainkan. Fix: tambahkan kondisi yang benar:
            // menang jika final boss sudah muncul dan sudah mati.
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

        // Melee: hit nearest enemy in range
        float range = 45f;//range attack lebih kecil
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

        // AoE
        soldiers.stream().filter(s -> s.isAlive() && dist(px, py, s.getX(), s.getY()) < range)
                .forEach(s -> {
                    s.takeDamage(dmg);
                    spawnDamageText(s.getX(), s.getY() - 20, dmg, COL_GOLD_LIGHT);
                });
        miniBosses.stream().filter(m -> m.isAlive() && dist(px, py, m.getX(), m.getY()) < range)
                .forEach(m -> {
                    m.takeDamage(dmg / 2);
                    spawnDamageText(m.getX(), m.getY() - 20, dmg / 2, COL_GOLD_LIGHT);
                });
        if (finalBoss != null && finalBoss.isAlive() && dist(px, py, finalBoss.getX(), finalBoss.getY()) < range) {
            finalBoss.takeDamage(dmg / 3);
            spawnDamageText(finalBoss.getX(), finalBoss.getY() - 40, dmg / 3, COL_GOLD_LIGHT);
        }
    }

    // ===========================
    // RENDER
    // ===========================
    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();

    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
    );

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
    }

    // finish area
    if (mapId == 3 && !miniBossSpawned && finishArea != null) {

        world.setColor(new Color(255, 215, 0, 120));
        world.fillRect(
            finishArea.x,
            finishArea.y,
            finishArea.width,
            finishArea.height
        );

        world.setColor(Color.YELLOW);
        world.drawRect(
            finishArea.x,
            finishArea.y,
            finishArea.width,
            finishArea.height
        );
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
        int currentWidth = (mapId == 3)
                ? WORLD_W
                : MAP_W;

        // Tilted dark ground
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

// Grid tiles
        g2.setColor(new Color(255, 255, 255, 10));

        for (int x = 0; x < currentWidth; x += 60) {
            g2.drawLine(x, 0, x, MAP_H);
        }

        for (int y = 0; y < MAP_H; y += 60) {
            g2.drawLine(0, y, currentWidth, y);
        }

        // Border walls
        g2.setColor(new Color(60, 50, 30));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(2, 2, MAP_W - 4, MAP_H - 4);
        g2.setColor(new Color(40, 32, 16));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(8, 8, MAP_W - 16, MAP_H - 16);

        // Map name badge
        g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 13));
        g2.setColor(new Color(100, 80, 40, 140));
        String[] mapNames = {"", "MAP I — PASUKAN PENJAGA", "MAP II — BERTAHAN HIDUP", "MAP III — DUA MINI BOSS", "MAP IV — FINAL BOSS"};
        g2.drawString(mapNames[mapId], 20, MAP_H - 12);
    }

    private void drawHUD(Graphics2D g2) {
        int hudY = MAP_H + 2;
        int hudH = getHeight() - MAP_H - 2;
        if (hudH < 1) {
            hudH = 65;
        }

        g2.setColor(new Color(8, 6, 14));
        g2.fillRect(0, MAP_H, MAP_W, hudH + 10);
        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, MAP_H, MAP_W, MAP_H);

        if (player == null) {
            return;
        }

        // Player HP
        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("HP", 15, MAP_H + 18);
        drawHPBar(g2, 40, MAP_H + 6, 200, 14, player.getHpRatio(), player.hp + "/" + player.maxHp);

        // Attack cooldown
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("ATK", 15, MAP_H + 36);
        int cdA = player.getAttackCooldownPct();
        g2.setColor(new Color(20, 16, 8));
        g2.fillRect(40, MAP_H + 24, 80, 8);
        g2.setColor(COL_GOLD);
        g2.fillRect(40, MAP_H + 24, (int) (80 * cdA / 100f), 8);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("[KLIK KIRI]", 125, MAP_H + 32);

        // Skill cooldown
        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("SKL", 15, MAP_H + 52);
        int cdS = player.getSkillCooldownPct();
        g2.setColor(new Color(10, 10, 25));
        g2.fillRect(40, MAP_H + 40, 80, 8);
        g2.setColor(new Color(80, 120, 200));
        g2.fillRect(40, MAP_H + 40, (int) (80 * cdS / 100f), 8);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(COL_TEXT_DIM);
        g2.drawString("[KLIK KANAN]", 125, MAP_H + 48);

        // Enemy count
        long alive = soldiers.stream().filter(Soldier::isAlive).count();
        long aliveM = miniBosses.stream().filter(MiniBoss::isAlive).count();
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.setColor(COL_RED_LIGHT);
        g2.drawString("Musuh: " + alive + " prajurit  |  " + aliveM + " mini boss", 270, MAP_H + 20);

        // Map 2 timer
        if (mapId == 2) {
            int secLeft = Math.max(0, (SURVIVAL_TARGET - survivalTicks) / 60);
            g2.setFont(new Font("Serif", Font.BOLD, 18));
            g2.setColor(secLeft < 5 ? COL_RED_LIGHT : COL_GOLD);
            g2.drawString("WAKTU: " + secLeft + "s", 270, MAP_H + 42);
        }

        // Boss HP
        if (finalBoss != null && finalBoss.isAlive()) {
            int bx = MAP_W / 2 - 150;
            g2.setFont(new Font("Serif", Font.BOLD, 11));
            g2.setColor(new Color(200, 0, 200));
            g2.drawString("FINAL BOSS", bx, MAP_H + 18);
            drawHPBar(g2, bx, MAP_H + 22, 300, 12, finalBoss.getHpRatio(), finalBoss.hp + "/" + finalBoss.maxHp);
            if (finalBoss.isEnraged()) {
                g2.setColor(new Color(220, 100, 220));
                g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 10));
                g2.drawString("⚠ MENGAMUK!", bx + 310, MAP_H + 33);
            }
        }

        // Controls hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(70, 60, 40));
        g2.drawString("WASD: Gerak  |  ESC: Menu", MAP_W - 200, MAP_H + 20);
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

    // ===========================
    // INPUT
    // ===========================
    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
       
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:
                        keyUp = true;
                        break;

                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:
                        keyDown = true;
                        break;
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                        keyLeft = true;
                        break;
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        keyRight = true;
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
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:
                        keyUp = false;
                        break;
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:
                        keyDown = false;
                        break;
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