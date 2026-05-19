package anara.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class AssetManager {

    // Sprite Player
    public static BufferedImage playerBasic;
    public static BufferedImage playerAttack;
    public static BufferedImage playerLari;
    public static BufferedImage playerEliminasi;

    // Sprite Soldier (Zombie)
    public static BufferedImage soldierBasic;
    public static BufferedImage soldierJalan1;
    public static BufferedImage soldierJalan2;
    public static BufferedImage soldierAttack1;
    public static BufferedImage soldierAttack2;
    public static BufferedImage soldierMati;
public static BufferedImage miniBossAttack1;
public static BufferedImage miniBossAttack2;

    public static void loadAssets() {
        playerBasic = load("/assets/images/playerbasic.png");
        playerAttack = load("/assets/images/playerattack.png");
        playerLari = load("/assets/images/playerlari.png");
        playerEliminasi = load("/assets/images/playerelminasi.png");
// Load Soldier
        soldierBasic = load("/assets/images/soliderbasic.png");
        soldierJalan1 = load("/assets/images/soliderjalan1.png");
        soldierJalan2 = load("/assets/images/soliderjalan2.png");
        soldierAttack1 = load("/assets/images/soliderattack1.png");
        soldierAttack2 = load("/assets/images/soliderattack2.png");
        soldierMati = load("/assets/images/solidermati.png");
        miniBossAttack1 = load("/assets/images/minimonsterattack1.png");
miniBossAttack2 = load("/assets/images/minimonsterattack2.png");
    }

    private static BufferedImage load(String path) {
        try {
            BufferedImage img = ImageIO.read(AssetManager.class.getResourceAsStream(path));
            System.out.println("OK: " + path);
            return img;
        } catch (Exception e) {
            System.out.println("GAGAL: " + path);
            return null;
        }
    }
}
