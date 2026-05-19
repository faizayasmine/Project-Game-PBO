package anara.main;

import anara.core.GameEngine;
import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameEngine engine = GameEngine.getInstance();
            engine.start();
        });
        
    }
}