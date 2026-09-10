package dev.padrewin.chatpolish;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Path;

public final class ChatPolish implements ClientModInitializer {
    private static volatile ChatSettings settings = ChatSettings.DEFAULT;

    public static ChatSettings settings() { return settings; }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("chatpolish.properties");
    }

    public static void saveSettings(ChatSettings next) {
        try {
            next.save(configPath());
            settings = next;
        } catch (IOException e) {
            throw new IllegalStateException("Could not save Chat Polish settings", e);
        }
    }

    @Override
    public void onInitializeClient() {
        try {
            settings = ChatSettings.load(configPath());
        } catch (IOException | IllegalArgumentException e) {
            System.getLogger("ChatPolish").log(System.Logger.Level.WARNING,
                    "Could not read settings; using defaults without overwriting the file", e);
        }
        ClientSendMessageEvents.MODIFY_CHAT.register(message -> ChatFormatter.format(message, settings));
    }
}
