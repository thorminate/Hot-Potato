package net.thorminate.hotpotato.server.logic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.server.HotPotatoManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static net.thorminate.hotpotato.server.HotPotatoManager.getCountdown;
import static net.thorminate.hotpotato.server.HotPotatoManager.getCurrentHotPotato;

public class LifecycleEvents {
    private static final Map<ServerPlayer, AtomicInteger> doomedCountdowns = new HashMap<>();

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register((server -> {
            if (getCurrentHotPotato(server) == null || getCountdown(server) <= 0) return;
            HotPotato.LOGGER.info("Hot potato resuming...");
            Timer.startTimer(server);
        }));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;

            if (HotPotatoManager.isDoomed(server, player.getUUID())) {
                server.execute(() -> startDoomCountdown(player, 5));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;

            // If they were in an active countdown, remove the countdown but keep them doomed
            doomedCountdowns.remove(player);
        });


        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (doomedCountdowns.isEmpty()) return;

            Iterator<Map.Entry<ServerPlayer, AtomicInteger>> iter = doomedCountdowns.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<ServerPlayer, AtomicInteger> entry = iter.next();
                ServerPlayer player = entry.getKey();
                AtomicInteger ticksRemaining = entry.getValue();

                ticksRemaining.decrementAndGet();

                // once per second, send title
                if (ticksRemaining.get() % 20 == 0 && ticksRemaining.get() > 0) {
                    int secondsLeft = ticksRemaining.get() / 20;
                    ChatFormatting color = switch (secondsLeft) {
                        case 5 -> ChatFormatting.WHITE;
                        case 4 -> ChatFormatting.YELLOW;
                        case 3 -> ChatFormatting.GOLD;
                        case 2 -> ChatFormatting.RED;
                        default -> ChatFormatting.DARK_RED;
                    };

                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(String.valueOf(secondsLeft)).withStyle(color)));
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 20, 0));
                    player.level().playSound(
                            null,
                            player.blockPosition(),
                            SoundEvents.NOTE_BLOCK_CHIME.value(),
                            SoundSource.PLAYERS,
                            1.0f,
                            secondsLeft == 1 ? 3.0f : 1.0f
                    );
                }

                // countdown finished
                if (ticksRemaining.get() <= 0) {
                    HotPotatoManager.explode(player);
                    HotPotatoManager.pardon(server, player.getUUID());
                    iter.remove(); // remove player from active countdowns
                }
            }
        });
    }
    public static void startDoomCountdown(ServerPlayer player, int seconds) {
        doomedCountdowns.put(player, new AtomicInteger(seconds * 20 + 1));
    }
}
