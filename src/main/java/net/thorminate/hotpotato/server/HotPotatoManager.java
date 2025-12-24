package net.thorminate.hotpotato.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.thorminate.hotpotato.server.logic.Timer;
import net.thorminate.hotpotato.server.resources.DamageTypes;
import net.thorminate.hotpotato.server.storage.StorageManager;
import net.thorminate.hotpotato.server.network.CountdownPayload;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.network.chat.Component.translatable;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotPotatoManager {
    /**
     * Simply returns the current hot potato player UUID, either from memory or from an NBT tag if not initialized to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The current hot potato player UUID, may be null.
     */
    public static UUID getCurrentHotPotato(@NotNull MinecraftServer server) {
        ServerLevel world = server.findRespawnDimension();
        return StorageManager.get(world).getCurrentHotPotato();
    }

    /**
     * Returns the current countdown in seconds, either from memory or from an NBT tag if not initialized to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The countdown in seconds, negative numbers are considered null.
     */
    public static int getCountdown(@NotNull MinecraftServer server) {
        ServerLevel world = server.findRespawnDimension();
        return StorageManager.get(world).getCountdown();
    }

    /**
     * Returns a set of players who are doomed to die.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return A set of players who are doomed to die.
     */
    public static Set<UUID> getDoomedPlayers(@NotNull MinecraftServer server) {
        ServerLevel world = server.findRespawnDimension();
        return StorageManager.get(world).getDoomedPlayers();
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
     * Adds a player UUID to the set of doomed players.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param playerUuid The UUID of which to add to the set.
     */
    public static void doom(@NotNull MinecraftServer server, @Nullable UUID playerUuid) {
        ServerLevel world = server.findRespawnDimension();
        StorageManager.get(world).doom(playerUuid);
    }

    /**
     * Removes a player UUID from the set of doomed players.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param playerUuid The UUID to remove from the set.
     */
    public static void pardon(@NotNull MinecraftServer server, @Nullable UUID playerUuid) {
        ServerLevel world = server.findRespawnDimension();
        StorageManager.get(world).pardon(playerUuid);
    }

    /**
     * Clears the doomed player set.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     */
    public static void clearDoomed(@NotNull MinecraftServer server) {
        ServerLevel world = server.findRespawnDimension();
        StorageManager.get(world).clearDoomed();
    }


    /**
     * Checks if a player UUID has been set to be doomed.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param uuid The UUID to check for.
     * @return Whether the player is doomed or not.
     */
    public static boolean isDoomed(@NotNull MinecraftServer server, UUID uuid) {
        ServerLevel world = server.findRespawnDimension();
        return StorageManager.get(world).isDoomed(uuid);
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

    /**
     * Starts a Hot Potato game.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param time How many seconds the game should go on for, may be null.
     * @param player The player to become the bearer of the hot potato, may be null.
     * @return A return code from 0 (fail) or 1 (success).
     */
    public static int startGame(MinecraftServer server, @Nullable Integer time, @Nullable ServerPlayer player) {
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
            // filter out non-survival mode players
            List<ServerPlayer> survivalPlayers = players.stream()
                    .filter(p -> !p.isCreative() && !p.isSpectator())
                    .toList();

            if (!survivalPlayers.isEmpty()) {
                player = survivalPlayers.get(new Random().nextInt(survivalPlayers.size()));
            } else {
                server.sendSystemMessage(translatable("error.hot-potato.no_players"));
                gameStartStatus = false;
            }
        }

        if (time == null || time <= 0) {
            time = 30;
        }

        if (gameStartStatus) {
        assert player != null;

        setCurrentHotPotato(server, player.getUUID());
        setCountdown(server, time);

        Timer.startTimer(server);

        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.0F));
        ServerPlayer finalPlayer = player;
        players.forEach(p -> {
            if (p.getUUID().equals(finalPlayer.getUUID())) {
                p.connection.send(new ClientboundSetTitleTextPacket(translatable("hot-potato.title").withStyle(ChatFormatting.RED)));
            } else {
                p.connection.send(new ClientboundSetTitleTextPacket(translatable("hot-potato.safe").withStyle(ChatFormatting.AQUA)));
            }
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 5));
        });
            return SINGLE_SUCCESS; // Command executed successfully, return 1 (success code)
        } else return 0;
    }

    /**
     * Will kill a player with some flashy effects.
     * @param player Player to kill.
     */
    public static void explode(@NotNull ServerPlayer player) {
        ServerLevel world = player.level();

        // Spawn lightning bolt
        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
        lightning.setPos(player.position());
        player.level().addFreshEntity(lightning);

        // Deal damage
        DamageSource source = new DamageSource(
                player.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .get(DamageTypes.HOT_POTATO_EXPLODED.identifier()).orElseThrow()
        );

        player.hurtServer(world, source, 1_000_000.0f);
    }
}
