package net.thorminate.hotpotato.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

public class AllCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((
                dispatcher,
                registryAccess,
                environment
        ) -> registerCommands(dispatcher));
    }
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        StartCmd.register(dispatcher);
        StopCmd.register(dispatcher);
    }
}
