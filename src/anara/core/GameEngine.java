package anara.core;

import anara.ui.*;
import anara.model.PlayerData;
import anara.audio.SoundManager;
import anara.utils.AssetManager;
import javax.swing.*;
import java.awt.*;

public class GameEngine {

    private static GameEngine instance;
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PlayerData currentPlayer;
    private SoundManager soundManager;

    // Screen name constants
    public static final String SCREEN_START       = "START";       // ← diubah: int→String, tambah StartScreen
    public static final String SCREEN_LOGIN       = "LOGIN";
    public static final String SCREEN_MAIN_MENU   = "MAIN_MENU";
    public static final String SCREEN_MAP_SELECT  = "MAP_SELECT";
    public static final String SCREEN_LOADING     = "LOADING";
    public static final String SCREEN_BATTLE      = "BATTLE";
    public static final String SCREEN_SHOP        = "SHOP";
    public static final String SCREEN_PLAYER_DATA = "PLAYER_DATA";
    public static final String SCREEN_SETTING     = "SETTING";

    private GameEngine() {
        soundManager = SoundManager.getInstance();
    }

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public void start() {
        mainFrame = new JFrame("SURVIVAL SYLVAN");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(900, 650);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setCursor(createCustomCursor());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.BLACK);

        AssetManager.loadAssets();

        // Register all screens
        mainPanel.add(new StartScreen(),      SCREEN_START);       // ← baru
        mainPanel.add(new LoginScreen(),      SCREEN_LOGIN);
        mainPanel.add(new MainMenuScreen(),   SCREEN_MAIN_MENU);
        mainPanel.add(new MapSelectScreen(),  SCREEN_MAP_SELECT);
        mainPanel.add(new LoadingScreen(),    SCREEN_LOADING);
        mainPanel.add(new BattleScreen(),     SCREEN_BATTLE);
        mainPanel.add(new ShopScreen(),       SCREEN_SHOP);
        mainPanel.add(new PlayerDataScreen(), SCREEN_PLAYER_DATA);
        mainPanel.add(new SettingScreen(),    SCREEN_SETTING);

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);

        showScreen(SCREEN_START);                                   // ← diubah: LOGIN → START
    }

    private Cursor createCustomCursor() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = createCursorImage();
        return tk.createCustomCursor(img, new Point(0, 0), "AnaraCursor");
    }

    private Image createCursorImage() {
        int size = 32;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(220, 180, 80));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(16, 0, 16, 32);
        g.drawLine(0, 16, 32, 16);
        g.setColor(new Color(255, 220, 100, 180));
        g.fillOval(13, 13, 6, 6);
        g.setColor(new Color(220, 180, 80));
        g.drawOval(13, 13, 6, 6);
        g.dispose();
        return img;
    }

    public void showScreen(String screenName) {
        // Re-create screens yang perlu data fresh setiap kali ditampilkan
        if (screenName.equals(SCREEN_MAIN_MENU)) {
            Component old = getScreenComponent(SCREEN_MAIN_MENU);
            if (old != null) mainPanel.remove(old);
            mainPanel.add(new MainMenuScreen(), SCREEN_MAIN_MENU);
        }
        if (screenName.equals(SCREEN_PLAYER_DATA)) {
            Component old = getScreenComponent(SCREEN_PLAYER_DATA);
            if (old != null) mainPanel.remove(old);
            mainPanel.add(new PlayerDataScreen(), SCREEN_PLAYER_DATA);
        }
        if (screenName.equals(SCREEN_SHOP)) {
            Component old = getScreenComponent(SCREEN_SHOP);
            if (old != null) mainPanel.remove(old);
            mainPanel.add(new ShopScreen(), SCREEN_SHOP);
        }
        cardLayout.show(mainPanel, screenName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showBattle(int mapId) {
        Component oldBattle = getScreenComponent(SCREEN_BATTLE);
        if (oldBattle != null) {
            mainPanel.remove(oldBattle);
        }
        BattleScreen battle = new BattleScreen();
        battle.setMapId(mapId);
        mainPanel.add(battle, SCREEN_BATTLE);
        showScreen(SCREEN_LOADING);
        Timer t = new Timer(2500, e -> {
            showScreen(SCREEN_BATTLE);
            battle.startBattle();
        });
        t.setRepeats(false);
        t.start();
    }

    private Component getScreenComponent(String name) {
        for (Component c : mainPanel.getComponents()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public PlayerData getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(PlayerData p) { this.currentPlayer = p; }
    public SoundManager getSoundManager() { return soundManager; }
    public JFrame getMainFrame() { return mainFrame; }
}