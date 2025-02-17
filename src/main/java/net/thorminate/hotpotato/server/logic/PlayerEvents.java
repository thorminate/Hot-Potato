package net.thorminate.hotpotato.server.logic;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
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

import static net.minecraft.text.Text.translatable;
import static net.thorminate.hotpotato.server.HotPotatoManager.*;

public class PlayerEvents {
    public static void register() {
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

        if (CooldownManager.isOnCooldown()) {
            player.sendMessage(translatable("hot-potato.cooldown_message").formatted(Formatting.BLUE), true);
            return ActionResult.PASS;
        }



        serverWorld.spawnParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.5);
        serverWorld.playSound(entity, entity.getBlockPos(), SoundEvents.ENTITY_SILVERFISH_STEP, SoundCategory.MASTER, 1, 1);

        setCurrentHotPotato(server, entity.getUuid());

        player.swingHand(player.getActiveHand(), true);
        CooldownManager.setCooldown();

        return ActionResult.SUCCESS;
    }
}
