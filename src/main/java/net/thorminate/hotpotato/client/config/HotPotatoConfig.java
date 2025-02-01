package net.thorminate.hotpotato.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.thorminate.hotpotato.HotPotato;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HotPotatoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hot_potato.json");

    public boolean shouldRenderImage = true; // Default: Enabled
    public boolean shouldRenderCountdown = true; // Default: Enabled

    // Load config from file
    public static HotPotatoConfig load() {
        if (!CONFIG_FILE.exists()) {
            HotPotatoConfig config = new HotPotatoConfig();
            config.save();
            return config;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            return GSON.fromJson(reader, HotPotatoConfig.class);
        } catch (IOException e) {
            HotPotato.LOGGER.error("An error occurred in someMethod:", e);
            return new HotPotatoConfig(); // Fallback default
        }
    }

    // Save config to file
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            HotPotato.LOGGER.error("An error occurred in someMethod:", e);
        }
    }
}

