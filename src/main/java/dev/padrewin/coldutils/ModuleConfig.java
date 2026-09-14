package dev.padrewin.coldutils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Desired module state is preserved while panic temporarily suppresses its effects. */
public final class ModuleConfig {
    public record Option(String tab, String key, String label, int initial, int min, int max, int step, Kind kind) {}
    public enum Kind { TOGGLE, NUMBER, COLOR, KEY }
    public static final List<Option> OPTIONS = new ArrayList<>();
    static {
        toggle("Chat", "chat.enabled", "Enable chat formatting", false);
        toggle("Chat", "chat.capitalization", "Capitalize first letter", true);
        toggle("Chat", "chat.period", "Add final period", true);
        toggle("Player ESP", "esp.enabled", "Player outlines", false);
        toggle("Player ESP", "esp.names", "Player names", false);
        toggle("Player ESP", "esp.invisibleOnly", "Only invisible players", true);
        toggle("Player ESP", "esp.serverColors", "Keep server name colors", true);
        color("Player ESP", "esp.color", "Outline color", 0x00FFFF);
        color("Player ESP", "esp.nameColor", "Name color", 0xFFFFFF);
        number("Player ESP", "esp.range", "Range (blocks)", 64, 1, 256, 8);
        toggle("Logout spots", "logout.enabled", "Enable last-seen markers", false);
        number("Logout spots", "logout.duration", "Keep markers (seconds)", 300, 10, 3600, 30);
        number("Logout spots", "logout.range", "Render range (blocks)", 128, 8, 256, 8);
        toggle("Logout spots", "logout.labels", "Show name and age", true);
        color("Logout spots", "logout.color", "Marker color", 0xFF424F);
        toggle("Trails", "trails.enabled", "Enable player trails", false);
        number("Trails", "trails.duration", "Trail length (seconds)", 5, 1, 30, 1);
        number("Trails", "trails.range", "Range (blocks)", 96, 8, 256, 8);
        number("Trails", "trails.width", "Line thickness", 2, 1, 4, 1);
        toggle("Trails", "trails.self", "Include your player", false);
        color("Trails", "trails.color", "Trail color", 0xFF424F);
        toggle("Storage ESP", "storage.enabled", "Enable Storage ESP", false);
        number("Storage ESP", "storage.range", "Range (blocks)", 64, 8, 128, 8);
        toggle("Storage ESP", "storage.labels", "Container labels", true);
        toggle("Storage ESP", "storage.fill", "Translucent fill", false);
        for (String type : List.of("chest", "ender", "shulker", "barrel", "hopper", "furnace", "spawner")) {
            toggle("Storage ESP", "storage." + type, "Show " + type, true);
            color("Storage ESP", "storage." + type + "Color", type + " color", switch (type) {
                case "chest" -> 0xFFB454; case "ender" -> 0xB085FF; case "shulker" -> 0xFF70C8;
                case "barrel" -> 0xD6A778; case "hopper" -> 0x68CDE8; case "spawner" -> 0x4DFF88; default -> 0xFF6655;
            });
        }
        toggle("Freecam", "freecam.enabled", "Enable Freecam", false);
        number("Freecam", "freecam.speed", "Speed (blocks / second)", 12, 1, 60, 1);
        number("Freecam", "freecam.sensitivity", "Mouse sensitivity (%)", 100, 10, 200, 10);
        key("Freecam", "freecam.forward", "Forward key", 87);
        key("Freecam", "freecam.back", "Backward key", 83);
        key("Freecam", "freecam.left", "Left key", 65);
        key("Freecam", "freecam.right", "Right key", 68);
        key("Freecam", "freecam.up", "Up key", 32);
        key("Freecam", "freecam.down", "Down key", 340);
        toggle("Fullbright", "fullbright.enabled", "Enable Fullbright", false);
        number("Fullbright", "fullbright.strength", "Brightness (%)", 100, 10, 100, 10);
        key("General", "gui.key", "Open menu / 4-tap panic", 39);
        key("General", "panic.key", "Separate panic key (Delete clears)", -1);
        number("General", "panic.window", "Four-tap window (milliseconds)", 1200, 400, 2500, 100);
    }
    private static void toggle(String t, String k, String l, boolean d) { OPTIONS.add(new Option(t,k,l,d?1:0,0,1,1,Kind.TOGGLE)); }
    private static void number(String t, String k, String l, int d, int min, int max, int step) { OPTIONS.add(new Option(t,k,l,d,min,max,step,Kind.NUMBER)); }
    private static void color(String t, String k, String l, int d) { OPTIONS.add(new Option(t,k,l,d,0,0xFFFFFF,1,Kind.COLOR)); }
    private static void key(String t, String k, String l, int d) { OPTIONS.add(new Option(t,k,l,d,-1,348,1,Kind.KEY)); }
    private final Map<String,Integer> values = new HashMap<>();
    private boolean panic;
    public ModuleConfig() { for (Option o : OPTIONS) values.put(o.key(), o.initial()); }
    public int get(String key) { return Objects.requireNonNull(values.get(key), key); }
    public boolean flag(String key) { return get(key) != 0; }
    public boolean active(String module) { return !panic && flag(module + ".enabled"); }
    public boolean panic() { return panic; }
    public void togglePanic() { panic = !panic; }
    public void set(String key, int value) {
        Option o = OPTIONS.stream().filter(v -> v.key().equals(key)).findFirst().orElseThrow();
        if (value < o.min() || value > o.max() || (key.equals("gui.key") && value < 32))
            throw new IllegalArgumentException("Invalid value for " + key);
        values.put(key,value);
    }
    public ColdUtilsSettings legacy() {
        return new ColdUtilsSettings(active("chat"), flag("chat.capitalization"), flag("chat.period"),
                active("esp"), !panic && flag("esp.names"), flag("esp.invisibleOnly"),
                get("esp.color"),get("esp.range"),get("esp.nameColor"),flag("esp.serverColors"));
    }
    public void importLegacy(ColdUtilsSettings s) {
        set("chat.enabled",s.enabled()?1:0); set("chat.capitalization",s.capitalization()?1:0); set("chat.period",s.period()?1:0);
        set("esp.enabled",s.espEnabled()?1:0); set("esp.names",s.espNames()?1:0); set("esp.invisibleOnly",s.espInvisibleOnly()?1:0);
        set("esp.color",s.espColor()); set("esp.range",s.espRange()); set("esp.nameColor",s.nameColor()); set("esp.serverColors",s.preserveServerNameColors()?1:0);
    }
    public static ModuleConfig load(Path path) throws IOException {
        ModuleConfig c = new ModuleConfig();
        if (!Files.exists(path)) return c;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(path)) { p.load(r); }
        for (Option o : OPTIONS) if (p.containsKey(o.key())) c.set(o.key(), Integer.parseInt(p.getProperty(o.key())));
        String panic = p.getProperty("panic.suspended", "false");
        if (!panic.equals("true") && !panic.equals("false")) throw new IllegalArgumentException("Invalid panic state");
        c.panic = Boolean.parseBoolean(panic);
        return c;
    }
    public void save(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent(); Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent,"coldutils-", ".tmp");
        try {
            Properties p = new Properties(); values.forEach((k,v) -> p.setProperty(k,v.toString()));
            p.setProperty("panic.suspended",Boolean.toString(panic));
            try (Writer w = Files.newBufferedWriter(temp)) { p.store(w,"ColdUtils - desired settings; panic does not erase enabled modules"); }
            try { Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
