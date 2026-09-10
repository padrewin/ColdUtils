package dev.padrewin.chatpolish;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public record ChatSettings(boolean enabled, boolean capitalization, boolean period) {
    public static final ChatSettings DEFAULT = new ChatSettings(true, true, true);

    public static ChatSettings load(Path file) throws IOException {
        if (!Files.exists(file)) return DEFAULT;
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) { values.load(reader); }
        return new ChatSettings(read(values, "enabled"), read(values, "capitalization"), read(values, "period"));
    }

    private static boolean read(Properties values, String name) {
        String value = values.getProperty(name, "true").strip();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Invalid boolean for " + name + ": " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = Files.createTempFile(file.toAbsolutePath().getParent(), "chatpolish-", ".tmp");
        try {
            Properties values = new Properties();
            values.setProperty("enabled", Boolean.toString(enabled));
            values.setProperty("capitalization", Boolean.toString(capitalization));
            values.setProperty("period", Boolean.toString(period));
            try (Writer writer = Files.newBufferedWriter(temp)) {
                values.store(writer, "Chat Polish - configurable through Mod Menu");
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
