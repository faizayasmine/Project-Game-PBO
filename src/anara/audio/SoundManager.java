package anara.audio;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private boolean soundEnabled = true;

    private Clip bgmClip;
    private final Map<String, Clip> sfxCache = new HashMap<>();

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) stopAll();
    }

    public void toggleSound() { setSoundEnabled(!this.soundEnabled); }

    /** Cari file audio di /assets/audio/<name>.wav lewat classpath (bukan File I/O absolut). */
    private Clip loadClip(String name) {
        try {
            java.io.InputStream raw = getClass().getResourceAsStream("/assets/audio/" + name + ".wav");
            if (raw == null) {
                System.out.println("[SoundManager] Berkas audio tidak ditemukan: " + name + ".wav");
                return null;
            }
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new java.io.BufferedInputStream(raw))) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                return clip;
            }
        } catch (Exception e) {
            System.out.println("[SoundManager] Gagal memuat audio '" + name + "': " + e.getMessage());
            return null;
        }
    }

    public void playBGM(String trackName) {
        if (!soundEnabled) return;
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
        }
        bgmClip = loadClip(trackName);
        if (bgmClip != null) {
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void playSFX(String sfxName) {
        if (!soundEnabled) return;
        Clip cached = sfxCache.get(sfxName);
        if (cached != null) {
            cached.stop();
            cached.setFramePosition(0);
            cached.start();
            return;
        }
        Clip clip = loadClip(sfxName);
        if (clip != null) {
            sfxCache.put(sfxName, clip);
            clip.start();
        }
    }

    public void stopAll() {
        if (bgmClip != null) bgmClip.stop();
        for (Clip c : sfxCache.values()) {
            if (c.isRunning()) c.stop();
        }
    }
}
