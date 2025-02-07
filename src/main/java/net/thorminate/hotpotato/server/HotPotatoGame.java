package net.thorminate.hotpotato.server;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import net.thorminate.hotpotato.client.network.RequestHotPotatoPayload;
import net.thorminate.hotpotato.server.command.HotPotatoStartCommand;
import net.thorminate.hotpotato.server.command.HotPotatoStopCommand;
import net.thorminate.hotpotato.server.logic.HotPotatoCooldownManager;
import net.thorminate.hotpotato.server.logic.HotPotatoTimer;
import net.thorminate.hotpotato.server.storage.WorldDataManager;
import net.thorminate.hotpotato.server.network.HotPotatoPayload;

import static net.thorminate.hotpotato.HotPotato.LOGGER;
import static net.minecraft.text.Text.translatable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotPotatoGame {
    /**
     * Initializes the commands, packets, and events for the hot potato game.
     */
    public static void init() {
        // First, register the commands
        CommandRegistrationCallback.EVENT.register((
                dispatcher,
                registryAccess,
                environment
        ) -> {
            HotPotatoStartCommand.register(dispatcher);
            HotPotatoStopCommand.register(dispatcher);
        });

        PayloadTypeRegistry.playS2C().register(HotPotatoPayload.ID, HotPotatoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestHotPotatoPayload.ID, RequestHotPotatoPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestHotPotatoPayload.ID, (payload, context) -> sync(context.server()));

        ServerLifecycleEvents.SERVER_STARTED.register(HotPotatoGame::resume);
        ServerLifecycleEvents.SERVER_STOPPING.register((server -> pause()));

        UseEntityCallback.EVENT.register((
                player,
                world,
                hand,
                entity,
                hitResult
        ) -> useEntity(player, world, entity));
    }

    /**
     * Terminates all processes related to the hot potato game and nullifies related data.
     * @param server The server where the game should stop, may not be null.
     * @return True if the game stopped successfully, false otherwise.
     */
    public static boolean stop(@NotNull MinecraftServer server) {
        HotPotatoTimer.stopTimer();
        setCurrentHotPotato(server, null);
        setCountdown(server, -1);
        sync(server);
        return true;
    }

    /**
     * Starts the HotPotato game. If a player is not provided, a random player will be picked.
     * @param server The server where the game should start, may not be null.
     * @param player The player to give the hot potato to, may be null.
     * @param seconds The countdown in seconds, place negative value to consider itself null and use default value.
     * @return True if the game started successfully, false otherwise.
     */
    public static boolean start(@NotNull MinecraftServer server, @Nullable ServerPlayerEntity player, Integer seconds) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            server.sendMessage(translatable("error.hot-potato.no_players"));
            return false;
        }

        // If player is not provided, pick a random player.
        if (player == null) {
            player = players.get(new Random().nextInt(players.size()));
            // If player is still null, something went wrong.
            if (player == null) {
                server.sendMessage(translatable("error.hot-potato.no_players"));
                return false;
            }
        }

        if (seconds <= 0) {
            seconds = 30;
        }

        LOGGER.info("Hot potato given to someone...");
        setCurrentHotPotato(server, player.getUuid());
        setCountdown(server, seconds);

        HotPotatoTimer.startTimer(server);
        return true;
    }

    /**
     * Pauses the HotPotato game by stopping all processes related to hot potato but does not nullify related data.
     */
    public static void pause() {
        HotPotatoTimer.stopTimer();
    }

    /**
     * Resumes the HotPotato game by restarting all processes related to hot potato.
     * @param server The server where the game should resume, may not be null.
     */
    public static void resume(@NotNull MinecraftServer server) {
        if (getCurrentHotPotato(server) == null || getCountdown(server) <= 0) return;
        LOGGER.info("Hot potato resuming...");
        HotPotatoTimer.startTimer(server);
    }

    /**
     * Code that is run when a player right-clicks an entity.
     * @param player The player that right-clicked the entity, may not be null.
     * @param world The world in which the entity and player is, may not be null.
     * @param entity The entity that was right-clicked, may not be null.
     * @return Whether the action was successful, via ActionResult.
     */
    public static ActionResult useEntity(@NotNull PlayerEntity player, @NotNull World world, @NotNull Entity entity) {
        if (world.isClient()) return ActionResult.PASS;

        MinecraftServer server = entity.getServer();
        if (server == null) return ActionResult.PASS;

        ServerWorld serverWorld = server.getWorld(world.getRegistryKey());
        if (serverWorld == null) return ActionResult.PASS;

        if (!entity.isPlayer()) return ActionResult.PASS;
        if (getCurrentHotPotato(server) == null) return ActionResult.PASS;
        if (!getCurrentHotPotato(server).equals(player.getUuid())) return ActionResult.PASS;
        if (getCountdown(server) <= 0) return ActionResult.PASS;

        if (HotPotatoCooldownManager.isOnCooldown()) {
            player.sendMessage(translatable("hot-potato.cooldown_message").formatted(Formatting.BLUE), true);
            return ActionResult.PASS;
        }



        serverWorld.spawnParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.5);
        serverWorld.playSound(entity, entity.getBlockPos(), SoundEvents.ENTITY_SILVERFISH_STEP, SoundCategory.MASTER, 1, 1);

        setCurrentHotPotato(server, entity.getUuid());

        player.swingHand(player.getActiveHand(), true);
        HotPotatoCooldownManager.setCooldown();

        return ActionResult.SUCCESS;
    }

    public static void eliminate(@NotNull ServerPlayerEntity player, ServerWorld world) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, player.getWorld());
        lightning.setPosition(player.getPos());
        player.getWorld().spawnEntity(lightning);
        player.kill(world);
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
            player.sendMessage(translatable("hot-potato.you_are_hot_potato").formatted(Formatting.RED), false);
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
    public static void sync(@NotNull MinecraftServer server) {
        if (getCurrentHotPotato(server) != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getUuid().equals(getCurrentHotPotato(server))) {
                    ServerPlayNetworking.send(player, new HotPotatoPayload(getCountdown(server)));
                } else {
                    ServerPlayNetworking.send(player, new HotPotatoPayload(-1));
                }
            }
        } else {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new HotPotatoPayload(-1));
            }
        }
    }
}
