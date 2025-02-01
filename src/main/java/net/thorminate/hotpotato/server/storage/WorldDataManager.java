package net.thorminate.hotpotato.server.storage;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import static net.minecraft.registry.RegistryWrapper.WrapperLookup;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class WorldDataManager extends PersistentState {
    public static final String COUNTDOWN_KEY = "hot_potato_countdown";
    public static final String PLAYER_KEY = "hot_potato_player";

    private UUID currentHotPotato;
    private int countdown;

    public WorldDataManager(NbtCompound nbt) {
        // First read the countdown and put it in the field to be stored in memory.
        if (nbt.contains(COUNTDOWN_KEY)) this.countdown = nbt.getInt(COUNTDOWN_KEY);

        // Then read the player UUID, if it exists return that, else return nullish UUID
        UUID player = nbt.contains(PLAYER_KEY) ? nbt.getUuid(PLAYER_KEY) : new UUID(0, 0);

        // Set the current hot potato to the player UUID, if it is nullish UUID, set it to null.
        this.currentHotPotato = player.equals(new UUID(0, 0)) ? null : player;
    }

    public WorldDataManager() {}

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        // If the current hot potato is null, return a nullish UUID instead.
        UUID storedUuid = this.currentHotPotato != null ? this.currentHotPotato : new UUID(0, 0);

        // Write the player UUID and the countdown into the NBT
        nbt.putUuid(PLAYER_KEY, storedUuid);
        nbt.putInt(COUNTDOWN_KEY, this.countdown);
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
