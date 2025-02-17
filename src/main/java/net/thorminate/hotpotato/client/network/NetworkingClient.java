package net.thorminate.hotpotato.client.network;

import net.thorminate.hotpotato.client.storage.StorageManagerClient;
import net.thorminate.hotpotato.server.network.CountdownPayload;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

public class NetworkingClient {
    public static void register() {
        registerGlobalReceiver(CountdownPayload.ID, (payload, context) -> context.client().execute(() -> StorageManagerClient.setCountdown(payload.countdown())));
    }
}
