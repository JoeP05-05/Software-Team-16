//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza
//Team: Team 16
import java.io.File;
import java.util.Random;

public class music {

    // Tracks are stored in project/assets/Music/ relative to the working directory.
    private static final String[] TRACKS = {
        "../assets/Music/Track01.mp3",
        "../assets/Music/Track02.mp3",
        "../assets/Music/Track03.mp3",
        "../assets/Music/Track04.mp3",
        "../assets/Music/Track05.mp3",
        "../assets/Music/Track06.mp3",
        "../assets/Music/Track07.mp3",
        "../assets/Music/Track08.mp3"
    };

    // Holds the currently running mpg123 process so we can stop it later.
    private static Process currentProcess = null;

    /**
     * Picks one of the three tracks at random and starts playing it
     * in a background thread so the UI is never blocked.
     * If a track is already playing it is stopped first.
     */
    public static void playRandomTrack() {
        stopMusic(); // stop any track that might already be running

        String track = TRACKS[new Random().nextInt(TRACKS.length)];
        File trackFile = new File(track);

        if (!trackFile.exists()) {
            System.err.println("[music] Track not found: " + trackFile.getAbsolutePath());
            return;
        }

        // Run mpg123 in a daemon thread so it never prevents JVM exit.
        Thread playerThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("mpg123", "-q", trackFile.getAbsolutePath());
                pb.redirectErrorStream(true); // silence mpg123 console output
                currentProcess = pb.start();
                System.out.println("[music] Now playing: " + track);
                currentProcess.waitFor(); // block this background thread until done
            } catch (Exception e) {
                System.err.println("[music] Playback error: " + e.getMessage());
                System.err.println("[music] Make sure mpg123 is installed: sudo apt install mpg123");
            }
        });

        playerThread.setDaemon(true);
        playerThread.start();
    }

    /**
     * Stops whatever track is currently playing (if any).
     */
    public static void stopMusic() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            System.out.println("[music] Playback stopped.");
        }
        currentProcess = null;
    }
}
