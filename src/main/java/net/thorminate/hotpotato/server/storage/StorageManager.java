package net.thorminate.hotpotato.server.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.thorminate.hotpotato.HotPotato;
import org.jspecify.annotations.Nullable;

public class StorageManager extends SavedData {
    private UUID currentHotPotato;
    private int countdown;

    public StorageManager() {}

    public StorageManager(UUID currentHotPotato, int countdown) {
        this.currentHotPotato = currentHotPotato;
        this.countdown = countdown;
    }

    public UUID getCurrentHotPotato() {
        return currentHotPotato;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCurrentHotPotato(@Nullable UUID currentHotPotato) {
        this.currentHotPotato = currentHotPotato;
        this.setDirty();
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
        this.setDirty();
    }

    private static final Codec<StorageManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("countdown").forGetter(StorageManager::getCountdown),
            Codec.STRING.fieldOf("currentHotPotato").forGetter(sm ->
                    sm.getCurrentHotPotato() != null ? sm.getCurrentHotPotato().toString() : new UUID(0, 0).toString())
    ).apply(instance, (countdown, uuidString) -> new StorageManager(UUID.fromString(uuidString).toString() == new UUID(0, 0).toString() ? UUID.fromString(uuidString) : null, countdown)));

    public static final SavedDataType<StorageManager> TYPE = new SavedDataType<>(
            "hot_potato_storage",
            StorageManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static StorageManager get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(TYPE);
    }
}