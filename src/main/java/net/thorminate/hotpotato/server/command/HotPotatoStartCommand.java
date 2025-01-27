package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thorminate.hotpotato.common.HotPotatoIndex;

public class HotPotatoStartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("starthotpotato")
                .requires(source -> source.hasPermissionLevel(2)) // Admin-level permission
                .executes(HotPotatoStartCommand::startGame));
    }

    private static int startGame(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();

        boolean gameStartStatus = HotPotatoIndex.startHotPotato(server);

        if (gameStartStatus) {
            context.getSource().sendFeedback(() -> Text.literal("Hot potato started!").formatted(Formatting.RED), true);
            return Command.SINGLE_SUCCESS;
        } else return 0;  // Command executed successfully, return 1 (success code){
    }
}
