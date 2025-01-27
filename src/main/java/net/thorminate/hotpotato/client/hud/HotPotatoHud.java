package net.thorminate.hotpotato.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thorminate.hotpotato.common.HotPotatoIndex;

import java.util.Objects;

public class HotPotatoHud {
    public static void render(DrawContext context) {
        int countdown = HotPotatoIndex.getCountdown(Objects.requireNonNull(MinecraftClient.getInstance().getServer()));
        if (countdown <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int seconds = countdown % 60;
        int minutes = countdown / 60;
        if (seconds < 10) {
            seconds = Integer.parseInt("0" + seconds);
        }
        if (minutes < 10) {
            minutes = Integer.parseInt("0" + minutes);
        }

        String timeText = "\uD83D\uDD25" + minutes + "m " + seconds + "s";
        context.drawText(client.textRenderer, Text.literal(timeText).formatted(Formatting.RED), screenWidth / 3, screenHeight / 3, 0xFFFFFF, false);
    }
}