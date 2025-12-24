package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.server.HotPotatoManager;
import net.thorminate.hotpotato.server.logic.Timer;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.network.chat.Component.translatable;
import static net.thorminate.hotpotato.server.HotPotatoManager.getCountdown;
import static net.thorminate.hotpotato.server.HotPotatoManager.getCurrentHotPotato;

public class MainCmd {
    private static int countdownTicks = -1;
    private static ServerPlayer countdownStarter;
    private static int gameTime = -1;
    private static ServerPlayer gameStarter;
    private static int lastSentCountdownNumber = -1;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (countdownTicks < 0) return; // no countdown running

            countdownTicks--;

            // once per second
            if (countdownTicks % 20 != 0) return;

            int secondsLeft = countdownTicks / 20;

            if (secondsLeft > 0) {
                if (secondsLeft == lastSentCountdownNumber) return;

                ChatFormatting color = switch (secondsLeft) {
                    case 5 -> ChatFormatting.WHITE;
                    case 4 -> ChatFormatting.YELLOW;
                    case 3 -> ChatFormatting.GOLD;
                    case 2 -> ChatFormatting.RED;
                    default -> ChatFormatting.DARK_RED;
                };

                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(String.valueOf(secondsLeft)).withStyle(color)));
                    p.connection.send(new ClientboundSetTitlesAnimationPacket(0, 20, 0));
                    p.level().playSound(
                            null,
                            p.blockPosition(),
                            SoundEvents.NOTE_BLOCK_CHIME.value(),
                            SoundSource.PLAYERS,
                            1.0f,
                            secondsLeft == 1 ? 3.0f : 1.0f
                    );
                }

                lastSentCountdownNumber = secondsLeft;
                return;
            }

            // Countdown finished, start the game
            int res = HotPotatoManager.startGame(server, gameTime, gameStarter);

            if (countdownStarter != null) {
                if (res == 0) {
                    countdownStarter.sendSystemMessage(translatable("commands.start_hot_potato.already_started").withStyle(ChatFormatting.RED), false);
                }
                countdownStarter = null;
            }

            countdownTicks = -1; // stop countdown
        });

        dispatcher.register(Commands.literal("hot-potato")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) // Admin-level permission
                        .then(Commands.literal("start")
                                .executes(context -> startGame(context, 30, null)) // No arguments
                                .then(Commands.argument("minutes", IntegerArgumentType.integer(0, 60))
                                        .executes(context -> startGame(context, IntegerArgumentType.getInteger(context, "minutes") * 60, null))
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 59))
                                                .executes(
                                                        context -> startGame(
                                                                context,
                                                                IntegerArgumentType.getInteger(context, "minutes") * 60 + IntegerArgumentType.getInteger(context, "seconds"),
                                                                null
                                                        )
                                                )
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(
                                                                context -> startGame(
                                                                        context,
                                                                        IntegerArgumentType.getInteger(context, "minutes") * 60 + IntegerArgumentType.getInteger(context, "seconds"),
                                                                        EntityArgument.getPlayer(context, "player")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ).then(Commands.literal("stop")
                                .executes(context -> stopGame(context, false))
                                .then(Commands.argument("pardonDoomed", BoolArgumentType.bool())
                                        .executes(
                                                context -> stopGame(
                                                        context,
                                                        BoolArgumentType.getBool(context, "pardonDoomed")
                                                )
                                        )
                                )
                )

        );
    }

    // Command method
    public static int startGame(CommandContext<CommandSourceStack> context, int time, ServerPlayer player) {
        if (countdownTicks >= 0) {
            context.getSource().sendFailure(translatable("commands.start_hot_potato.countdown_already_started").withStyle(ChatFormatting.RED));
            return 0;
        }

        MinecraftServer server = context.getSource().getServer();

        if (getCountdown(server) > 0 || getCurrentHotPotato(server) != null) {
            context.getSource().sendFailure(translatable("commands.start_hot_potato.already_started").withStyle(ChatFormatting.RED));
            return 0;
        }

        HotPotato.LOGGER.info("Starting hot-potato");

        countdownTicks = 5 * 20 + 1;
        countdownStarter = context.getSource().getPlayer();
        gameTime = time;
        gameStarter = player;

        return 1;
    }

    private static int stopGame(CommandContext<CommandSourceStack> context, boolean pardonDoomed) {
        MinecraftServer server = context.getSource().getServer();

        boolean gameStopStatus = stop(server, pardonDoomed);

        if (gameStopStatus) {
            context.getSource().sendSuccess(
                    () -> translatable("commands.stop_hot_potato.success").withStyle(ChatFormatting.DARK_GREEN),
                    true
            );
            return Command.SINGLE_SUCCESS; // Command succeeded
        } else {
            context.getSource().sendSuccess(
                    () -> translatable("commands.stop_hot_potato.failure").withStyle(ChatFormatting.RED),
                    true
            );
            return 0;  // Command executed but failed
        }
    }


    public static boolean stop(@NotNull MinecraftServer server, boolean pardonDoomed) {
        // Stop countdown
        Timer.stopTimer();
        countdownTicks = -1;
        countdownStarter = null;
        gameTime = -1;
        gameStarter = null;
        lastSentCountdownNumber = -1;

        if (pardonDoomed) {
            HotPotatoManager.clearDoomed(server);
        }

        // Clear hot potato state
        HotPotatoManager.setCurrentHotPotato(server, null);
        HotPotatoManager.setCountdown(server, -1);

        // Sync clients
        HotPotatoManager.syncWithClients(server);

        return true;
    }
}
