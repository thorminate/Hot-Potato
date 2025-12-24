package net.thorminate.hotpotato;

import net.fabricmc.api.ModInitializer;
import net.thorminate.hotpotato.server.command.AllCommands;
import net.thorminate.hotpotato.server.logic.LifecycleEvents;
import net.thorminate.hotpotato.server.logic.PlayerEvents;
import net.thorminate.hotpotato.server.logic.Timer;
import net.thorminate.hotpotato.server.network.Networking;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class HotPotato implements ModInitializer {
	public static final String MOD_ID = "hot-potato";
	public static final Logger LOGGER = getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// First, register the commands
		AllCommands.register();
		Networking.register();
		LifecycleEvents.register();
		PlayerEvents.register();
		Timer.register();
	}
}