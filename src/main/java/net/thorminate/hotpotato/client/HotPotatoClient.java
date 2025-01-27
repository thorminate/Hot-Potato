package net.thorminate.hotpotato.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.thorminate.hotpotato.client.hud.HotPotatoHud;
import net.thorminate.hotpotato.common.HotPotatoIndex;
import net.thorminate.hotpotato.network.HotPotatoPayload;

import java.util.Objects;

public class HotPotatoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(HotPotatoPayload.ID, (payload, context) -> context.client().execute(() -> {
            HotPotatoIndex.setCurrentHotPotato(Objects.requireNonNull(context.client().getServer()), payload.player());
            HotPotatoIndex.setCountdown(Objects.requireNonNull(context.client().getServer()), payload.countdown());
        }));
        HudRenderCallback.EVENT.register(((context, tickDelta) -> HotPotatoHud.render(context)));
    }
}
