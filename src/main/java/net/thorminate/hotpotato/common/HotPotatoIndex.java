package net.thorminate.hotpotato.common;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.common.storage.WorldDataManager;
import net.thorminate.hotpotato.network.HotPotatoPayload;
import net.thorminate.hotpotato.server.logic.HotPotatoTimer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class HotPotatoIndex {
    public static final UUID NULLISH_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static boolean stopHotPotato(MinecraftServer server) {
        HotPotatoTimer.stopTimer();
        setCurrentHotPotato(server, null);
        setCountdown(server, -1);
        syncDataWithPlayers(server);
        return true;
    }
    public static boolean startHotPotato(MinecraftServer server) {
        if (getCurrentHotPotato(server) != null) {
            server.sendMessage(Text.literal("Unable to start, Hot potato already started!"));
            return false;
        }

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            server.sendMessage(Text.literal("Unable to start, No player to give Hot potato to!"));
            return false;
        }

        // Select a random player
        ServerPlayerEntity randomPlayer = players.get(new Random().nextInt(players.size()));

        if (randomPlayer == null) {
            server.sendMessage(Text.literal("Unable to start, No player to give Hot potato to!"));
            return false;
        }

        HotPotato.LOGGER.info("Hot potato given to someone...");
        setCurrentHotPotato(server, randomPlayer.getUuid());

        HotPotatoTimer.startTimer(10, server);
        return true;
    }
    public static UUID getCurrentHotPotato(@NotNull MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.HOT_POTATO_NBT_KEY).getCurrentHotPotato();
    }
    public static int getCountdown(@NotNull MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.HOT_POTATO_NBT_KEY).getCountdown();
    }
    public static void setCurrentHotPotato(@NotNull MinecraftServer server, @Nullable UUID playerUuid) {
        ServerWorld world = server.getOverworld();
        world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.HOT_POTATO_NBT_KEY).setCurrentHotPotato(playerUuid);
    }
    public static void setCountdown(@NotNull MinecraftServer server, int time) {
        ServerWorld world = server.getOverworld();
        world.getPersistentStateManager().getOrCreate(WorldDataManager.TYPE, WorldDataManager.HOT_POTATO_NBT_KEY).setCountdown(time);
    }
    public static void syncDataWithPlayers(@NotNull MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new HotPotatoPayload(getCurrentHotPotato(server), getCountdown(server)));
        }
    }
}
