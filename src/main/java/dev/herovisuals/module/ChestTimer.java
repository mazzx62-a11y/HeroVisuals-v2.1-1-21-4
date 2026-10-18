package dev.herovisuals.module;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads public countdown announcements delivered to the player's chat. It does not
 * infer hidden server state; if the server never announces a timer, no timer is shown.
 */
public final class ChestTimer {
    private ChestTimer() {}
    private static final Pattern CLOCK = Pattern.compile("(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)");
    private static final Pattern SECONDS = Pattern.compile("(?i)(\\d{1,4})\\s*(?:сек(?:унд[аы]?)?|seconds?|sec)");
    private static long deadlineMs = -1;
    private static String sourceLine = "";

    public static void accept(String message) {
        if (message == null) return;
        String lower = message.toLowerCase(Locale.ROOT);
        if (!(lower.contains("сундук") || lower.contains("сунду") || lower.contains("chest") || lower.contains("обнов") || lower.contains("refill") || lower.contains("рестарт"))) return;
        Matcher clock = CLOCK.matcher(message);
        if (clock.find()) {
            int minutes = Integer.parseInt(clock.group(1));
            int seconds = Integer.parseInt(clock.group(2));
            if (seconds < 60) set(minutes * 60 + seconds, message);
            return;
        }
        Matcher secs = SECONDS.matcher(message);
        if (secs.find()) {
            try { set(Integer.parseInt(secs.group(1)), message); } catch (NumberFormatException ignored) {}
        }
    }

    private static void set(int seconds, String line) {
        if (seconds < 0 || seconds > 86400) return;
        deadlineMs = System.currentTimeMillis() + seconds * 1000L;
        sourceLine = line;
    }

    public static String display() {
        if (deadlineMs < 0) return "Chest timer: waiting for server message";
        long left = Math.max(0, (deadlineMs - System.currentTimeMillis() + 999) / 1000);
        return String.format(Locale.ROOT, "Chest refresh: %02d:%02d", left / 60, left % 60);
    }

    public static String sourceLine() { return sourceLine; }
}
