package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class LoginScreen extends BasePanel {

    // ── Warna ─────────────────────────────────────────────────────────────────
    private static final Color COL_GREEN_DARK   = new Color(45, 85, 20);
    private static final Color COL_GREEN_MID    = new Color(80, 145, 40);
    private static final Color COL_GREEN_LIGHT  = new Color(160, 220, 80);
    private static final Color COL_LABEL_BG     = new Color(70, 105, 30);
    private static final Color COL_FIELD_BG     = new Color(230, 220, 185);   // krem seperti gambar referensi
    private static final Color COL_FIELD_TEXT   = new Color(60, 40, 20);      // coklat gelap
    private static final Color COL_PLACEHOLDER  = new Color(140, 120, 90);    // abu coklat

    // ── Font pixel kustom ─────────────────────────────────────────────────────
    private Font pixelFont;
    private Font pixelFontLg;   // ukuran besar untuk LOGIN GAME
    private Font pixelFontMd;   // ukuran label
    private Font pixelFontSm;   // ukuran field / tombol

    // ── Images ────────────────────────────────────────────────────────────────
    private Image bgImage;

    // ── Komponen form ─────────────────────────────────────────────────────────
    private JTextField     fieldUserId;
    private JTextField     fieldUsername;
    private JPasswordField fieldPassword;
    private JComboBox<String> comboExisting;
    private JButton        btnAction;
    private JButton        btnToggle;
    private JLabel         statusLabel;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean isRegisterMode = false;

    // ── Animasi ───────────────────────────────────────────────────────────────
    private Timer animTimer;
    private float glowPhase = 0f;

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int FORM_X = 60;
    private static final int FORM_Y = 160;
    private static final int FORM_W = 320;
    private static final int FIELD_H = 36;

    public LoginScreen() {
        setLayout(null);
        setOpaque(true);
        loadFont();
        loadAssets();
        setupComponents();
        startAnimation();
        refreshPlayerList();
    }

    // ── Load font kustom ──────────────────────────────────────────────────────
    private void loadFont() {
        String[] fontPaths = {
            "/assets/images/ari-w9500-condensed-bold.ttf",
            "/assets/fonts/ari-w9500-condensed-bold.ttf",
            "/Assets/images/ari-w9500-condensed-bold.ttf",
        };
        for (String path : fontPaths) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    pixelFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    ge.registerFont(pixelFont);
                    System.out.println("[LoginScreen] Font loaded: " + path);
                    break;
                }
            } catch (Exception e) {
                System.err.println("[LoginScreen] Font error: " + e.getMessage());
            }
        }
        // Derive ukuran
        if (pixelFont != null) {
            pixelFontLg = pixelFont.deriveFont(Font.BOLD, 52f);
            pixelFontMd = pixelFont.deriveFont(Font.BOLD, 16f);
            pixelFontSm = pixelFont.deriveFont(Font.BOLD, 14f);
        } else {
            // Fallback ke Monospaced
            pixelFontLg = new Font("Monospaced", Font.BOLD, 48);
            pixelFontMd = new Font("Monospaced", Font.BOLD, 14);
            pixelFontSm = new Font("Monospaced", Font.BOLD, 13);
        }
    }

    // ── Load background ───────────────────────────────────────────────────────
    private void loadAssets() {
        for (String path : new String[]{"/assets/images/login.png", "/Assets/images/login.png"}) {
            URL url = getClass().getResource(path);
            if (url != null) {
                bgImage = new ImageIcon(url).getImage();
                System.out.println("[LoginScreen] BG: " + path);
                return;
            }
        }
        System.err.println("[LoginScreen] login.png tidak ditemukan!");
    }

    // ── Setup komponen ────────────────────────────────────────────────────────
    private void setupComponents() {

        // ID User field
        fieldUserId = createPixelField("Masukkan ID pengguna...");
        fieldUserId.setBounds(FORM_X, FORM_Y + 100, FORM_W, FIELD_H);
        add(fieldUserId);

        // Username field (register)
        fieldUsername = createPixelField("Masukkan Username pengguna...");
        fieldUsername.setBounds(FORM_X, FORM_Y + 180, FORM_W, FIELD_H);
        add(fieldUsername);

        // Combo (login)
        comboExisting = new JComboBox<>();
        comboExisting.setBounds(FORM_X, FORM_Y + 100, FORM_W, FIELD_H);
        styleCombo(comboExisting);
        add(comboExisting);

        // Password field
        fieldPassword = createPixelPasswordField("Masukkan Password pengguna...");
        fieldPassword.setBounds(FORM_X, FORM_Y + 260, FORM_W, FIELD_H);
        add(fieldPassword);

        // Tombol aksi
        btnAction = createPixelButton("Login");
        btnAction.setBounds(FORM_X + FORM_W/2 - 90, FORM_Y + 315, 180, 44);
        btnAction.addActionListener(e -> doAction());
        add(btnAction);

        // Tombol toggle
        btnToggle = createLinkButton("Belum punya akun? Daftar");
        btnToggle.setBounds(FORM_X, FORM_Y + 368, FORM_W, 24);
        btnToggle.addActionListener(e -> toggleMode());
        add(btnToggle);

        // Status
        statusLabel = new JLabel("", SwingConstants.LEFT);
        statusLabel.setBounds(FORM_X, FORM_Y + 396, FORM_W, 22);
        statusLabel.setFont(pixelFontSm.deriveFont(11f));
        statusLabel.setForeground(new Color(255, 100, 80));
        add(statusLabel);

        fieldPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doAction();
            }
        });

        updateFieldVisibility();
    }

    // ── Field helpers ─────────────────────────────────────────────────────────
    private JTextField createPixelField(String placeholder) {
        JTextField f = new JTextField(placeholder) {
            boolean showPlaceholder = true;
            {
                setForeground(COL_PLACEHOLDER);
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) {
                        if (showPlaceholder) { setText(""); setForeground(COL_FIELD_TEXT); showPlaceholder = false; }
                    }
                    @Override public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) { setText(placeholder); setForeground(COL_PLACEHOLDER); showPlaceholder = true; }
                    }
                });
            }
            public String getRealText() { return showPlaceholder ? "" : getText(); }
        };
        f.setBackground(COL_FIELD_BG);
        f.setFont(pixelFontSm.deriveFont(13f));
        f.setOpaque(true);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 80, 40), 2),
            BorderFactory.createEmptyBorder(4, 10, 4, 8)
        ));
        f.setCaretColor(COL_FIELD_TEXT);
        return f;
    }

    private JPasswordField createPixelPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField() {
            boolean showPlaceholder = true;
            {
                setEchoChar((char)0);
                setText(placeholder);
                setForeground(COL_PLACEHOLDER);
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) {
                        if (showPlaceholder) { setText(""); setEchoChar('●'); setForeground(COL_FIELD_TEXT); showPlaceholder = false; }
                    }
                    @Override public void focusLost(FocusEvent e) {
                        if (getPassword().length == 0) { setEchoChar((char)0); setText(placeholder); setForeground(COL_PLACEHOLDER); showPlaceholder = true; }
                    }
                });
            }
        };
        f.setBackground(COL_FIELD_BG);
        f.setFont(pixelFontSm.deriveFont(13f));
        f.setOpaque(true);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 80, 40), 2),
            BorderFactory.createEmptyBorder(4, 10, 4, 8)
        ));
        f.setCaretColor(COL_FIELD_TEXT);
        return f;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(COL_FIELD_BG);
        cb.setForeground(COL_FIELD_TEXT);
        cb.setFont(pixelFontSm.deriveFont(13f));
        cb.setBorder(BorderFactory.createLineBorder(new Color(100, 80, 40), 2));
    }

    private JButton createPixelButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hov = getModel().isRollover();
                boolean prs = getModel().isPressed();
                int dy = prs ? 2 : 0;

                // Tombol hijau
                GradientPaint gp = new GradientPaint(0, 0,
                    hov ? new Color(120, 185, 55) : new Color(95, 150, 40),
                    0, getHeight(),
                    hov ? new Color(65, 115, 18) : COL_GREEN_DARK
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Border tebal coklat/gelap
                g2.setColor(new Color(40, 60, 15));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);

                // Highlight atas
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()/2-4, 4, 4);

                // Teks
                g2.setFont(pixelFontMd);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + dy;
                // Shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawString(getText(), tx+1, ty+2);
                // Teks putih
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(pixelFontSm.deriveFont(12f));
                g2.setColor(getModel().isRollover() ? COL_GREEN_LIGHT : new Color(200, 220, 160, 220));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Toggle mode ───────────────────────────────────────────────────────────
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        statusLabel.setText("");
        updateFieldVisibility();
        repaint();
    }

    private void updateFieldVisibility() {
        if (isRegisterMode) {
            fieldUserId.setVisible(true);
            fieldUsername.setVisible(true);
            comboExisting.setVisible(false);
            fieldUserId.setBounds(FORM_X,  FORM_Y + 100, FORM_W, FIELD_H);
            fieldUsername.setBounds(FORM_X, FORM_Y + 180, FORM_W, FIELD_H);
            fieldPassword.setBounds(FORM_X, FORM_Y + 260, FORM_W, FIELD_H);
            btnAction.setBounds(FORM_X + FORM_W/2 - 90, FORM_Y + 315, 180, 44);
            btnToggle.setBounds(FORM_X, FORM_Y + 368, FORM_W, 24);
            statusLabel.setBounds(FORM_X, FORM_Y + 396, FORM_W, 22);
            btnAction.setText("Buat Akun");
            btnToggle.setText("Sudah punya akun? Login");
        } else {
            fieldUserId.setVisible(false);
            fieldUsername.setVisible(false);
            comboExisting.setVisible(true);
            comboExisting.setBounds(FORM_X, FORM_Y + 100, FORM_W, FIELD_H);
            fieldPassword.setBounds(FORM_X,  FORM_Y + 180, FORM_W, FIELD_H);
            btnAction.setBounds(FORM_X + FORM_W/2 - 90, FORM_Y + 235, 180, 44);
            btnToggle.setBounds(FORM_X, FORM_Y + 288, FORM_W, 24);
            statusLabel.setBounds(FORM_X, FORM_Y + 316, FORM_W, 22);
            refreshPlayerList();
            btnAction.setText("Login");
            btnToggle.setText("Belum punya akun? Daftar");
        }
    }

    private void refreshPlayerList() {
        comboExisting.removeAllItems();
        List<String> players = SaveManager.getRegisteredPlayers();
        if (players.isEmpty()) comboExisting.addItem("-- Belum ada akun --");
        else for (String p : players) comboExisting.addItem(p);
    }

    // ── Aksi ─────────────────────────────────────────────────────────────────
    private void doAction() {
        statusLabel.setForeground(new Color(255, 80, 80));
        if (isRegisterMode) {
            String userId   = getFieldText(fieldUserId);
            String username = getFieldText(fieldUsername);
            String password = new String(fieldPassword.getPassword()).trim();
            if (userId.isEmpty())   { statusLabel.setText("ID pengguna tidak boleh kosong!"); return; }
            if (username.length()<3){ statusLabel.setText("Username minimal 3 karakter!");    return; }
            if (password.length()<4){ statusLabel.setText("Password minimal 4 karakter!");    return; }
            if (SaveManager.playerExists(username)){ statusLabel.setText("Username sudah digunakan!"); return; }
            PlayerData p = new PlayerData(username);
            p.setPassword(password);
            SaveManager.savePlayer(p);
            GameEngine.getInstance().setCurrentPlayer(p);
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        } else {
            if (SaveManager.getRegisteredPlayers().isEmpty()) { statusLabel.setText("Belum ada akun. Silakan daftar!"); return; }
            String sel = (String) comboExisting.getSelectedItem();
            if (sel == null || sel.startsWith("--")) { statusLabel.setText("Pilih akun terlebih dahulu!"); return; }
            String password = new String(fieldPassword.getPassword()).trim();
            if (!SaveManager.verifyPassword(sel, password)) { statusLabel.setText("Password salah!"); return; }
            PlayerData p = SaveManager.loadPlayer(sel);
            if (p == null) { statusLabel.setText("Gagal memuat data akun!"); return; }
            GameEngine.getInstance().setCurrentPlayer(p);
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        }
    }

    /** Ambil teks field, abaikan teks placeholder */
    private String getFieldText(JTextField f) {
        Color fg = f.getForeground();
        if (fg.equals(COL_PLACEHOLDER)) return "";
        return f.getText().trim();
    }

    // ── Animasi ───────────────────────────────────────────────────────────────
    private void startAnimation() {
        animTimer = new Timer(30, e -> { glowPhase += 0.05f; if (glowPhase > Math.PI*2) glowPhase=0; repaint(); });
        animTimer.start();
    }
    public void stopAnimation() { if (animTimer != null) animTimer.stop(); }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();

        // ── 1. BACKGROUND ─────────────────────────────────────────────────────
        if (bgImage != null) {
            int iw = bgImage.getWidth(this), ih = bgImage.getHeight(this);
            if (iw > 0 && ih > 0) {
                double sc = Math.max((double)w/iw, (double)h/ih);
                int dw=(int)(iw*sc), dh=(int)(ih*sc);
                g2.drawImage(bgImage, (w-dw)/2, (h-dh)/2, dw, dh, this);
            }
        } else {
            g2.setPaint(new GradientPaint(0,0,new Color(18,10,35),0,h/2,new Color(35,20,60),true));
            g2.fillRect(0,0,w,h);
            g2.setPaint(new GradientPaint(0,h*0.55f,new Color(15,40,15),0,h,new Color(5,15,5)));
            g2.fillRect(0,(int)(h*0.55f),w,h);
        }

        // ── 2. OVERLAY kiri (ringan) ───────────────────────────────────────────
        g2.setPaint(new GradientPaint(0,0,new Color(0,0,0,0), w*0.55f,0,new Color(0,0,0,0)));
        g2.fillRect(0,0,w,h);


        // ── 4. LABEL FIELD ────────────────────────────────────────────────────
        if (isRegisterMode) {
            drawFieldLabel(g2, "ID User",   FORM_X, FORM_Y + 65);
            drawFieldLabel(g2, "Username",  FORM_X, FORM_Y + 145);
            drawFieldLabel(g2, "Password",  FORM_X, FORM_Y + 225);
        } else {
            drawFieldLabel(g2, "Username",  FORM_X, FORM_Y + 65);
            drawFieldLabel(g2, "Password",  FORM_X, FORM_Y + 145);
        }
        g2.dispose();
    }

    private void drawFieldLabel(Graphics2D g2, String text, int x, int y) {
        g2.setFont(pixelFontMd);
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(text) + 24;
        int lh = 28;

        // Background label
        g2.setColor(COL_LABEL_BG);
        g2.fillRoundRect(x, y, lw, lh, 4, 4);
        // Border gelap
        g2.setColor(new Color(30, 50, 10));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, lw, lh, 4, 4);

        // Teks putih
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 12, y + lh - 7);
    }
}