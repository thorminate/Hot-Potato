package net.thorminate.hotpotato.server.logic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.server.HotPotatoGame;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotPotatoTimer {
    private static ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static ServerPlayerEntity currentHotPotato = null;
    private static int timeLeft = -1;

    public static void startTimer(MinecraftServer server) {
        if (SCHEDULER.isShutdown()) {
            SCHEDULER = Executors.newScheduledThreadPool(1);
        }
        SCHEDULER.scheduleAtFixedRate(() -> {
            timeLeft = HotPotatoGame.getCountdown(server);
            currentHotPotato = server.getPlayerManager().getPlayer(HotPotatoGame.getCurrentHotPotato(server));

            if (timeLeft <= 0) {
                HotPotato.LOGGER.info("Hot potato exploded!");
                if (currentHotPotato != null) {
                    eliminatePlayer(currentHotPotato);
                } else {
                    HotPotato.LOGGER.info("Hot potato exploded, but the hot potato was null! Make sure the player is online.");
                }
                server.getPlayerManager().broadcast(Text.literal("Hot potato exploded!").formatted(Formatting.RED), true);
                HotPotatoGame.stopHotPotato(server);
            }

            timeLeft--;
            HotPotatoGame.setCountdown(server, timeLeft);
            HotPotatoGame.syncDataWithPlayers(server);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopTimer() {
        SCHEDULER.shutdown();
    }

    private static void eliminatePlayer(ServerPlayerEntity player) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, player.getWorld());
        lightning.setPosition(player.getPos());
        player.getWorld().spawnEntity(lightning);
        player.kill();
    }
}
