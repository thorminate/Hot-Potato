package net.thorminate.hotpotato.server.resources;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.thorminate.hotpotato.HotPotato;

public class DamageTypes {
    public static final ResourceKey<DamageType> HOT_POTATO_EXPLODED = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(HotPotato.MOD_ID, "hot_potato_exploded")
    );
}
