package anara.ui;

import anara.core.GameEngine;
import anara.model.PlayerData;
import anara.utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class LoginScreen extends BasePanel {
    private JTextField nameField;
    private JButton btnLogin, btnRegister;
    private JComboBox<String> existingPlayers;
    private JLabel statusLabel;
    private boolean isRegisterMode = false;

    // Animated logo
    private Timer animTimer;
    private float glowPhase = 0f;

    public LoginScreen() {
        setLayout(null);
        setOpaque(true);
        setupComponents();
        startAnimation();
    }

    private void setupComponents() {
        int w = 900, h = 580;
        int cx = w / 2;

        // Name input field
        nameField = new JTextField();
        nameField.setBounds(cx - 150, 320, 300, 38);
        nameField.setBackground(new Color(15, 12, 22));
        nameField.setForeground(COL_TEXT);
        nameField.setCaretColor(COL_GOLD);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COL_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        nameField.setFont(new Font("Serif", Font.PLAIN, 16));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.putClientProperty("JTextField.placeholderText", "Masukkan nama...");
        add(nameField);

        // Existing players dropdown
        existingPlayers = new JComboBox<>();
        existingPlayers.setBounds(cx - 150, 320, 300, 38);
        existingPlayers.setBackground(new Color(15, 12, 22));
        existingPlayers.setForeground(COL_TEXT);
        existingPlayers.setFont(new Font("Serif", Font.PLAIN, 15));
        refreshPlayerList();
        existingPlayers.setVisible(false);
        add(existingPlayers);

        // Login Button
        btnLogin = createStyledButton("MASUK", cx - 155, 375, 145, 42);
        btnLogin.addActionListener(e -> doLogin());
        add(btnLogin);

        // Register Button
        btnRegister = createStyledButton("DAFTAR BARU", cx + 10, 375, 145, 42);
        btnRegister.addActionListener(e -> toggleMode());
        add(btnRegister);

        // Status label
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setBounds(cx - 200, 430, 400, 30);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusLabel.setForeground(COL_RED_LIGHT);
        add(statusLabel);

        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
    }

    private JButton createStyledButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean hov = getModel().isRollover();
                drawButton(g2, 0, 0, getWidth(), getHeight(), getText(), hov, getModel().isPressed());
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

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            nameField.setVisible(true);
            existingPlayers.setVisible(false);
            btnRegister.setText("BATAL");
            btnLogin.setText("BUAT AKUN");
            statusLabel.setText("Mode: Buat akun baru");
            statusLabel.setForeground(COL_GREEN);
        } else {
            refreshPlayerList();
            nameField.setVisible(false);
            existingPlayers.setVisible(true);
            btnRegister.setText("DAFTAR BARU");
            btnLogin.setText("MASUK");
            statusLabel.setText("");
        }
        repaint();
    }

    private void refreshPlayerList() {
        existingPlayers.removeAllItems();
        List<String> players = SaveManager.getRegisteredPlayers();
        if (players.isEmpty()) {
            existingPlayers.addItem("-- Belum ada akun --");
        } else {
            for (String p : players) existingPlayers.addItem(p);
        }
    }

    private void doLogin() {
        if (isRegisterMode) {
            // Register new player
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                statusLabel.setText("Nama tidak boleh kosong!");
                statusLabel.setForeground(COL_RED_LIGHT);
                return;
            }
            if (name.length() < 3) {
                statusLabel.setText("Nama minimal 3 karakter!");
                statusLabel.setForeground(COL_RED_LIGHT);
                return;
            }
            if (SaveManager.playerExists(name)) {
                statusLabel.setText("Nama sudah digunakan!");
                statusLabel.setForeground(COL_RED_LIGHT);
                return;
            }
            PlayerData newPlayer = new PlayerData(name);
            SaveManager.savePlayer(newPlayer);
            GameEngine.getInstance().setCurrentPlayer(newPlayer);
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        } else {
            // Login existing
            List<String> players = SaveManager.getRegisteredPlayers();
            if (players.isEmpty()) {
                statusLabel.setText("Belum ada akun. Buat akun dulu!");
                statusLabel.setForeground(COL_RED_LIGHT);
                return;
            }
            String selectedName = (String) existingPlayers.getSelectedItem();
            if (selectedName == null || selectedName.startsWith("--")) {
                statusLabel.setText("Pilih akun terlebih dahulu!");
                return;
            }
            PlayerData player = SaveManager.loadPlayer(selectedName);
            if (player == null) {
                statusLabel.setText("Gagal memuat data!");
                return;
            }
            GameEngine.getInstance().setCurrentPlayer(player);
            GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAIN_MENU);
        }
    }

    private void startAnimation() {
        animTimer = new Timer(30, e -> {
            glowPhase += 0.05f;
            if (glowPhase > Math.PI * 2) glowPhase = 0;
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        drawBackground(g2, w, h);

        // Draw Anara silhouette (warrior figure)
        drawAnaraSilhouette(g2, cx, h / 2 - 30);

        // Title glow effect
        float glow = (float)(0.7 + 0.3 * Math.sin(glowPhase));
        g2.setColor(new Color(220, 175, 60, (int)(glow * 80)));
        g2.setFont(new Font("Serif", Font.BOLD, 78));
        FontMetrics fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth("ANARA") / 2;
        g2.drawString("ANARA", tx + 2, 155);

        drawTitle(g2, "ANARA", cx, 155, 78);

        // Subtitle
        g2.setFont(new Font("Serif", Font.ITALIC, 16));
        g2.setColor(COL_TEXT_DIM);
        String sub = "The Wandering Warrior";
        g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub) / 2, 178);

        // Decorative divider
        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(cx - 180, 200, cx - 20, 200);
        g2.drawLine(cx + 20, 200, cx + 180, 200);
        g2.setColor(COL_GOLD);
        g2.fillOval(cx - 6, 195, 12, 12);

        // Panel
        drawPanel(g2, cx - 200, 270, 400, 80);

        // Label above input
        String mode = isRegisterMode ? "NAMA KARAKTER BARU" : "PILIH AKUN";
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(COL_GOLD);
        g2.drawString(mode, cx - g2.getFontMetrics().stringWidth(mode) / 2, 295);

        // Instruction
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(COL_TEXT_DIM);
        String hint = isRegisterMode ? "Ketik nama lalu klik BUAT AKUN" : "Pilih nama dari dropdown";
        g2.drawString(hint, cx - g2.getFontMetrics().stringWidth(hint) / 2, 312);

        // Footer
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(80, 70, 50));
        g2.drawString("© Anara Game Studio", cx - 60, h - 15);

        g2.dispose();
    }

    private void drawAnaraSilhouette(Graphics2D g2, int cx, int cy) {
        // Simple warrior silhouette (top-down angle hint, just decorative)
        g2.setColor(new Color(40, 30, 60, 120));

        // Body circle (top-down view)
        g2.fillOval(cx - 25, cy - 25, 50, 50);

        // Ponytail
        g2.setColor(new Color(60, 40, 20, 100));
        g2.fillOval(cx - 10, cy - 40, 20, 30);
        g2.fillOval(cx - 5, cy - 55, 10, 25);

        // Sword on back
        g2.setColor(new Color(80, 80, 100, 120));
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 15, cy - 35, cx + 25, cy + 35);
    }
}