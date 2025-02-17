package net.thorminate.hotpotato;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.thorminate.hotpotato.client.network.RequestHotPotatoPayload;
import net.thorminate.hotpotato.server.command.HotPotatoStartCommand;
import net.thorminate.hotpotato.server.command.HotPotatoStopCommand;
import net.thorminate.hotpotato.server.logic.HotPotatoCooldownManager;
import net.thorminate.hotpotato.server.logic.HotPotatoTimer;
import net.thorminate.hotpotato.server.network.HotPotatoPayload;
import org.slf4j.Logger;

import static net.minecraft.text.Text.translatable;
import static net.thorminate.hotpotato.server.HotPotatoManager.*;
import static org.slf4j.LoggerFactory.getLogger;

public class HotPotato implements ModInitializer {
	public static final String MOD_ID = "hot-potato";
	public static final Logger LOGGER = getLogger(MOD_ID);

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

		ServerPlayNetworking.registerGlobalReceiver(
			RequestHotPotatoPayload.ID,
			(payload, context) -> syncWithClients(context.server())
		);

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			if (getCurrentHotPotato(server) == null || getCountdown(server) <= 0) return;
			LOGGER.info("Hot potato resuming...");
			HotPotatoTimer.startTimer(server);
		}));

		ServerLifecycleEvents.SERVER_STOPPING.register((server -> HotPotatoTimer.stopTimer()));

		UseEntityCallback.EVENT.register((
				player,
				world,
				hand,
				entity,
				hitResult
		) -> useEntity(player, world, entity));
	}

	private static ActionResult useEntity(PlayerEntity player, World world, Entity entity) {
		if (world.isClient()) return ActionResult.PASS;

		MinecraftServer server = entity.getServer();
		if (server == null) return ActionResult.PASS;

		ServerWorld serverWorld = server.getWorld(world.getRegistryKey());
		if (serverWorld == null) return ActionResult.PASS;

		if (!entity.isPlayer()) return ActionResult.PASS;
		if (getCurrentHotPotato(server) == null) return ActionResult.PASS;
		if (!getCurrentHotPotato(server).equals(player.getUuid())) return ActionResult.PASS;
		if (getCountdown(server) <= 0) return ActionResult.PASS;

		if (HotPotatoCooldownManager.isOnCooldown()) {
			player.sendMessage(translatable("hot-potato.cooldown_message").formatted(Formatting.BLUE), true);
			return ActionResult.PASS;
		}



		serverWorld.spawnParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.5);
		serverWorld.playSound(entity, entity.getBlockPos(), SoundEvents.ENTITY_SILVERFISH_STEP, SoundCategory.MASTER, 1, 1);

		setCurrentHotPotato(server, entity.getUuid());

		player.swingHand(player.getActiveHand(), true);
		HotPotatoCooldownManager.setCooldown();

		return ActionResult.SUCCESS;
	}
}