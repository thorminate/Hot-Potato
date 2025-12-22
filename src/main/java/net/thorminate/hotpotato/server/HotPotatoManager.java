package net.thorminate.hotpotato.server;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.sounds.SoundEvents;
import net.thorminate.hotpotato.server.logic.Timer;
import net.thorminate.hotpotato.server.storage.StorageManager;
import net.thorminate.hotpotato.server.network.CountdownPayload;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.network.chat.Component.translatable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotPotatoManager {
    /**
     * Simply returns the current hot potato player UUID, either from memory or from an NBT tag if not initialised to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The current hot potato player UUID, may be null.
     */
    public static UUID getCurrentHotPotato(@NotNull MinecraftServer server) {
        ServerLevel world = server.findRespawnDimension();
        return StorageManager.get(world).getCurrentHotPotato();
    }

    /**
     * Returns the current countdown in seconds, either from memory or from an NBT tag if not initialised to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The countdown in seconds, negative numbers are considered null.
     */
    public static int getCountdown(@NotNull MinecraftServer server) {
        ServerLevel world = server.findRespawnDimension();
        return StorageManager.get(world).getCountdown();
    }

    /**
     * Sets the current hot potato player UUID, gets written to an NBT tag on world save.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param playerUuid The player UUID that will override the current hot potato, may be null.
     */
    public static void setCurrentHotPotato(@NotNull MinecraftServer server, @Nullable UUID playerUuid) {
        if (playerUuid != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            if (player != null) {
                player.displayClientMessage(translatable("hot-potato.you_are_hot_potato").withStyle(ChatFormatting.RED), false);
            }
        }

        ServerLevel world = server.findRespawnDimension();
        StorageManager.get(world).setCurrentHotPotato(playerUuid);
    }

    /**
     * Sets the countdown in seconds, gets written to an NBT tag on world save.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param time The countdown in seconds to override the current countdown, will be considered as null if negative.
     */
    public static void setCountdown(@NotNull MinecraftServer server, int time) {
        ServerLevel world = server.findRespawnDimension();
        StorageManager.get(world).setCountdown(time);
    }

    /**
     * Sends a Packet to all players that contains the current countdown so they can update their UI.
     * @param server The server where the game is running, is used to get the player list, may not be null.
     */
    public static void syncWithClients(@NotNull MinecraftServer server) {
        if (getCurrentHotPotato(server) != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (UUID.fromString(player.getStringUUID()).equals(getCurrentHotPotato(server))) {
                    ServerPlayNetworking.send(player, new CountdownPayload(getCountdown(server)));
                } else {
                    ServerPlayNetworking.send(player, new CountdownPayload(-1));
                }
            }
        } else {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, new CountdownPayload(-1));
            }
        }
    }

    public static int startGame(MinecraftServer server, int time, ServerPlayer player) {
        if (getCountdown(server) > 0 || getCurrentHotPotato(server) != null) {
            return 0;
        }

        boolean gameStartStatus = true;

        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (players.isEmpty()) {
            server.sendSystemMessage(translatable("error.hot-potato.no_players"));
            gameStartStatus = false;
        }

        // If player is not provided, pick a random player.
        if (player == null) {
            player = players.get(new Random().nextInt(players.size()));
            // If player is still null, something went wrong.
            if (player == null) {
                server.sendSystemMessage(translatable("error.hot-potato.no_players"));
                gameStartStatus = false;
            }
        }

        if (time <= 0) {
            time = 30;
        }

        setCurrentHotPotato(server, player.getUUID());
        setCountdown(server, time);

        Timer.startTimer(server);

        if (gameStartStatus) {
            server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.0F));
            return SINGLE_SUCCESS;
        } else return 0;  // Command executed successfully, return 1 (success code)
    }
}
