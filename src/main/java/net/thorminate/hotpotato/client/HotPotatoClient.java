package net.thorminate.hotpotato.client;

import net.fabricmc.api.ClientModInitializer;
import net.thorminate.hotpotato.client.config.HotPotatoConfig;
import net.thorminate.hotpotato.client.hud.HotPotatoHud;
import net.thorminate.hotpotato.client.network.RequestHotPotatoPayload;
import net.thorminate.hotpotato.client.storage.HotPotatoClientStorage;
import net.thorminate.hotpotato.server.network.HotPotatoPayload;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT;
import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN;
import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;
import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send;
import static net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT;

public class HotPotatoClient implements ClientModInitializer {
    public static HotPotatoConfig config;

    @Override
    public void onInitializeClient() {
        // Loads the config into memory.
        config = HotPotatoConfig.load();
        registerGlobalReceiver(HotPotatoPayload.ID, (payload, context) -> context.client().execute(() -> HotPotatoClientStorage.setCountdown(payload.countdown())));
        EVENT.register(new HotPotatoHud());
        DISCONNECT.register((handler, client) -> HotPotatoClientStorage.setCountdown(-1));
        JOIN.register((handler, sender, client) -> send(new RequestHotPotatoPayload()));
    }
}
