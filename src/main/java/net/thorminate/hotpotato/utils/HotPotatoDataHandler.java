package net.thorminate.hotpotato.utils;

import net.minecraft.server.network.ServerPlayerEntity;

public class HotPotatoDataHandler {
    private static ServerPlayerEntity currentHotPotato = null;

    public static ServerPlayerEntity getCurrentHotPotato() {
        return currentHotPotato;
    }

    public static void setCurrentHotPotato(ServerPlayerEntity currentHotPotato) {
        HotPotatoDataHandler.currentHotPotato = currentHotPotato;
    }
}
