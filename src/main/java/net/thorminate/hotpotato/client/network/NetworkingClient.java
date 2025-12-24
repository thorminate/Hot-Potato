package net.thorminate.hotpotato.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.thorminate.hotpotato.client.storage.StorageManagerClient;
import net.thorminate.hotpotato.server.network.CountdownPayload;

public class NetworkingClient {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CountdownPayload.TYPE, (payload, context) -> context.client().execute(() -> StorageManagerClient.setCountdown(payload.countdown())));
    }
}
