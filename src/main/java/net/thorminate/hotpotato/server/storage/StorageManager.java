package net.thorminate.hotpotato.server.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

public class StorageManager extends SavedData {
    private UUID currentHotPotato;
    private int countdown;
    private final Set<UUID> doomedPlayers = new HashSet<>();

    public StorageManager() {}

    public StorageManager(UUID currentHotPotato, int countdown, Set<UUID> doomedPlayers) {
        this.currentHotPotato = currentHotPotato;
        this.countdown = countdown;
        this.doomedPlayers.addAll(doomedPlayers);
    }

    public UUID getCurrentHotPotato() {
        return currentHotPotato;
    }

    public int getCountdown() {
        return countdown;
    }

    public Set<UUID> getDoomedPlayers() {
        return doomedPlayers;
    }

    public void setCurrentHotPotato(@Nullable UUID uuid) { this.currentHotPotato = uuid; this.setDirty(); }
    public void setCountdown(int time) { this.countdown = time; this.setDirty(); }
    public void doom(UUID uuid) { doomedPlayers.add(uuid); setDirty(); }
    public void pardon(UUID uuid) { doomedPlayers.remove(uuid); setDirty(); }
    public void clearDoomed() { doomedPlayers.clear(); setDirty(); }
    public boolean isDoomed(UUID uuid) { return doomedPlayers.contains(uuid); }

    private static final Codec<Set<UUID>> UUID_SET_CODEC =
            Codec.STRING
                    .xmap(UUID::fromString, UUID::toString)
                    .listOf()
                    .xmap(
                            HashSet::new,
                            ArrayList::new
                    );

    private static final Codec<StorageManager> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("countdown").forGetter(StorageManager::getCountdown),
                Codec.STRING.optionalFieldOf("currentHotPotato")
                        .forGetter(sm -> sm.getCurrentHotPotato() == null
                                ? Optional.empty()
                                : Optional.of(sm.currentHotPotato.toString())
                        ),
                    UUID_SET_CODEC.optionalFieldOf("doomedPlayers", Set.of()).forGetter(sm -> sm.doomedPlayers)
            ).apply(instance, (countdown, uuidOpt, doomedSet) ->
                    new StorageManager(
                            uuidOpt.map(UUID::fromString).orElse(null),
                            countdown,
                            doomedSet
                    )
                )
            );

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