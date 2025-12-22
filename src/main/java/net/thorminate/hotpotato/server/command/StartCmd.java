package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.thorminate.hotpotato.server.HotPotatoManager;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.network.chat.Component.translatable;

public class StartCmd {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("start-hot-potato")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))// Admin-level permission
            .executes(context -> startGame(context, -1, null)) // No arguments
            .then(Commands.argument("minutes", integer(0, 60))
                .executes(context -> startGame(context, getInteger(context, "minutes") * 60, null))
                .then(Commands.argument("seconds", integer(0, 59))
                    .executes(
                            context -> startGame(
                                    context,
                                    getInteger(context, "minutes") * 60 + getInteger(context, "seconds"),
                                    null
                            )
                    )
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(
                                context -> startGame(
                                        context,
                                        getInteger(context, "minutes") * 60 + getInteger(context, "seconds"),
                                        getPlayer(context, "player")
                                )
                        )
                    )
                )
            )
        );
    }

    public static int startGame(CommandContext<CommandSourceStack> context, int time, ServerPlayer player) {
        MinecraftServer server = context.getSource().getServer();
        int res = HotPotatoManager.startGame(server, time, player);

        if (res == 0) {
            context.getSource().sendSuccess(() -> translatable("commands.start_hot_potato.already_started").withStyle(ChatFormatting.RED), true);
            return 0;
        }


        context.getSource().sendSuccess(() -> translatable("commands.start_hot_potato.success").withStyle(ChatFormatting.GOLD), true);
        return res;
    }
}
