package net.thorminate.hotpotato.server.logic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.common.HotPotatoIndex;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotPotatoTimer {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static ServerPlayerEntity currentHotPotato = null;
    private static int timeLeft = -1;

    public static void startTimer(int timeInSeconds, MinecraftServer server) {
        currentHotPotato = server.getPlayerManager().getPlayer(HotPotatoIndex.getCurrentHotPotato(server));
        timeLeft = timeInSeconds;
        if (currentHotPotato == null) {
            HotPotato.LOGGER.warn("No hot potato given!");
        } else {
            HotPotato.LOGGER.info("Hot potato timer starting!");
            SCHEDULER.scheduleAtFixedRate(() -> {
                if (timeLeft <= 0) {
                    HotPotato.LOGGER.info("Hot potato exploded!");
                    spawnLightning(currentHotPotato);
                    HotPotatoIndex.stopHotPotato(server);
                }

                timeLeft--;
                HotPotatoIndex.setCountdown(server, timeLeft);
                HotPotatoIndex.syncDataWithPlayers(server);
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private static void spawnLightning(ServerPlayerEntity player) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, player.getWorld());
        lightning.setPosition(player.getPos());
        player.getWorld().spawnEntity(lightning);
        player.kill();
    }

    public static void stopTimer() {
        SCHEDULER.shutdown();
        timeLeft = -1;
    }
}
