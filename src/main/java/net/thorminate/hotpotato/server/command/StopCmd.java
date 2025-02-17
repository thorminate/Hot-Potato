package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import net.thorminate.hotpotato.server.HotPotatoManager;
import net.thorminate.hotpotato.server.logic.Timer;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.text.Text.translatable;

public class StopCmd {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stop-hot-potato")
                .requires(source -> source.hasPermissionLevel(2)) // Admin-level permission
                .executes(StopCmd::stopGame)
        );
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();

        boolean gameStopStatus = stop(server);

        if (gameStopStatus) {
            context.getSource().sendFeedback(() -> translatable("commands.stop_hot_potato.success").formatted(Formatting.DARK_GREEN), true);
            return Command.SINGLE_SUCCESS; // Command succeeded, return 0 (failure code)
        } else {
            context.getSource().sendFeedback(() -> translatable("commands.stop_hot_potato.failure").formatted(Formatting.RED), true);
            return 0;  // Command executed successfully, return 1 (success code)
        }
    }

    public static boolean stop(@NotNull MinecraftServer server) {
        Timer.stopTimer();
        HotPotatoManager.setCurrentHotPotato(server, null);
        HotPotatoManager.setCountdown(server, -1);
        HotPotatoManager.syncWithClients(server);
        return true;
    }
}
