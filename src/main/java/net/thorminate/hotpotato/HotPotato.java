package net.thorminate.hotpotato;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.ActionResult;
import net.thorminate.hotpotato.common.HotPotatoIndex;
import net.thorminate.hotpotato.network.HotPotatoPayload;
import net.thorminate.hotpotato.server.command.HotPotatoStartCommand;
import net.thorminate.hotpotato.server.command.HotPotatoStopCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HotPotato implements ModInitializer {
	public static final String MOD_ID = "hot-potato";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// First, register the commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
				HotPotatoStartCommand.register(dispatcher);
				HotPotatoStopCommand.register(dispatcher);
			}
		);
		// Then, register the payload
		PayloadTypeRegistry.playS2C().register(HotPotatoPayload.ID, HotPotatoPayload.CODEC);

		UseEntityCallback.EVENT.register(((playerEntity, world, hand, entity, entityHitResult) -> {
			if (!entity.isPlayer()) {
				return ActionResult.FAIL;
			}
			if (!playerEntity.getUuid().equals(HotPotatoIndex.getCurrentHotPotato(Objects.requireNonNull(playerEntity.getServer())))) {
				return ActionResult.FAIL;
			}

			return ActionResult.SUCCESS;
		}));
	}
}