package net.thorminate.hotpotato.common.storage;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.thorminate.hotpotato.common.HotPotatoIndex;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WorldDataManager extends PersistentState {
    public static final String HOT_POTATO_NBT_KEY = "hot_potato_data";

    private UUID currentHotPotato = HotPotatoIndex.NULLISH_UUID;
    private int countdown = -1;

    public WorldDataManager(NbtCompound nbt) {
        if (nbt.contains(HOT_POTATO_NBT_KEY, NbtElement.INT_TYPE)) this.countdown = nbt.getInt(HOT_POTATO_NBT_KEY);
        this.currentHotPotato = nbt.getUuid(HOT_POTATO_NBT_KEY);
    }

    public WorldDataManager() {}

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putUuid(HOT_POTATO_NBT_KEY, this.currentHotPotato);
        nbt.putInt(HOT_POTATO_NBT_KEY, this.countdown);
        return nbt;
    }

    public UUID getCurrentHotPotato() {
        return this.currentHotPotato;
    }

    public int getCountdown() {
        return this.countdown;
    }

    public void setCurrentHotPotato(@Nullable UUID playerUuid) {
        this.currentHotPotato = playerUuid;
        this.markDirty();
    }

    public void setCountdown(int time) {
        this.countdown = time;
        this.markDirty();
    }

    public static final Type<WorldDataManager> TYPE = new Type<>(
            WorldDataManager::new,
            (nbt, registryLookup) -> new WorldDataManager(nbt),
            DataFixTypes.LEVEL
    );
}
