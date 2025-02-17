package net.thorminate.hotpotato.server.logic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.thorminate.hotpotato.server.command.HotPotatoStopCommand;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.util.Formatting.RED;
import static net.minecraft.text.Text.translatable;
import static net.thorminate.hotpotato.HotPotato.LOGGER;
import static net.thorminate.hotpotato.server.HotPotatoManager.*;

import java.util.concurrent.*;

public class HotPotatoTimer {
    private static ScheduledExecutorService SCHEDULER;
    private static ServerPlayerEntity currentHotPotato;
    private static int timeLeft = -1;

    public static void startTimer(MinecraftServer server) {
        if (SCHEDULER != null) SCHEDULER.shutdown();
        SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Hot-Potato-Timer");
            return thread;
        });

        SCHEDULER.scheduleAtFixedRate(() -> {
            timeLeft = getCountdown(server);
            currentHotPotato = server.getPlayerManager().getPlayer(getCurrentHotPotato(server));

            if (timeLeft <= 1) {
                if (currentHotPotato != null) {
                    eliminate(currentHotPotato, server.getWorld(currentHotPotato.getWorld().getRegistryKey()));
                } else {
                    LOGGER.warn("The hot potato was not found! Make sure the player is online.");
                }
                server.getPlayerManager().broadcast(translatable("hot-potato.exploded").formatted(RED), true);
                HotPotatoStopCommand.stop(server);
            }

            timeLeft--;
            setCountdown(server, timeLeft);
            syncWithClients(server);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopTimer() {
        if (SCHEDULER != null) SCHEDULER.shutdown();
    }

    private static void eliminate(@NotNull ServerPlayerEntity player, ServerWorld world) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, player.getWorld());
        lightning.setPosition(player.getPos());
        player.getWorld().spawnEntity(lightning);
        player.kill(world);
    }
}
