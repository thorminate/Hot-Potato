package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import net.thorminate.hotpotato.server.HotPotatoGame;

import static net.minecraft.text.Text.literal;

public class HotPotatoStopCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stop-hot-potato")
                .requires(source -> source.hasPermissionLevel(2)) // Admin-level permission
                .executes(HotPotatoStopCommand::stopGame));
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();

        boolean gameStopStatus = HotPotatoGame.stopHotPotato(server);

        if (gameStopStatus) {
            context.getSource().sendFeedback(() -> literal("Hot potato stopped!").formatted(Formatting.DARK_GREEN), true);
            return 0; // Command failed, return 0 (failure code)
        } else return Command.SINGLE_SUCCESS;  // Command executed successfully, return 1 (success code)
    }
}
