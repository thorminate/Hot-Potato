package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING;
import static net.minecraft.text.Text.translatable;
import static net.minecraft.util.Formatting.*;
import static net.thorminate.hotpotato.server.HotPotatoGame.*;

public class HotPotatoStartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start-hot-potato")
                .requires(source -> source.hasPermissionLevel(2))// Admin-level permission
                .executes(context -> startGame(context, -1, null)) // No arguments
                .then(CommandManager.argument("minutes", integer(0, 60))
                    .executes(context -> startGame(context, getInteger(context, "minutes") * 60, null))
                .then(CommandManager.argument("seconds", integer(0, 59))
                    .executes(context -> startGame(context, getInteger(context, "minutes") * 60 + getInteger(context, "seconds"), null))
                .then(CommandManager.argument("player", player())
                    .executes(context -> startGame(context, getInteger(context, "minutes") * 60 + getInteger(context, "seconds"), getPlayer(context, "player")))))
        ));
    }

    private static int startGame(CommandContext<ServerCommandSource> context, int time, ServerPlayerEntity player) {
        MinecraftServer server = context.getSource().getServer();

        if (getCountdown(server) > 0 || getCurrentHotPotato(server) != null) {
            context.getSource().sendFeedback(() -> translatable("commands.start_hot_potato.already_started").formatted(RED), true);
            return 0; // Command failed, return 0 (failure code)
        }

        boolean gameStartStatus = start(server, player, time);

        if (gameStartStatus) {
            context.getSource().sendFeedback(() -> translatable("commands.start_hot_potato.success").formatted(GOLD), true);
            ServerPlayerEntity commandRunner = context.getSource().getPlayer();
            if (commandRunner != null) commandRunner.playSound(BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
            return SINGLE_SUCCESS;
        } else return 0;  // Command executed successfully, return 1 (success code){
    }
}
