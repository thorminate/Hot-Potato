package net.thorminate.hotpotato.server.logic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LightningBolt;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.server.command.StopCmd;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.network.chat.Component.translatable;
import static net.thorminate.hotpotato.HotPotato.LOGGER;
import static net.thorminate.hotpotato.server.HotPotatoManager.*;

import java.util.concurrent.*;

public class Timer {
    private static ScheduledExecutorService SCHEDULER;
    private static ServerPlayer currentHotPotato;
    private static int timeLeft = -1;

    private static final ResourceKey<DamageType> HOT_POTATO_EXPLODED = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(HotPotato.MOD_ID, "hot_potato_exploded")
    );

    public static void startTimer(MinecraftServer server) {
        if (SCHEDULER != null) SCHEDULER.shutdown();
        SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Hot-Potato-Timer");
            return thread;
        });

        SCHEDULER.scheduleAtFixedRate(() -> {
            timeLeft = getCountdown(server);
            currentHotPotato = server.getPlayerList().getPlayer(getCurrentHotPotato(server));

            if (timeLeft <= 1) {
                if (currentHotPotato != null) {
                    eliminate(currentHotPotato, server.getLevel(currentHotPotato.level().dimension()));
                } else {
                    LOGGER.warn("The hot potato was not found! Make sure the player is online.");
                }
                server.getPlayerList().broadcastSystemMessage(translatable("hot-potato.exploded").withStyle(ChatFormatting.RED), true);
                StopCmd.stop(server);
            }

            timeLeft--;
            setCountdown(server, timeLeft);
            syncWithClients(server);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopTimer() {
        if (SCHEDULER != null) SCHEDULER.shutdown();
    }

    private static void eliminate(@NotNull ServerPlayer player, ServerLevel world) {
        // Spawn lightning bolt
        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
        lightning.setPos(player.position());
        player.level().addFreshEntity(lightning);

        // Deal damage
        DamageSource source = new DamageSource(
                player.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .get(HOT_POTATO_EXPLODED.identifier()).get()
        );

        player.hurtServer(world, source, 1_000_000.0f);
    }
}
