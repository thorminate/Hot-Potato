package net.thorminate.hotpotato.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static java.lang.Integer.parseInt;
import static net.thorminate.hotpotato.HotPotato.MOD_ID;

import net.thorminate.hotpotato.client.HotPotatoClient;
import net.thorminate.hotpotato.client.storage.HotPotatoClientStorage;

public class HotPotatoHud implements HudRenderCallback {
    private static final Identifier FINAL_POTATO = Identifier.of(MOD_ID, "textures/gui/final_potato.png");

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter renderTickCounter) {
        if (HotPotatoClient.config.shouldRenderCountdown || HotPotatoClient.config.shouldRenderImage) {
            int countdown = HotPotatoClientStorage.getCountdown();
            if (countdown <= 0) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            // Variables and logic for the text
            if (HotPotatoClient.config.shouldRenderImage) {
                int textureHudPosX = client.getWindow().getScaledWidth() - 64;
                int textureHudPosY = 0;

                // Variables and logic for the texture
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                context.drawTexture(
                        FINAL_POTATO,
                        textureHudPosX, textureHudPosY,
                        0, 0,
                        64, 64,
                        64, 64
                );

                RenderSystem.disableBlend();
            }

            if (HotPotatoClient.config.shouldRenderCountdown) {
                int textHudPosX = client.getWindow().getScaledWidth() - 48;
                int textHudPosY = 74;
                int seconds = countdown % 60;
                int minutes = countdown / 60;

                if (seconds < 10) {
                    seconds = parseInt("0" + seconds);
                }
                if (minutes < 10) {
                    minutes = parseInt("0" + minutes);
                }

                Formatting formatting;
                if (countdown < 60) {
                    formatting = Formatting.RED;
                } else if (countdown < 300) {
                    formatting = Formatting.YELLOW;
                } else {
                    formatting = Formatting.GREEN;
                }

                Text text = Text.literal("\uD83D\uDD25" + minutes + "m " + seconds + "s").formatted(formatting);

                context.drawText(
                        client.textRenderer,
                        text,
                        textHudPosX, textHudPosY,
                        0xFFFFFF,
                        true
                );

            }
        }
    }
}
