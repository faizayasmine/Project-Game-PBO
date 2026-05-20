package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.utils.SaveManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

/**
 * LoginScreen - Layar login/register bergaya pixel RPG Layout mengikuti
 * referensi: field ID User, Username, Password + tombol Login Wireframe:
 * placeholder siap diganti asset gambar.
 *
 * Alur layar: StartScreen --> LoginScreen --> MainMenuScreen
 *
 * Mode: - LOGIN (default): pilih akun yang sudah ada via Username dropdown -
 * REGISTER: isi ID User + Username + Password baru
 *
 * Catatan integrasi SaveManager: - PlayerData perlu field: userId (String),
 * username (String), password (String) - SaveManager.loadPlayer(username,
 * password) untuk verifikasi - SaveManager.playerExists(username) untuk cek
 * duplikat
 */
public class LoginScreen extends BasePanel {

    // ── Warna pixel-RPG ───────────────────────────────────────────────────────
    private static final Color COL_GREEN_DARK = new Color(45, 85, 20);
    private static final Color COL_GREEN_MID = new Color(80, 145, 40);
    private static final Color COL_GREEN_LIGHT = new Color(160, 220, 80);
    private static final Color COL_LABEL_BG = new Color(60, 100, 25);
    private static final Color COL_FIELD_BG = new Color(15, 35, 8);
    private static final Color COL_TITLE = new Color(200, 240, 100);

    // ── Komponen form ─────────────────────────────────────────────────────────
    private JTextField fieldUserId;
    private JTextField fieldUsername;
    private JPasswordField fieldPassword;
    private JComboBox<String> comboExisting;
    private JButton btnAction;
    private JButton btnToggle;
    private JLabel statusLabel;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean isRegisterMode = false;

    // ── Animasi ───────────────────────────────────────────────────────────────
    private Timer animTimer;
    private float glowPhase = 0f;
    private float starPhase = 0f;
    private int[] starX, starY;
    private float[] starBright;

    // ── Posisi form (dihitung relatif terhadap panel form) ────────────────────
    // Panel form: x=55, y=100, w=320, h=360 (kiri)
    private static final int FORM_X = 55;
    private static final int FORM_Y = 90;
    private static final int FORM_W = 320;

    public LoginScreen() {
        setLayout(null);
        setOpaque(true);
        initStars();
        setupComponents();
        startAnimation();
        refreshPlayerList();
    }

    // ── Bintang acak ──────────────────────────────────────────────────────────
    private void initStars() {
        int n = 100;
        starX = new int[n];
        starY = new int[n];
        starBright = new float[n];
        java.util.Random rnd = new java.util.Random(99);
        for (int i = 0; i < n; i++) {
            starX[i] = rnd.nextInt(900);
            starY[i] = rnd.nextInt(250);
            starBright[i] = 0.3f + rnd.nextFloat() * 0.7f;
        }
    }

    // ── Setup komponen Swing ──────────────────────────────────────────────────
    private void setupComponents() {

        // ── Field: ID User ────────────────────────────────────────────────────
        fieldUserId = createTextField("Masukkan ID pengguna...");
        fieldUserId.setBounds(FORM_X, FORM_Y + 90, FORM_W, 36);
        add(fieldUserId);

        // ── Field: Username ───────────────────────────────────────────────────
        fieldUsername = createTextField("Masukkan Username pengguna...");
        fieldUsername.setBounds(FORM_X, FORM_Y + 170, FORM_W, 36);
        add(fieldUsername);

        // ── Combo existing players (login mode) ───────────────────────────────
        comboExisting = new JComboBox<>();
        comboExisting.setBounds(FORM_X, FORM_Y + 170, FORM_W, 36);
        styleCombo(comboExisting);
        comboExisting.setVisible(true); // visible di login mode
        add(comboExisting);

        // ── Field: Password ───────────────────────────────────────────────────
        fieldPassword = new JPasswordField();
        fieldPassword.setBounds(FORM_X, FORM_Y + 250, FORM_W, 36);
        stylePasswordField(fieldPassword, "Masukkan Password pengguna...");
        add(fieldPassword);

        // ── Tombol Login / Buat Akun ──────────────────────────────────────────
        btnAction = createPixelButton("Login", FORM_X + FORM_W / 2 - 75, FORM_Y + 305, 150, 42);
        btnAction.addActionListener(e -> doAction());
        add(btnAction);

        // ── Tombol toggle Register ────────────────────────────────────────────
        btnToggle = new JButton("Belum punya akun? Daftar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
                g2.setColor(getModel().isRollover()
                        ? COL_GREEN_LIGHT
                        : new Color(120, 180, 60));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnToggle.setBounds(FORM_X, FORM_Y + 355, FORM_W, 24);
        btnToggle.setOpaque(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> toggleMode());
        add(btnToggle);

        // ── Status / error label ──────────────────────────────────────────────
        statusLabel = new JLabel("", SwingConstants.LEFT);
        statusLabel.setBounds(FORM_X, FORM_Y + 385, FORM_W, 22);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(255, 80, 80));
        add(statusLabel);

        // Enter key di password field
        fieldPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doAction();
                }
            }
        });

        // Set awal: mode login
        updateFieldVisibility();
    }

    // ── Helpers gaya pixel-RPG ────────────────────────────────────────────────
    private JTextField createTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(COL_FIELD_BG);
        f.setForeground(COL_GREEN_LIGHT);
        f.setCaretColor(COL_GREEN_LIGHT);
        f.setFont(new Font("Monospaced", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_GREEN_MID, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private void stylePasswordField(JPasswordField f, String placeholder) {
        f.setBackground(COL_FIELD_BG);
        f.setForeground(COL_GREEN_LIGHT);
        f.setCaretColor(COL_GREEN_LIGHT);
        f.setFont(new Font("Monospaced", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_GREEN_MID, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        f.putClientProperty("JPasswordField.placeholderText", placeholder);
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(COL_FIELD_BG);
        cb.setForeground(COL_GREEN_LIGHT);
        cb.setFont(new Font("Monospaced", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(COL_GREEN_MID, 1));
    }

    private JButton createPixelButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hov = getModel().isRollover();
                boolean prs = getModel().isPressed();
                int dy = prs ? 2 : 0;

                // Tombol hijau pixel
                GradientPaint gp = new GradientPaint(
                        0, 0, hov ? new Color(120, 185, 55) : COL_GREEN_MID,
                        0, getHeight(), hov ? new Color(70, 125, 20) : COL_GREEN_DARK
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Border
                g2.setColor(COL_GREEN_LIGHT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);

                // Shadow bawah
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(2, getHeight() - 4, getWidth() - 4, 4, 4, 4);

                // Teks
                g2.setFont(new Font("Monospaced", Font.BOLD, 15));
                g2.setColor(new Color(230, 255, 180));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + dy);

                g2.dispose();
            }
        };
        btn.setBounds(x, y, w, h);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Toggle mode Login / Register ──────────────────────────────────────────
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        statusLabel.setText("");
        updateFieldVisibility();
        repaint();
    }

    private void updateFieldVisibility() {
        if (isRegisterMode) {
            // Register: tampilkan ID User + text Username + Password
            fieldUserId.setVisible(true);
            fieldUsername.setVisible(true);
            comboExisting.setVisible(false);
            btnAction.setText("Buat Akun");
            btnToggle.setText("Sudah punya akun? Login");
        } else {
            // Login: sembunyikan ID User, Username field -> combo
            fieldUserId.setVisible(false);
            fieldUsername.setVisible(false);
            comboExisting.setVisible(true);
            refreshPlayerList();
            btnAction.setText("Login");
            btnToggle.setText("Belum punya akun? Daftar");
        }
    }

    private void refreshPlayerList() {
        comboExisting.removeAllItems();
        List<String> players = SaveManager.getRegisteredPlayers();
        if (players.isEmpty()) {
            comboExisting.addItem("-- Belum ada akun --");
        } else {
            for (String p : players) {
                comboExisting.addItem(p);
            }
        }
    }

    // ── Aksi Login / Register ─────────────────────────────────────────────────
    private void doAction() {
        statusLabel.setForeground(new Color(255, 80, 80));

        if (isRegisterMode) {
            // ── Register baru ──────────────────────────────────────────────
            String userId = fieldUserId.getText().trim();
            String username = fieldUsername.getText().trim();
            String password = new String(fieldPassword.getPassword()).trim();

            if (userId.isEmpty()) {
                statusLabel.setText("ID pengguna tidak boleh kosong!");
                return;
            }
            if (username.isEmpty() || username.length() < 3) {
                statusLabel.setText("Username minimal 3 karakter!");
                return;
            }
            if (password.isEmpty() || password.length() < 4) {
                statusLabel.setText("Password minimal 4 karakter!");
                return;
            }
            if (SaveManager.playerExists(username)) {
                statusLabel.setText("Username sudah digunakan!");
                return;
            }

            // Buat player baru
            // Catatan: sesuaikan konstruktor PlayerData jika perlu tambah field userId & password
            PlayerData newPlayer = new PlayerData(username);
            newPlayer.setPassword(password);
            // newPlayer.setUserId(userId);       // uncomment jika PlayerData punya field ini
            // newPlayer.setPassword(password);   // uncomment jika PlayerData punya field ini
            SaveManager.savePlayer(newPlayer);
            GameEngine.getInstance().setCurrentPlayer(newPlayer);
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);

        } else {
            // ── Login existing ─────────────────────────────────────────────
            List<String> players = SaveManager.getRegisteredPlayers();
            if (players.isEmpty()) {
                statusLabel.setText("Belum ada akun. Silakan daftar!");
                return;
            }
            String selectedName = (String) comboExisting.getSelectedItem();
            if (selectedName == null || selectedName.startsWith("--")) {
                statusLabel.setText("Pilih akun terlebih dahulu!");
                return;
            }
            String password = new String(fieldPassword.getPassword()).trim();
            // Jika Password verification diaktifkan:
             if (!SaveManager.verifyPassword(selectedName, password)) {
                 statusLabel.setText("Password salah!");
                 return;
             }

            PlayerData player = SaveManager.loadPlayer(selectedName);
            if (player == null) {
                statusLabel.setText("Gagal memuat data akun!");
                return;
            }
            GameEngine.getInstance().setCurrentPlayer(player);
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        }
    }

    // ── Animasi timer ─────────────────────────────────────────────────────────
    private void startAnimation() {
        animTimer = new Timer(30, e -> {
            glowPhase += 0.05f;
            starPhase += 0.025f;
            if (glowPhase > Math.PI * 2) {
                glowPhase = 0;
            }
            if (starPhase > Math.PI * 2) {
                starPhase = 0;
            }
            repaint();
        });
        animTimer.start();
    }

    public void stopAnimation() {
        if (animTimer != null) {
            animTimer.stop();
        }
    }

    // ── Render utama ──────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // ── 1. BACKGROUND ─────────────────────────────────────────────────────
        // Saat bgImage tersedia, ganti dengan:
        //   g2.drawImage(bgImage, 0, 0, w, h, this);
        GradientPaint bgGrad = new GradientPaint(
                0, 0, new Color(18, 10, 35),
                0, h / 2, new Color(35, 20, 60),
                true
        );
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, w, h);

        // Ground
        g2.setPaint(new GradientPaint(0, h * 0.55f, new Color(15, 40, 15), 0, h, new Color(5, 15, 5)));
        g2.fillRect(0, (int) (h * 0.55f), w, h);
        drawHillAndCastle(g2, w, h);

        // ── 2. BINTANG ────────────────────────────────────────────────────────
        for (int i = 0; i < starX.length; i++) {
            float b = starBright[i] * (0.6f + 0.4f * (float) Math.sin(starPhase + i * 0.5f));
            g2.setColor(new Color(1f, 1f, 1f, b));
            g2.fillOval(starX[i], starY[i], (i % 4 == 0) ? 2 : 1, (i % 4 == 0) ? 2 : 1);
        }

        // ── 3. KARAKTER PLACEHOLDER (kanan) ───────────────────────────────────
        // Ganti dengan: g2.drawImage(charImg, w-260, h/2-160, 240, 320, this);
        drawCharacterPlaceholder(g2, w - 260, h / 2 - 160);

        // ── 4. LOGO TOP-RIGHT ─────────────────────────────────────────────────
        drawLogoTopRight(g2, w);

        // ── 5. JUDUL "LOGIN GAME" ─────────────────────────────────────────────
        float glow = (float) (0.6 + 0.4 * Math.sin(glowPhase));
        // Shadow
        g2.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2.setColor(new Color(0, 0, 0, 130));
        g2.drawString("LOGIN GAME", FORM_X + 2, FORM_Y - 12 + 2);
        // Glow layer
        g2.setColor(new Color(160, 220, 80, (int) (glow * 50)));
        g2.drawString("LOGIN GAME", FORM_X - 1, FORM_Y - 12 - 1);
        // Teks utama
        g2.setColor(COL_TITLE);
        g2.drawString("LOGIN GAME", FORM_X, FORM_Y - 12);

        // ── 6. LABEL FIELD ────────────────────────────────────────────────────
        if (isRegisterMode) {
            drawFieldLabel(g2, "ID User", FORM_X, FORM_Y + 55);
            drawFieldLabel(g2, "Username", FORM_X, FORM_Y + 135);
        } else {
            drawFieldLabel(g2, "Username", FORM_X, FORM_Y + 135);
        }
        drawFieldLabel(g2, "Password", FORM_X, FORM_Y + 215);

        g2.dispose();
    }

    /**
     * Label hijau di atas masing-masing field (seperti gambar referensi).
     */
    private void drawFieldLabel(Graphics2D g2, String text, int x, int y) {
        FontMetrics fm = g2.getFontMetrics(new Font("Monospaced", Font.BOLD, 13));
        int lw = fm.stringWidth(text) + 20;

        // Background kotak label
        g2.setColor(COL_LABEL_BG);
        g2.fillRoundRect(x, y, lw, 24, 4, 4);
        g2.setColor(COL_GREEN_LIGHT);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, lw, 24, 4, 4);

        // Teks label
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(COL_GREEN_LIGHT);
        g2.drawString(text, x + 10, y + 17);
    }

    private void drawHillAndCastle(Graphics2D g2, int w, int h) {
        // Bukit
        Polygon hill = new Polygon();
        hill.addPoint(0, h);
        hill.addPoint(0, (int) (h * 0.6f));
        hill.addPoint((int) (w * 0.2f), (int) (h * 0.45f));
        hill.addPoint((int) (w * 0.45f), (int) (h * 0.56f));
        hill.addPoint((int) (w * 0.6f), (int) (h * 0.5f));
        hill.addPoint(w, (int) (h * 0.55f));
        hill.addPoint(w, h);
        g2.setColor(new Color(10, 35, 10, 200));
        g2.fillPolygon(hill);

        // Kastil silhouette kanan
        int cx = w - 110, cy = (int) (h * 0.08f);
        g2.setColor(new Color(45, 25, 65, 180));
        g2.fillRect(cx - 18, cy + 40, 36, 90);
        int[] xp = {cx - 20, cx, cx + 20};
        int[] yp = {cy + 42, cy + 8, cy + 42};
        g2.fillPolygon(xp, yp, 3);
        g2.fillRect(cx - 50, cy + 60, 22, 70);
        g2.fillRect(cx - 72, cy + 100, 72, 30);
        g2.setColor(new Color(255, 220, 80, 50));
        g2.fillRect(cx - 8, cy + 58, 14, 20);
    }

    private void drawCharacterPlaceholder(Graphics2D g2, int x, int y) {
        // Ganti blok ini dengan: g2.drawImage(characterImage, x, y, 240, 320, this);
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRoundRect(x, y, 200, 300, 12, 12);
        g2.setColor(new Color(160, 220, 80, 80));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{5, 5}, 0));
        g2.drawRoundRect(x, y, 200, 300, 12, 12);

        // Bentuk orang sederhana
        g2.setColor(new Color(140, 200, 70, 70));
        g2.fillOval(x + 70, y + 20, 60, 60);
        g2.fillRoundRect(x + 65, y + 82, 70, 100, 10, 10);
        g2.fillRoundRect(x + 35, y + 90, 30, 80, 6, 6);
        g2.fillRoundRect(x + 135, y + 90, 30, 80, 6, 6);
        g2.fillRoundRect(x + 68, y + 182, 28, 90, 6, 6);
        g2.fillRoundRect(x + 104, y + 182, 28, 90, 6, 6);

        // Staff
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(100, 180, 220, 120));
        g2.drawLine(x + 165, y + 55, x + 165, y + 270);
        g2.fillOval(x + 155, y + 42, 20, 20);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.setColor(new Color(160, 220, 80, 120));
        g2.drawString("[CHARACTER SPRITE]", x + 20, y + 318);
    }

    private void drawLogoTopRight(Graphics2D g2, int w) {
        // Logo "Survival SYLVAN" kecil di pojok kanan atas (seperti gambar referensi)
        int lx = w - 115, ly = 14;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(lx - 4, ly - 2, 112, 58, 6, 6);
        g2.setColor(COL_GREEN_LIGHT);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx - 4, ly - 2, 112, 58, 6, 6);

        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(new Color(200, 240, 100));
        g2.drawString("Survival", lx, ly + 18);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("SYLVAN", lx, ly + 42);
    }
}
