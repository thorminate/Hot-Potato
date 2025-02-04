package net.thorminate.hotpotato;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.thorminate.hotpotato.client.network.RequestHotPotatoPayload;
import net.thorminate.hotpotato.server.HotPotatoGame;
import net.thorminate.hotpotato.server.network.HotPotatoPayload;
import net.thorminate.hotpotato.server.command.HotPotatoStartCommand;
import net.thorminate.hotpotato.server.command.HotPotatoStopCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotPotato implements ModInitializer {
	public static final String MOD_ID = "hot-potato";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static void receiveDataRequest(
			RequestHotPotatoPayload payload,
			ServerPlayNetworking.Context context
	) {
		HotPotatoGame.syncDataWithPlayers(context.server());
	}

	@Override
	public void onInitialize() {
		// First, register the commands
		CommandRegistrationCallback.EVENT.register((
				dispatcher,
				registryAccess,
				environment
		) -> {
			HotPotatoStartCommand.register(dispatcher);
			HotPotatoStopCommand.register(dispatcher);
		});

		PayloadTypeRegistry.playS2C().register(HotPotatoPayload.ID, HotPotatoPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestHotPotatoPayload.ID, RequestHotPotatoPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestHotPotatoPayload.ID, HotPotato::receiveDataRequest);

		ServerLifecycleEvents.SERVER_STARTED.register(HotPotatoGame::resumeHotPotato);
		ServerLifecycleEvents.SERVER_STOPPING.register((server -> HotPotatoGame.pauseHotPotato()));

		UseEntityCallback.EVENT.register((
				player,
				world,
				hand,
				entity,
				hitResult
		) -> HotPotatoGame.onUseEntity(player, world, entity));
	}
}