package anara.audio;

public class SoundManager {
    private static SoundManager instance;
    private boolean soundEnabled = true;

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }
    public void toggleSound() { this.soundEnabled = !this.soundEnabled; }

    // Placeholder: In a full implementation, use javax.sound.sampled
    // to load and play WAV/OGG files from /resources/audio/
    public void playBGM(String trackName) {
        if (!soundEnabled) return;
        // javax.sound.sampled.Clip clip = loadClip("audio/" + trackName + ".wav");
        // clip.loop(Clip.LOOP_CONTINUOUSLY);
        System.out.println("[SoundManager] Playing BGM: " + trackName);
    }

    public void playSFX(String sfxName) {
        if (!soundEnabled) return;
        System.out.println("[SoundManager] Playing SFX: " + sfxName);
    }

    public void stopAll() {
        System.out.println("[SoundManager] Stopped all audio");
    }
}