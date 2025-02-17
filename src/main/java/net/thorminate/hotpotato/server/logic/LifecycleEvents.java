package net.thorminate.hotpotato.server.logic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.thorminate.hotpotato.HotPotato;

import static net.thorminate.hotpotato.server.HotPotatoManager.getCountdown;
import static net.thorminate.hotpotato.server.HotPotatoManager.getCurrentHotPotato;

public class LifecycleEvents {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register((server -> {
            if (getCurrentHotPotato(server) == null || getCountdown(server) <= 0) return;
            HotPotato.LOGGER.info("Hot potato resuming...");
            Timer.startTimer(server);
        }));

        ServerLifecycleEvents.SERVER_STOPPING.register((server -> Timer.stopTimer()));
    }
}
