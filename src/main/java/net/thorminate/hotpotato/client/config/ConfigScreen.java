package net.thorminate.hotpotato.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.thorminate.hotpotato.client.HotPotatoClient;

public class ConfigScreen {
    public static Screen create(Screen parent) {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) return parent;

        Config config = HotPotatoClient.config;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.hot-potato.title"));

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.hot-potato.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.hot-potato.shouldRenderPotato"), config.shouldRenderImage)
                .setTooltip(Component.translatable("config.hot-potato.shouldRenderPotato.tooltip"))
                .setDefaultValue(true)
                .setSaveConsumer(shouldRenderImage -> config.shouldRenderImage = shouldRenderImage)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.hot-potato.shouldRenderCountdown"), config.shouldRenderCountdown)
                .setTooltip(Component.translatable("config.hot-potato.shouldRenderCountdown.tooltip"))
                .setDefaultValue(true)
                .setSaveConsumer(shouldRenderCountdown -> config.shouldRenderCountdown = shouldRenderCountdown)
                .build());

        builder.setSavingRunnable(config::save);

        return builder.build();
    }
}
