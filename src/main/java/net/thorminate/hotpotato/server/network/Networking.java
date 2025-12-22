package net.thorminate.hotpotato.server.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.thorminate.hotpotato.HotPotato;
import net.thorminate.hotpotato.client.network.RequestCountdownPayload;
import net.thorminate.hotpotato.server.HotPotatoManager;

import static net.thorminate.hotpotato.server.HotPotatoManager.syncWithClients;

public class Networking {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(CountdownPayload.TYPE, CountdownPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestCountdownPayload.TYPE, RequestCountdownPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                RequestCountdownPayload.TYPE,
                (payload, context) -> syncWithClients(context.server())
        );

        HotPotato.LOGGER.info("[HotPotato] Registered Networking");
    }
}
