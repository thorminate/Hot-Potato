package net.thorminate.hotpotato.utils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.thorminate.hotpotato.HotPotato;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotPotatoTimer {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    public static final Identifier TIMER_PACKET_ID = Identifier.of(HotPotato.MOD_ID, "hot_potato_timer");
    private static ServerPlayerEntity currentHotPotato = null;
    private static int timeLeft = 1800;

    public static void startTimer(int timeInSeconds, MinecraftServer server) {
        currentHotPotato = HotPotatoDataHandler.getCurrentHotPotato();
        timeLeft = timeInSeconds;
        if (currentHotPotato == null) {
            HotPotato.LOGGER.info("No hot potato given!");
        } else {
            SCHEDULER.scheduleAtFixedRate(() -> {
                if (timeLeft <= 0) {
                    explodePlayer(currentHotPotato);
                    stopTimer();
                    return;
                }

                timeLeft--;
                sendCountdownToAllPlayers(server);
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private static void explodePlayer(ServerPlayerEntity player) {
        player.getWorld().createExplosion(null, player.getX(), player.getY(), player.getZ(), 5, false, null);
        player.sendMessage(Text.literal("💥 You exploded!").formatted(Formatting.RED), false);
    }

    public static void stopTimer() {
        HotPotatoDataHandler.setCurrentHotPotato(null);
        timeLeft = -1;
        SCHEDULER.shutdown();
    }

    private static void sendCountdownToAllPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new HotPotatoCountdownPayload(timeLeft));
        }
    }
}
