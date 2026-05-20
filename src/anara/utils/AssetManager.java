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
    public static BufferedImage soldierBasic;
    public static BufferedImage soldierJalan1;
    public static BufferedImage soldierJalan2;
    public static BufferedImage soldierAttack1;
    public static BufferedImage soldierAttack2;
    public static BufferedImage soldierMati;
    public static BufferedImage miniBossAttack1;
    public static BufferedImage miniBossAttack2;
    public static BufferedImage finalBossBasic;
    public static BufferedImage finalBossAttack1;
    public static BufferedImage map1Background;
    public static BufferedImage beranda;
    public static BufferedImage map3Background;
    public static BufferedImage Shop;
    public static BufferedImage senjata1, senjata2, senjata3, senjata4;
    public static BufferedImage skil1, skil2, skil3;
    public static BufferedImage map4Background;
    

    public static void loadAssets() {
        playerBasic = load("/assets/images/playerbasic.png");
        playerAttack = load("/assets/images/playerattack.png");
        playerLari = load("/assets/images/playerlari.png");
        playerEliminasi = load("/assets/images/playerelminasi.png");
        soldierBasic = load("/assets/images/soliderbasic.png");
        soldierJalan1 = load("/assets/images/soliderjalan1.png");
        soldierJalan2 = load("/assets/images/soliderjalan2.png");
        soldierAttack1 = load("/assets/images/soliderattack1.png");
        soldierAttack2 = load("/assets/images/soliderattack2.png");
        soldierMati = load("/assets/images/solidermati.png");
        miniBossAttack1 = load("/assets/images/minimonsterattack1.png");
        miniBossAttack2 = load("/assets/images/minimonsterattack2.png");
        finalBossBasic = load("/assets/images/finalbosbasic.png");
        finalBossAttack1 = load("/assets/images/finalbosattack1.png");
        map1Background = load("/assets/images/map1.png");
        beranda = load("/assets/images/beranda.png");
        map3Background = load("/assets/images/MAP3.png");
        Shop = load("/assets/images/shop.png");
        senjata1 = load("/assets/images/senjata1.png");
        senjata2 = load("/assets/images/senjata2.png");
        senjata3 = load("/assets/images/senjata3.png");
        senjata4 = load("/assets/images/senjata4.png");
        skil1 = load("/assets/images/skil1.png");
        skil2 = load("/assets/images/skil2.png");
        skil3 = load("/assets/images/skil3.png");
        map4Background = load("/assets/images/map4.png");
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
