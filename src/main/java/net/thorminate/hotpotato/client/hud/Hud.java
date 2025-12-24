package net.thorminate.hotpotato.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static java.lang.Integer.parseInt;
import static net.thorminate.hotpotato.HotPotato.MOD_ID;

import net.thorminate.hotpotato.client.HotPotatoClient;
import net.thorminate.hotpotato.client.storage.StorageManagerClient;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public class Hud implements HudElement {
    private static final Identifier POTATO_1 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_1.png");
    private static final Identifier POTATO_2 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_2.png");
    private static final Identifier POTATO_3 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_3.png");
    private static final Identifier POTATO_4 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_4.png");
    private static final Identifier POTATO_5 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_5.png");
    private static final Identifier POTATO_6 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_6.png");
    private static final Identifier POTATO_7 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_7.png");
    private static final Identifier POTATO_8 = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/potato_8.png");
    private final Random random = new Random();

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
    public void render(@NonNull GuiGraphics guiGraphics, @NonNull DeltaTracker deltaTracker) {
        if (HotPotatoClient.config.shouldRenderCountdown || HotPotatoClient.config.shouldRenderImage) {
            int countdown = StorageManagerClient.getCountdown();
            if (countdown <= 0) return;

            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            int textureHudPosX;
            int textureHudPosY;
            int textureSize = 64;

            int textHudPosX;
            int textHudPosY;


            // Variables and logic for the text
            if (HotPotatoClient.config.shouldRenderImage) {

                textureHudPosX = client.getWindow().getGuiScaledWidth() - 64;
                textureHudPosY = 0;

                if (countdown < 10) {
                    int shakeIntensity = Math.round((11 - countdown) * 1.5f);
                    textureHudPosX = textureHudPosX + (random.nextInt(shakeIntensity * 2 + 1) - shakeIntensity);
                    textureHudPosY = textureHudPosY + (random.nextInt(shakeIntensity * 2 + 1) - shakeIntensity);
                }

                // Variables and logic for the texture
                Identifier potatoTexture = getIdentifier(countdown);

                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        potatoTexture,
                        textureHudPosX, textureHudPosY,
                        0, 0,
                        64, 64,
                        64, 64
                );
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

                ChatFormatting formatting;
                if (countdown < 60) {
                    formatting = ChatFormatting.RED;
                } else if (countdown < 300) {
                    formatting = ChatFormatting.YELLOW;
                } else {
                    formatting = ChatFormatting.GREEN;
                }

                Component text = Component.literal("\uD83D\uDD25" + minutes + "m " + seconds + "s").withStyle(formatting);

                int textWidth = client.font.width(text);

                if (HotPotatoClient.config.shouldRenderImage) {

                    int textPadding = (textureSize - textWidth) / 2;

                    textHudPosX = client.getWindow().getGuiScaledWidth() - textWidth - textPadding;
                    textHudPosY = 64;
                } else {
                    textHudPosX = client.getWindow().getGuiScaledWidth() - textWidth - 5;
                    textHudPosY = 5;
                }

                guiGraphics.drawString(
                        client.font,
                        text,
                        textHudPosX, textHudPosY,
                        0xFFFFFFFF,
                        true
                );

            }
        }
    }
}