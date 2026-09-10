package dev.padrewin.coldutils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ColdUtilsSettingsTest {
    @TempDir Path directory;

    @Test void migratesLegacyConfigOnceAndPreservesOriginal() throws Exception {
        Path legacy = directory.resolve("chatpolish.properties");
        Path current = directory.resolve("coldutils.properties");
        ColdUtilsSettings selected = new ColdUtilsSettings(false, true, false, true, true, false,
                0x123456, 100, 0xABCDEF);
        selected.save(legacy);
        String original = Files.readString(legacy);
        assertEquals(selected, ColdUtilsSettings.loadWithLegacyFallback(current));
        assertEquals(selected, ColdUtilsSettings.load(current));
        assertEquals(original, Files.readString(legacy));
        ColdUtilsSettings.DEFAULT.save(current);
        assertEquals(ColdUtilsSettings.DEFAULT, ColdUtilsSettings.loadWithLegacyFallback(current));
    }

    @Test void invalidLegacyConfigDoesNotCreateNewConfig() throws Exception {
        Files.writeString(directory.resolve("chatpolish.properties"), "esp.enabled=invalid");
        Path current = directory.resolve("coldutils.properties");
        assertThrows(IllegalArgumentException.class, () -> ColdUtilsSettings.loadWithLegacyFallback(current));
        assertFalse(Files.exists(current));
    }

    @Test void oldConfigRetainsChatPreferencesAndLeavesEspOff() throws Exception {
        Path file = directory.resolve("coldutils.properties");
        Files.writeString(file, "enabled=false\ncapitalization=false\nperiod=true\n");
        ColdUtilsSettings loaded = ColdUtilsSettings.load(file);
        assertFalse(loaded.enabled());
        assertFalse(loaded.capitalization());
        assertTrue(loaded.period());
        assertFalse(loaded.espEnabled());
        assertFalse(loaded.espNames());
        assertTrue(loaded.espInvisibleOnly());
        assertEquals(0x00FFFF, loaded.espColor());
        assertEquals(64, loaded.espRange());
        assertEquals(0xFFFFFF, loaded.nameColor());
    }

    @Test void allSettingsSurviveSavingAndReplacingAnExistingFile() throws Exception {
        Path file = directory.resolve("config/coldutils.properties");
        ColdUtilsSettings.DEFAULT.save(file);
        ColdUtilsSettings chosen = new ColdUtilsSettings(false, true, false, true, false, false, 0x1234AB, 128, 0xFF0088);
        chosen.save(file);
        assertEquals(chosen, ColdUtilsSettings.load(file));
        try (var files = Files.list(file.getParent())) {
            assertEquals(1, files.count(), "Temporary config files must be cleaned up");
        }
    }

    @Test void missingConfigUsesDefaults() throws Exception {
        assertEquals(ColdUtilsSettings.DEFAULT, ColdUtilsSettings.load(directory.resolve("missing.properties")));
    }

    @Test void acceptsHashPrefixedHexColor() throws Exception {
        Path file = directory.resolve("coldutils.properties");
        Files.writeString(file, "esp.color=#ff0088\nesp.range=1\n");
        assertEquals(0xFF0088, ColdUtilsSettings.load(file).espColor());
    }

    @Test void rejectsInvalidValuesWithoutChangingTheFile() throws Exception {
        Path file = directory.resolve("coldutils.properties");
        for (String content : new String[]{"esp.enabled=maybe", "esp.names=invalid",
                "esp.invisibleOnly=invalid", "esp.color=1000000", "esp.color=-1",
                "esp.color=oops", "esp.range=0", "esp.range=257", "esp.range=NaN",
                "esp.nameColor=1000000", "esp.nameColor=-1", "esp.nameColor=oops"}) {
            Files.writeString(file, content);
            assertThrows(IllegalArgumentException.class, () -> ColdUtilsSettings.load(file), content);
            assertEquals(content, Files.readString(file));
        }
    }

    @Test void espDoesNotChangeChatFormatting() {
        ColdUtilsSettings esp = new ColdUtilsSettings(true, true, true, true, true, true, 0, 256, 0xFFFFFF);
        assertEquals("Hello.", ChatFormatter.format("hello", esp));
        assertEquals("/spawn", ChatFormatter.format("/spawn", esp));
        ColdUtilsSettings chatDisabled = new ColdUtilsSettings(false, true, true, true, true, true, 0, 256, 0xFFFFFF);
        assertEquals("hello", ChatFormatter.format("hello", chatDisabled));
    }

    @Test void outlineAndNamesCanBeSavedIndependently() throws Exception {
        Path file = directory.resolve("coldutils.properties");
        for (boolean outline : new boolean[]{false, true}) {
            for (boolean names : new boolean[]{false, true}) {
                ColdUtilsSettings selected = new ColdUtilsSettings(true, true, true, outline, names, false,
                        0xFF0000, 64, 0x00FF00);
                selected.save(file);
                assertEquals(selected, ColdUtilsSettings.load(file));
            }
        }
    }
}
