package net.thorminate.hotpotato.client.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.thorminate.hotpotato.client.config.HotPotatoConfigScreen;
import org.lwjgl.glfw.GLFW;

public class HotPotatoKeybinds {
    private static final KeyBinding openConfigKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.hot-potato.open_config", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc")
    );

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            while (openConfigKey.wasPressed()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.setScreen(HotPotatoConfigScreen.create(mc.currentScreen));
            }
        });
    }
}
