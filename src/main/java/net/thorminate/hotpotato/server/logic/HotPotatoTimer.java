package net.thorminate.hotpotato.server.logic;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.util.Formatting.RED;
import static net.minecraft.text.Text.translatable;
import static net.thorminate.hotpotato.HotPotato.LOGGER;
import static net.thorminate.hotpotato.server.HotPotatoGame.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotPotatoTimer {
    private static ScheduledExecutorService SCHEDULER;
    private static ServerPlayerEntity currentHotPotato = null;
    private static int timeLeft = -1;

    public static void startTimer(MinecraftServer server) {
        if (SCHEDULER != null) SCHEDULER.shutdown();
        SCHEDULER = Executors.newScheduledThreadPool(1);
        SCHEDULER.scheduleAtFixedRate(() -> {
            timeLeft = getCountdown(server);
            currentHotPotato = server.getPlayerManager().getPlayer(getCurrentHotPotato(server));

            if (timeLeft <= 1) {
                if (currentHotPotato != null) {
                    eliminate(currentHotPotato, server.getWorld(currentHotPotato.getWorld().getRegistryKey()));
                } else {
                    LOGGER.warn("Hot potato exploded, but the hot potato was not found! Make sure the player is online.");
                }
                server.getPlayerManager().broadcast(translatable("hot-potato.exploded").formatted(RED), true);
                stop(server);
            }

            timeLeft--;
            setCountdown(server, timeLeft);
            sync(server);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopTimer() {
        if (SCHEDULER != null) SCHEDULER.shutdown();
    }
}
