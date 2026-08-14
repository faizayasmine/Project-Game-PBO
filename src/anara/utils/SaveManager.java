package anara.utils;

import anara.model.PlayerData;
import java.io.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SaveManager {

    private static final String SAVE_DIR = "saves/";

    static {
        new File(SAVE_DIR).mkdirs();
    }

    /** Bersihkan nama pemain sebelum dipakai sebagai nama file, cegah path-traversal/karakter aneh. */
    private static String sanitize(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /** Hash SHA-256 satu arah untuk password, supaya tidak disimpan plain text. */
    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void savePlayer(PlayerData player) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_DIR + sanitize(player.getName()) + ".sav"))) {
            oos.writeObject(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayerData loadPlayer(String name) {
        File f = new File(SAVE_DIR + sanitize(name) + ".sav");
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
        return new File(SAVE_DIR + sanitize(name) + ".sav").exists();
    }

    public static List<String> getRegisteredPlayers() {
        List<String> names = new ArrayList<>();
        File dir = new File(SAVE_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".sav"));
            if (files != null) {
                for (File f : files) {
                    names.add(f.getName().replace(".sav", ""));
                }
            }
        }
        return names;
    }

    public static void deletePlayer(String name) {
        new File(SAVE_DIR + sanitize(name) + ".sav").delete();
    }

    public static boolean verifyPassword(String username, String password) {
        PlayerData player = loadPlayer(username);

        if (player == null) {
            return false;
        }

        return player.getPassword() != null
                && player.getPassword().equals(hash(password));
    }
}
