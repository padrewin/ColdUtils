package dev.padrewin.coldutils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public record ColdUtilsSettings(boolean enabled, boolean capitalization, boolean period,
                           boolean espEnabled, boolean espNames, boolean espInvisibleOnly,
                           int espColor, int espRange, int nameColor, boolean preserveServerNameColors) {
    public static final ColdUtilsSettings DEFAULT = new ColdUtilsSettings(false, true, true, false, false, true, 0x00FFFF, 64, 0xFFFFFF);

    public ColdUtilsSettings(boolean enabled, boolean capitalization, boolean period,
                             boolean espEnabled, boolean espNames, boolean espInvisibleOnly,
                             int espColor, int espRange, int nameColor) {
        this(enabled, capitalization, period, espEnabled, espNames, espInvisibleOnly,
                espColor, espRange, nameColor, true);
    }
    public ColdUtilsSettings(boolean enabled, boolean capitalization, boolean period) {
        this(enabled, capitalization, period, false, false, true, 0x00FFFF, 64, 0xFFFFFF);
    }

    public ColdUtilsSettings {
        if (espColor < 0 || espColor > 0xFFFFFF) throw new IllegalArgumentException("ESP color must be RGB");
        if (nameColor < 0 || nameColor > 0xFFFFFF) throw new IllegalArgumentException("Name color must be RGB");
        if (espRange < 1 || espRange > 256) throw new IllegalArgumentException("ESP range must be 1–256 blocks");
    }

    public static ColdUtilsSettings loadWithLegacyFallback(Path file) throws IOException {
        if (Files.exists(file)) return load(file);
        Path legacy = file.resolveSibling("chatpolish.properties");
        if (!Files.exists(legacy)) return DEFAULT;
        ColdUtilsSettings migrated = load(legacy);
        migrated.save(file);
        return migrated;
    }

    public static ColdUtilsSettings load(Path file) throws IOException {
        if (!Files.exists(file)) return DEFAULT;
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) { values.load(reader); }
        return new ColdUtilsSettings(read(values, "enabled", false), read(values, "capitalization", true),
                read(values, "period", true), read(values, "esp.enabled", false),
                read(values, "esp.names", false), read(values, "esp.invisibleOnly", true),
                Integer.parseInt(values.getProperty("esp.color", "00FFFF").strip().replaceFirst("^#", ""), 16),
                Integer.parseInt(values.getProperty("esp.range", "64").strip()),
                Integer.parseInt(values.getProperty("esp.nameColor", "FFFFFF").strip().replaceFirst("^#", ""), 16), read(values, "esp.preserveServerNameColors", true));
    }

    private static boolean read(Properties values, String name, boolean fallback) {
        String value = values.getProperty(name, Boolean.toString(fallback)).strip();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Invalid boolean for " + name + ": " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = Files.createTempFile(file.toAbsolutePath().getParent(), "coldutils-", ".tmp");
        try {
            Properties values = new Properties();
            values.setProperty("enabled", Boolean.toString(enabled));
            values.setProperty("capitalization", Boolean.toString(capitalization));
            values.setProperty("period", Boolean.toString(period));
            values.setProperty("esp.enabled", Boolean.toString(espEnabled));
            values.setProperty("esp.names", Boolean.toString(espNames));
            values.setProperty("esp.invisibleOnly", Boolean.toString(espInvisibleOnly));
            values.setProperty("esp.color", String.format("%06X", espColor));
            values.setProperty("esp.range", Integer.toString(espRange));
            values.setProperty("esp.nameColor", String.format("%06X", nameColor));
            values.setProperty("esp.preserveServerNameColors", Boolean.toString(preserveServerNameColors));
            try (Writer writer = Files.newBufferedWriter(temp)) {
                values.store(writer, "ColdUtils - configurable through Mod Menu");
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
