package net.thorminate.hotpotato.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

import net.thorminate.hotpotato.server.storage.StorageManager;
import net.thorminate.hotpotato.server.network.CountdownPayload;

import static net.minecraft.text.Text.translatable;

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
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(StorageManager.TYPE, StorageManager.PLAYER_KEY).getCurrentHotPotato();
    }

    /**
     * Returns the current countdown in seconds, either from memory or from an NBT tag if not initialised to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The countdown in seconds, negative numbers are considered null.
     */
    public static int getCountdown(@NotNull MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(StorageManager.TYPE, StorageManager.COUNTDOWN_KEY).getCountdown();
    }

    /**
     * Sets the current hot potato player UUID, gets written to an NBT tag on world save.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param playerUuid The player UUID that will override the current hot potato, may be null.
     */
    public static void setCurrentHotPotato(@NotNull MinecraftServer server, @Nullable UUID playerUuid) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
        if (player != null) {
            player.sendMessage(translatable("hot-potato.you_are_hot_potato").formatted(Formatting.RED), false);
        }
        ServerWorld world = server.getOverworld();
        world.getPersistentStateManager().getOrCreate(StorageManager.TYPE, StorageManager.PLAYER_KEY).setCurrentHotPotato(playerUuid);
    }

    /**
     * Sets the countdown in seconds, gets written to an NBT tag on world save.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param time The countdown in seconds to override the current countdown, will be considered as null if negative.
     */
    public static void setCountdown(@NotNull MinecraftServer server, int time) {
        ServerWorld world = server.getOverworld();
        world.getPersistentStateManager().getOrCreate(StorageManager.TYPE, StorageManager.COUNTDOWN_KEY).setCountdown(time);
    }

    /**
     * Sends a Packet to all players that contains the current countdown so they can update their UI.
     * @param server The server where the game is running, is used to get the player list, may not be null.
     */
    public static void syncWithClients(@NotNull MinecraftServer server) {
        if (getCurrentHotPotato(server) != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getUuid().equals(getCurrentHotPotato(server))) {
                    ServerPlayNetworking.send(player, new CountdownPayload(getCountdown(server)));
                } else {
                    ServerPlayNetworking.send(player, new CountdownPayload(-1));
                }
            }
        } else {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new CountdownPayload(-1));
            }
        }
    }
}
