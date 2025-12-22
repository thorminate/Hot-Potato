package net.thorminate.hotpotato.server.logic;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static net.minecraft.network.chat.Component.translatable;
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

    private static InteractionResult useEntity(Player player, Level level, Entity entity) {
        if (level.isClientSide()) return InteractionResult.PASS;

        MinecraftServer server = entity.level().getServer();

        if (server == null) return InteractionResult.PASS;

        ServerLevel serverLevel = server.getLevel(level.dimension());
        if (serverLevel == null) return InteractionResult.PASS;

        if (!entity.isAlwaysTicking()) return InteractionResult.PASS;
        if (getCurrentHotPotato(server) == null) return InteractionResult.PASS;
        if (!getCurrentHotPotato(server).equals(player.getUUID())) return InteractionResult.PASS;
        if (getCountdown(server) <= 0) return InteractionResult.PASS;

        if (CooldownManager.isOnCooldown()) {
            player.displayClientMessage(translatable("hot-potato.cooldown_message").withStyle(ChatFormatting.BLUE), true);
            return InteractionResult.PASS;
        }



        serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.5);
        serverLevel.playSound(entity, entity.blockPosition(), SoundEvents.SILVERFISH_STEP, SoundSource.MASTER);

        setCurrentHotPotato(server, entity.getUUID());

        player.swing(player.getUsedItemHand(), true);
        CooldownManager.setCooldown();

        return InteractionResult.SUCCESS;
    }
}
