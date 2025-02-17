package net.thorminate.hotpotato.client;

import net.fabricmc.api.ClientModInitializer;
import net.thorminate.hotpotato.client.config.Config;
import net.thorminate.hotpotato.client.hud.AllHudClient;
import net.thorminate.hotpotato.client.logic.LifecycleEventsClient;
import net.thorminate.hotpotato.client.network.NetworkingClient;

public class HotPotatoClient implements ClientModInitializer {
    public static Config config;

    @Override
    public void onInitializeClient() {
        // Loads the config into memory.
        config = Config.load();

        NetworkingClient.register();
        AllHudClient.register();
        LifecycleEventsClient.register();
    }
}
