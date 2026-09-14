package dev.padrewin.coldutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Path;

public final class ColdUtils implements ClientModInitializer {
    private static volatile ColdUtilsSettings settings = ColdUtilsSettings.DEFAULT;
    private static ModuleConfig modules = new ModuleConfig();
    private static String saveError;

    public static ColdUtilsSettings settings() { return settings; }
    public static ModuleConfig modules() { return modules; }
    public static String saveError() { return saveError; }
    private static Path modulesPath() { return FabricLoader.getInstance().getConfigDir().resolve("coldutils-modules.properties"); }
    public static void saveModules() {
        settings = modules.legacy();
        try { modules.save(modulesPath()); saveError=null; }
        catch(IOException e) { saveError="Save failed - check config permissions"; RuntimeBridge.fail("Save configuration",e); }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("coldutils.properties");
    }

    public static void saveSettings(ColdUtilsSettings next) {
        try {
            next.save(configPath());
            settings = next;
            modules.importLegacy(next);
            saveModules();
        } catch (IOException e) {
            throw new IllegalStateException("Could not save ColdUtils settings", e);
        }
    }

    @Override
    public void onInitializeClient() {
        try {
            if (java.nio.file.Files.exists(modulesPath())) modules = ModuleConfig.load(modulesPath());
            else modules.importLegacy(ColdUtilsSettings.loadWithLegacyFallback(configPath()));
            settings = modules.legacy();
        } catch (IOException | IllegalArgumentException e) {
            System.getLogger("ColdUtils").log(System.Logger.Level.WARNING,
                    "Could not read settings; using defaults without overwriting the file", e);
        }
        ClientSendMessageEvents.ALLOW_CHAT.register(ColdNear::allowChat);
        ClientSendMessageEvents.MODIFY_CHAT.register(message -> ChatFormatter.format(message, settings));
    }
}
