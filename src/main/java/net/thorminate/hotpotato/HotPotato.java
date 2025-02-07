package net.thorminate.hotpotato;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

import static net.thorminate.hotpotato.server.HotPotatoGame.init;
import static org.slf4j.LoggerFactory.getLogger;

public class HotPotato implements ModInitializer {
	public static final String MOD_ID = "hot-potato";
	public static final Logger LOGGER = getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		init();
	}
}