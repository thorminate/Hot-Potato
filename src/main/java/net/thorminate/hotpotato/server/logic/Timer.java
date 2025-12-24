package net.thorminate.hotpotato.server.logic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.thorminate.hotpotato.server.HotPotatoManager;
import net.thorminate.hotpotato.server.command.MainCmd;

import static net.minecraft.network.chat.Component.translatable;
import static net.thorminate.hotpotato.server.HotPotatoManager.*;

import java.util.concurrent.*;

public final class Timer {

    private static int ticksRemaining = -1;
    private static int lastSecondSent = -1;
    private static boolean running = false;

    private Timer() {}

    public static void startTimer(MinecraftServer server) {
        if (running) return;

        ticksRemaining = getCountdown(server) * 20;
        lastSecondSent = -1;
        running = true;
    }

    public static void stopTimer() {
        running = false;
        ticksRemaining = -1;
        lastSecondSent = -1;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Timer::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!running || ticksRemaining < 0) return;

        ticksRemaining--;

        int secondsLeft = ticksRemaining / 20;

        // Send packets only when the visible second changes
        if (secondsLeft != lastSecondSent) {
            lastSecondSent = secondsLeft;
            setCountdown(server, secondsLeft);
            syncWithClients(server);
        }

        if (ticksRemaining > 0) return;

        ServerPlayer player = server.getPlayerList()
                .getPlayer(getCurrentHotPotato(server));

        if (player != null) {
            HotPotatoManager.explode(player);

            server.getPlayerList().broadcastSystemMessage(
                    translatable("hot-potato.exploded").withStyle(ChatFormatting.RED),
                    true
            );
        } else {
            doom(server, getCurrentHotPotato(server));

            server.getPlayerList().broadcastSystemMessage(
                    translatable("hot-potato.player_is_doomed").withStyle(ChatFormatting.RED),
                    true
            );
        }





        MainCmd.stop(server, false);
        stopTimer();
    }
}