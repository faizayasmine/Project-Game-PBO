package anara.utils;

import anara.model.PlayerData;
import java.io.*;
import java.util.*;

public class SaveManager {

    private static final String SAVE_DIR = "saves/";
    private static final String REGISTRY_FILE = SAVE_DIR + "players.dat";

    static {
        new File(SAVE_DIR).mkdirs();
    }

    public static void savePlayer(PlayerData player) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_DIR + player.getName() + ".sav"))) {
            oos.writeObject(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerPlayer(player.getName());
    }

    public static PlayerData loadPlayer(String name) {
        File f = new File(SAVE_DIR + name + ".sav");
        if (!f.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (PlayerData) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean playerExists(String name) {
        return new File(SAVE_DIR + name + ".sav").exists();
    }

    public static List<String> getRegisteredPlayers() {
        List<String> names = new ArrayList<>();
        File dir = new File(SAVE_DIR);
        if (dir.exists()) {
            for (File f : dir.listFiles((d, n) -> n.endsWith(".sav"))) {
                names.add(f.getName().replace(".sav", ""));
            }
        }
        return names;
    }

    private static void registerPlayer(String name) {
        // Players auto-discovered by .sav files
    }

    public static void deletePlayer(String name) {
        new File(SAVE_DIR + name + ".sav").delete();
    }

    public static boolean verifyPassword(String username, String password) {
        PlayerData player = loadPlayer(username);

        if (player == null) {
            return false;
        }

        return player.getPassword() != null
                && player.getPassword().equals(password);
    }
}
