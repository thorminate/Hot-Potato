package net.thorminate.hotpotato.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class AllHudClient {
    public static void register() {
        HudRenderCallback.EVENT.register(new Hud());
    }
}
