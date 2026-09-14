package dev.padrewin.coldutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Path;

public final class ColdUtils implements ClientModInitializer {
    private static volatile ColdUtilsSettings settings = ColdUtilsSettings.DEFAULT;

    public static ColdUtilsSettings settings() { return settings; }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("coldutils.properties");
    }

    public static void saveSettings(ColdUtilsSettings next) {
        try {
            next.save(configPath());
            settings = next;
        } catch (IOException e) {
            throw new IllegalStateException("Could not save ColdUtils settings", e);
        }
    }

    @Override
    public void onInitializeClient() {
        try {
            settings = ColdUtilsSettings.loadWithLegacyFallback(configPath());
        } catch (IOException | IllegalArgumentException e) {
            System.getLogger("ColdUtils").log(System.Logger.Level.WARNING,
                    "Could not read settings; using defaults without overwriting the file", e);
        }
        ClientSendMessageEvents.ALLOW_CHAT.register(ColdNear::allowChat);
        ClientSendMessageEvents.MODIFY_CHAT.register(message -> ChatFormatter.format(message, settings));
    }
}
