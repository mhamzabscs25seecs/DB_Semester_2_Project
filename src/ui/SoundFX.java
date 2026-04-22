package ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SoundFX {
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        SoundFX.enabled = enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void click() {
        playTone(880, 35, 0.18);
    }

    public static void success() {
        new Thread(() -> {
            if (!enabled) {
                return;
            }
            generateTone(740, 55, 0.16);
            generateTone(1040, 70, 0.14);
        }, "clixky-success-sound").start();
    }

    public static void error() {
        new Thread(() -> {
            if (!enabled) {
                return;
            }
            generateTone(240, 80, 0.18);
            generateTone(180, 90, 0.16);
        }, "clixky-error-sound").start();
    }

    private static void playTone(int hz, int millis, double volume) {
        if (!enabled) {
            return;
        }

        new Thread(() -> generateTone(hz, millis, volume), "clixky-click-sound").start();
    }

    private static void generateTone(int hz, int millis, double volume) {
        try {
            float sampleRate = 44100f;
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            int samples = (int) ((millis / 1000.0) * sampleRate);
            byte[] output = new byte[samples];

            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * i * hz / sampleRate;
                double fade = 1.0 - (double) i / samples;
                output[i] = (byte) (Math.sin(angle) * 127.0 * volume * fade);
            }

            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                line.write(output, 0, output.length);
                line.drain();
            }
        } catch (Exception ignored) {
            // Sound is optional. Some lab machines or headless runs have no audio device.
        }
    }
}
