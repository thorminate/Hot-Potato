package net.thorminate.hotpotato.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.resources.Identifier;

import net.thorminate.hotpotato.HotPotato;

public class AllHudClient {
    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(HotPotato.MOD_ID, "hud"), ((guiGraphics, deltaTracker) -> new Hud().render(guiGraphics, deltaTracker)));
    }
}
