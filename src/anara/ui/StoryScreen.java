package anara.ui;

import anara.core.GameEngine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StoryScreen extends BasePanel {

    private static final long serialVersionUID = 1L;

    private static final String[] SCENES = {
        "cerita1", "cerita2", "cerita3", "cerita4"
    };

    private int currentScene = 0;
    private float alpha = 0f;
    private boolean fading = false;
    private Timer animTimer;
    private Font storyFont;
    private Image[] sceneImages;

    public StoryScreen() {
        setLayout(null);
        setOpaque(true);
        loadFont();
        loadImages();
        startFadeIn();
        setupInput();
    }

    private void loadFont() {
        try (java.io.InputStream is = getClass().getResourceAsStream(
                "/assets/font/ari-w9500-condensed-bold.ttf")) {
            if (is != null) {
                storyFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(storyFont);
                return;
            }
        } catch (Exception ignored) {}
        storyFont = new Font("Serif", Font.BOLD, 14);
    }

    private void loadImages() {
        sceneImages = new Image[SCENES.length];
        for (int i = 0; i < SCENES.length; i++) {
            java.net.URL url = getClass().getResource("/assets/images/story/" + SCENES[i] + ".png");
            if (url != null) {
                sceneImages[i] = new ImageIcon(url).getImage();
                System.out.println("[StoryScreen] Loaded: " + SCENES[i] + ".png");
            } else {
                System.err.println("[StoryScreen] NOT FOUND: " + SCENES[i] + ".png");
            }
        }
    }

    private void startFadeIn() {
        alpha = 0f;
        fading = false;
        animTimer = new Timer(16, e -> {
            if (!fading) {
                if (alpha < 1f) alpha = Math.min(1f, alpha + 0.025f);
            } else {
                alpha = Math.max(0f, alpha - 0.04f);
                if (alpha <= 0f) {
                    animTimer.stop();
                    currentScene++;
                    if (currentScene >= SCENES.length) {
                        GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAP_SELECT);
                    } else {
                        startFadeIn();
                    }
                }
            }
            repaint();
        });
        animTimer.start();
    }

    private void nextScene() {
        if (!fading) fading = true;
    }

    private void skipAll() {
        if (animTimer != null) animTimer.stop();
        GameEngine.getInstance().showScreen(GameEngine.SCREEN_MAP_SELECT);
    }

    private void setupInput() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE
         || e.getKeyCode() == KeyEvent.VK_ENTER) nextScene();
    }
});
       addMouseListener(new MouseAdapter() {
    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        nextScene();
    }
});
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background hitam
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        // Gambar scene dengan fade
        Image img = (currentScene < sceneImages.length) ? sceneImages[currentScene] : null;
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int iw = img.getWidth(this), ih = img.getHeight(this);
            if (iw > 0 && ih > 0) {
                double scale = Math.max((double) w / iw, (double) h / ih);
                int dw = (int)(iw * scale), dh = (int)(ih * scale);
                g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, this);
            }
        }

        // Indikator dots
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        int dotSpacing = 18;
        int dotStartX = w / 2 - (SCENES.length * dotSpacing) / 2;
        for (int i = 0; i < SCENES.length; i++) {
            g2.setColor(i == currentScene ? new Color(220, 180, 80) : new Color(100, 100, 100));
            g2.fillOval(dotStartX + i * dotSpacing, h - 55, 10, 10);
        }

//        // Tombol SKIP — bawah tengah
//        int skipW = 120, skipH = 32;
//        int skipX = w - skipW - 20;
//int skipY = h - 50;
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
//        g2.setColor(new Color(0x59, 0x69, 0x00));
//        g2.fillRoundRect(skipX, skipY, skipW, skipH, 8, 8);
//        g2.setColor(new Color(0x8a, 0xaa, 0x00));
//        g2.setStroke(new BasicStroke(1.5f));
//        g2.drawRoundRect(skipX, skipY, skipW, skipH, 8, 8);
//        g2.setFont(storyFont.deriveFont(Font.PLAIN, 13f));
//        g2.setColor(new Color(0xd4, 0xf5, 0x42));
//        FontMetrics fm = g2.getFontMetrics();
//        g2.drawString("SKIP  >>>", skipX + (skipW - fm.stringWidth("SKIP  >>>")) / 2, skipY + 21);
//
//        g2.dispose();
    }
}