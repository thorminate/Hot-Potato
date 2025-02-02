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
import org.jetbrains.annotations.NotNull;

public class HotPotatoHud implements HudRenderCallback {
    private static final Identifier POTATO_1 = Identifier.of(MOD_ID, "textures/gui/potato_1.png");
    private static final Identifier POTATO_2 = Identifier.of(MOD_ID, "textures/gui/potato_2.png");
    private static final Identifier POTATO_3 = Identifier.of(MOD_ID, "textures/gui/potato_3.png");
    private static final Identifier POTATO_4 = Identifier.of(MOD_ID, "textures/gui/potato_4.png");
    private static final Identifier POTATO_5 = Identifier.of(MOD_ID, "textures/gui/potato_5.png");
    private static final Identifier POTATO_6 = Identifier.of(MOD_ID, "textures/gui/potato_6.png");
    private static final Identifier POTATO_7 = Identifier.of(MOD_ID, "textures/gui/potato_7.png");
    private static final Identifier POTATO_8 = Identifier.of(MOD_ID, "textures/gui/potato_8.png");

    private static @NotNull Identifier getIdentifier(int countdown) {
        Identifier potatoTexture;

        if (countdown < 30) {
            potatoTexture = POTATO_8;
        } else if (countdown < 60) {
            potatoTexture = POTATO_7;
        } else if (countdown < 180) {
            potatoTexture = POTATO_6;
        } else if (countdown < 300) {
            potatoTexture = POTATO_5;
        } else if (countdown < 420) {
            potatoTexture = POTATO_4;
        } else if (countdown < 600) {
            potatoTexture = POTATO_3;
        } else if (countdown < 900) {
            potatoTexture = POTATO_2;
        } else {
            potatoTexture = POTATO_1;
        }
        return potatoTexture;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter renderTickCounter) {
        if (HotPotatoClient.config.shouldRenderCountdown || HotPotatoClient.config.shouldRenderImage) {
            int countdown = HotPotatoClientStorage.getCountdown();
            if (countdown <= 0) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            int textureHudPosX;
            int textureHudPosY;
            int textureSize = 64;

            int textHudPosX;
            int textHudPosY;


            // Variables and logic for the text
            if (HotPotatoClient.config.shouldRenderImage) {

                textureHudPosX = client.getWindow().getScaledWidth() - 64;
                textureHudPosY = 0;

                // Variables and logic for the texture
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                Identifier potatoTexture = getIdentifier(countdown);

                context.drawTexture(
                        potatoTexture,
                        textureHudPosX, textureHudPosY,
                        0, 0,
                        textureSize, textureSize,
                        textureSize, textureSize
                );

                RenderSystem.disableBlend();
            }

            if (HotPotatoClient.config.shouldRenderCountdown) {

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

                int textWidth = client.textRenderer.getWidth(text);

                if (HotPotatoClient.config.shouldRenderImage) {

                    int textPadding = (textureSize - textWidth) / 2;

                    textHudPosX = client.getWindow().getScaledWidth() - textWidth - textPadding;
                    textHudPosY = 64;
                } else {
                    textHudPosX = client.getWindow().getScaledWidth() - textWidth - 5;
                    textHudPosY = 5;
                }

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
