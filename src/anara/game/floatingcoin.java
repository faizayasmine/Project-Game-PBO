package anara.game;

import java.awt.*;

public class floatingcoin {
    public float x, y;
    private float baseY;       // posisi Y asli (untuk animasi bob)
    private float bobPhase;    // fase animasi naik-turun
    private boolean collected = false;

    // Reward saat diambil
    public final int goldReward;
    public final int healReward;

    // Efek collect — partikel kecil
    private int collectTimer = 0;
    private static final int COLLECT_ANIM = 30;

    public floatingcoin(float x, float y) {
        this.x          = x;
        this.baseY      = y;
        this.y          = y;
        this.bobPhase   = (float)(Math.random() * Math.PI * 2); // fase acak biar tidak sync
        this.goldReward = 5 + (int)(Math.random() * 11);  // 5–15 gold
        this.healReward = 10 + (int)(Math.random() * 11); // 10–20 HP
    }

    public boolean isCollected() { return collected && collectTimer <= 0; }
    public boolean isActive()    { return !collected; }

    public void collect() {
        collected    = true;
        collectTimer = COLLECT_ANIM;
    }

    public void update() {
        if (!collected) {
            // Animasi bob naik-turun pelan
            bobPhase += 0.05f;
            y = baseY + (float)(Math.sin(bobPhase) * 5);
        } else {
            if (collectTimer > 0) collectTimer--;
        }
    }

    /**
     * Cek apakah player menyentuh koin ini.
     * radius sentuh = 25px
     */
    public boolean touches(float px, float py) {
        float dx = px - x;
        float dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) < 25;
    }

    public void draw(Graphics2D g2) {
        if (isCollected()) return;

        Graphics2D p = (Graphics2D) g2.create();
        int cx = (int) x, cy = (int) y;

        if (!collected) {
            // --- Aura glow kuning ---
            float glow = (float)(0.5 + 0.5 * Math.sin(bobPhase * 2));
            p.setColor(new Color(255, 220, 50, (int)(glow * 60)));
            p.fillOval(cx - 18, cy - 18, 36, 36);

            // --- Badan koin ---
            p.setColor(new Color(255, 200, 30));
            p.fillOval(cx - 10, cy - 10, 20, 20);

            // --- Highlight ---
            p.setColor(new Color(255, 240, 120));
            p.fillOval(cx - 6, cy - 7, 6, 6);

            // --- Border ---
            p.setColor(new Color(180, 130, 0));
            p.setStroke(new BasicStroke(1.5f));
            p.drawOval(cx - 10, cy - 10, 20, 20);

            // --- Tanda "$" di tengah ---
            p.setFont(new Font("Serif", Font.BOLD, 10));
            p.setColor(new Color(140, 90, 0));
            p.drawString("$", cx - 4, cy + 4);

            // --- Label reward kecil di atas koin ---
            p.setFont(new Font("SansSerif", Font.BOLD, 9));
            p.setColor(new Color(255, 230, 80));
            String label = "+" + goldReward + "g +" + healReward + "hp";
            int lw = p.getFontMetrics().stringWidth(label);
            p.drawString(label, cx - lw / 2, cy - 14);

        } else if (collectTimer > 0) {
            // --- Animasi collect: teks melayang ke atas ---
            float alpha = collectTimer / (float) COLLECT_ANIM;
            int offsetY = (int)((1f - alpha) * 30);

            p.setFont(new Font("Serif", Font.BOLD, 13));
            p.setColor(new Color(255, 220, 50, (int)(alpha * 255)));
            String txt = "+" + goldReward + " Gold";
            p.drawString(txt, cx - p.getFontMetrics().stringWidth(txt) / 2, cy - 20 - offsetY);

            p.setColor(new Color(80, 220, 120, (int)(alpha * 255)));
            String txt2 = "+" + healReward + " HP";
            p.drawString(txt2, cx - p.getFontMetrics().stringWidth(txt2) / 2, cy - 5 - offsetY);
        }

        p.dispose();
    }
}