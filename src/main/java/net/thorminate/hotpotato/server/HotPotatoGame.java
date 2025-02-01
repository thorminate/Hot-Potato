package net.thorminate.hotpotato.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.thorminate.hotpotato.server.storage.WorldDataManager;
import net.thorminate.hotpotato.server.network.HotPotatoPayload;

import static net.thorminate.hotpotato.server.logic.HotPotatoTimer.startTimer;
import static net.thorminate.hotpotato.server.logic.HotPotatoTimer.stopTimer;
import static net.thorminate.hotpotato.HotPotato.LOGGER;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.text.Text.literal;
import static net.minecraft.sound.SoundCategory.MASTER;
import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send;
import static net.minecraft.particle.ParticleTypes.FLAME;
import static net.minecraft.sound.SoundEvents.ENTITY_SILVERFISH_STEP;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotPotatoGame {
    /**
     * Terminates all processes related to the hot potato game and nullifies related data.
     * @param server The server where the game should stop, may not be null.
     * @return True if the game stopped successfully, false otherwise.
     */
    public static boolean stopHotPotato(@NotNull MinecraftServer server) {
        stopTimer();
        setCurrentHotPotato(server, null);
        setCountdown(server, -1);
        syncDataWithPlayers(server);
        return true;
    }

    /**
     * Starts the HotPotato game. If a player is not provided, a random player will be picked.
     * @param server The server where the game should start, may not be null.
     * @param player The player to give the hot potato to, may be null.
     * @param seconds The countdown in seconds, place negative value to consider itself null and use default value.
     * @return True if the game started successfully, false otherwise.
     */
    public static boolean startHotPotato(@NotNull MinecraftServer server, @Nullable ServerPlayerEntity player, Integer seconds) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            server.sendMessage(literal("Unable to start, No player to give Hot potato to!"));
            return false;
        }

        // If player is not provided, pick a random player.
        if (player == null) {
            player = players.get(new Random().nextInt(players.size()));
            // If player is still null, something went wrong.
            if (player == null) {
                server.sendMessage(literal("Unable to start, No player to give Hot potato to!"));
                return false;
            }
        }

        if (seconds <= 0) {
            seconds = 30;
        }

        LOGGER.info("Hot potato given to someone...");
        setCurrentHotPotato(server, player.getUuid());
        setCountdown(server, seconds);

        startTimer(server);
        return true;
    }

    /**
     * Pauses the HotPotato game by stopping all processes related to hot potato but does not nullify related data.
     */
    public static void pauseHotPotato() {
        stopTimer();
    }

    /**
     * Resumes the HotPotato game by restarting all processes related to hot potato.
     * @param server The server where the game should resume, may not be null.
     */
    public static void resumeHotPotato(@NotNull MinecraftServer server) {
        if (getCurrentHotPotato(server) == null || getCountdown(server) <= 0) {
            server.sendMessage(literal("Unable to resume, Hot potato not started!"));
            return;
        }
        LOGGER.info("Hot potato resuming...");
        startTimer(server);
    }

    /**
     * Code that is run when a player right-clicks an entity.
     * @param player The player that right-clicked the entity, may not be null.
     * @param world The world in which the entity and player is, may not be null.
     * @param entity The entity that was right-clicked, may not be null.
     * @return Whether the action was successful, via ActionResult.
     */
    public static ActionResult onUseEntity(@NotNull PlayerEntity player, @NotNull World world, @NotNull Entity entity) {
        if (world.isClient()) return ActionResult.PASS;

        MinecraftServer server = entity.getServer();
        if (server == null) {
            return ActionResult.FAIL;
        }

        if (!entity.isPlayer()) return ActionResult.FAIL;
        if (!getCurrentHotPotato(server).equals(player.getUuid())) return ActionResult.FAIL;
        if (getCountdown(server) <= 0) return ActionResult.FAIL;

        ServerWorld serverWorld = server.getWorld(world.getRegistryKey());

        if (serverWorld == null) return ActionResult.FAIL;

        serverWorld.spawnParticles(FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.5);
        serverWorld.playSound(entity, entity.getBlockPos(), ENTITY_SILVERFISH_STEP, MASTER, 1, 1);

        setCurrentHotPotato(server, entity.getUuid());

        return ActionResult.SUCCESS;
    }

    /**
     * Simply returns the current hot potato player UUID, either from memory or from an NBT tag if not initialised to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The current hot potato player UUID, may be null.
     */
    public static UUID getCurrentHotPotato(@NotNull MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.PLAYER_KEY).getCurrentHotPotato();
    }

    /**
     * Returns the current countdown in seconds, either from memory or from an NBT tag if not initialised to memory yet.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @return The countdown in seconds, negative numbers are considered null.
     */
    public static int getCountdown(@NotNull MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.COUNTDOWN_KEY).getCountdown();
    }

    /**
     * Sets the current hot potato player UUID, gets written to an NBT tag on world save.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param playerUuid The player UUID that will override the current hot potato, may be null.
     */
    public static void setCurrentHotPotato(@NotNull MinecraftServer server, @Nullable UUID playerUuid) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
        if (player != null) {
            player.sendMessage(literal("You have become the hot potato! Right click on another player to give them the hot potato.").formatted(RED), false);
        }
        ServerWorld world = server.getOverworld();
        world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.PLAYER_KEY).setCurrentHotPotato(playerUuid);
    }

    /**
     * Sets the countdown in seconds, gets written to an NBT tag on world save.
     * @param server The server where the game is running, is used to get overworld (nbt data location), may not be null.
     * @param time The countdown in seconds to override the current countdown, will be considered as null if negative.
     */
    public static void setCountdown(@NotNull MinecraftServer server, int time) {
        ServerWorld world = server.getOverworld();
        world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.COUNTDOWN_KEY).setCountdown(time);
    }

    /**
     * Sends a Packet to all players that contains the current countdown so they can update their UI.
     * @param server The server where the game is running, is used to get the player list, may not be null.
     */
    public static void syncDataWithPlayers(@NotNull MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            send(player, new HotPotatoPayload(getCountdown(server)));
        }
    }
}
