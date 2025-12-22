package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.thorminate.hotpotato.server.HotPotatoManager;
import net.thorminate.hotpotato.server.logic.Timer;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.network.chat.Component.translatable;

public class StopCmd {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stop-hot-potato")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) // Admin-level permission
                .executes(StopCmd::stopGame)
        );
    }

    private static int stopGame(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();

        boolean gameStopStatus = stop(server);

        if (gameStopStatus) {
            context.getSource().sendSuccess(() -> translatable("commands.stop_hot_potato.success").withStyle(ChatFormatting.DARK_GREEN), true);
            return Command.SINGLE_SUCCESS; // Command succeeded, return 0 (failure code)
        } else {
            context.getSource().sendSuccess(() -> translatable("commands.stop_hot_potato.failure").withStyle(ChatFormatting.RED), true);
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
