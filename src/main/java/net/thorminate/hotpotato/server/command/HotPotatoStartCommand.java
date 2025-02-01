package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.thorminate.hotpotato.server.HotPotatoGame;

public class HotPotatoStartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start-hot-potato")
                .requires(source -> source.hasPermissionLevel(2))// Admin-level permission
                .executes(context -> startGame(context, -1, null)) // No arguments
                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(0, 60))
                    .executes(context -> startGame(context, IntegerArgumentType.getInteger(context, "minutes") * 60, null))
                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, 59))
                    .executes(context -> startGame(context, IntegerArgumentType.getInteger(context, "minutes") * 60 + IntegerArgumentType.getInteger(context, "seconds"), null))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> startGame(context, IntegerArgumentType.getInteger(context, "minutes") * 60 + IntegerArgumentType.getInteger(context, "seconds"), EntityArgumentType.getPlayer(context, "player")))))
        ));
    }

    private static int startGame(CommandContext<ServerCommandSource> context, int time, ServerPlayerEntity player) {
        MinecraftServer server = context.getSource().getServer();

        boolean gameStartStatus = HotPotatoGame.startHotPotato(server, player, time);

        if (gameStartStatus) {
            ServerPlayerEntity commandRunner = context.getSource().getPlayer();
            if (commandRunner != null) commandRunner.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
            return Command.SINGLE_SUCCESS;
        } else return 0;  // Command executed successfully, return 1 (success code){
    }
}
