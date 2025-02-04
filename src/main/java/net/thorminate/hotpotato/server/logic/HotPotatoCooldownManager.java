package net.thorminate.hotpotato.server.logic;

public class HotPotatoCooldownManager {
    private static long cooldown = 0;
    private static final long COOLDOWN_TIME = 3000; // 3 seconds

    public static boolean isOnCooldown() {
        long currentTime = System.currentTimeMillis();
        return cooldown > currentTime;
    }

    public static void setCooldown() {
        cooldown = System.currentTimeMillis() + COOLDOWN_TIME;
    }
}
