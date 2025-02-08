package net.thorminate.hotpotato.server.logic;

public class HotPotatoCooldownManager {
    private static long cooldown = 0;
    private static final int COOLDOWN_TIME = 3000; // 3 seconds

    public static boolean isOnCooldown() {
        long currentTime = System.currentTimeMillis(); // Milliseconds since jan 1, 1970
        return cooldown > currentTime;
    }

    public static void setCooldown() {
        long currentTime = System.currentTimeMillis(); // Milliseconds since jan 1, 1970
        cooldown = currentTime + COOLDOWN_TIME;
    }
}
