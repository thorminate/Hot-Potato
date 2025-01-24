package net.thorminate.hotpotato.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.thorminate.hotpotato.HotPotato;

import java.util.List;
import java.util.Random;

public class HotPotatoStartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("starthotpotato")
                .requires(source -> source.hasPermissionLevel(2)) // Admin-level permission
                .executes(HotPotatoStartCommand::startGame));
    }

    private static int startGame(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("No players available!"), false);
            return 0;
        }

        // Select a random player
        ServerPlayerEntity randomPlayer = players.get(new Random().nextInt(players.size()));

        HotPotato.LOGGER.info("Hot potato given to {}", randomPlayer.getNameForScoreboard());

        randomPlayer.sendMessage(Text.literal("Hot potato has been given to " + randomPlayer.getNameForScoreboard()), false);
        HotPotatoDataHandler.setCurrentHotPotato(randomPlayer);


        return Command.SINGLE_SUCCESS;
    }
}
