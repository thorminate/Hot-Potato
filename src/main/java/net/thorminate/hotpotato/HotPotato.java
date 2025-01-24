package net.thorminate.hotpotato;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.thorminate.hotpotato.utils.HotPotatoStartCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotPotato implements ModInitializer {
	public static final String MOD_ID = "hot-potato";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// First, register the start command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				HotPotatoStartCommand.register(dispatcher)
		);
	}
}