package net.thorminate.hotpotato.client.logic;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.thorminate.hotpotato.client.network.RequestCountdownPayload;
import net.thorminate.hotpotato.client.storage.StorageManagerClient;

public class LifecycleEventsClient {
    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> StorageManagerClient.setCountdown(-1));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.send(new RequestCountdownPayload()));
    }
}
