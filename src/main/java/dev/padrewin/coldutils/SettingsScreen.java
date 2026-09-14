package dev.padrewin.coldutils;

import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.Method;
import java.util.function.Consumer;

final class SettingsScreen {
    private SettingsScreen() {}

    static Object create(Object parent) {
        try {
            Object builder = Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder")
                    .getMethod("create").invoke(null);
            call(builder, "setParentScreen", parent);
            call(builder, "setTitle", text("ColdUtils"));
            Object category = call(builder, "getOrCreateCategory", text("Chat"));
            Object entries = call(builder, "entryBuilder");
            ColdUtilsSettings current = ColdUtils.settings();
            boolean[] draft = {current.enabled(), current.capitalization(), current.period()};
            String[] labels = {"Enable chat formatting", "Capitalize first letter", "Add final period"};
            for (int i = 0; i < labels.length; i++) {
                final int index = i;
                Object entry = call(entries, "startBooleanToggle", text(labels[i]), draft[i]);
                call(entry, "setDefaultValue", true);
                call(entry, "setSaveConsumer", (Consumer<Boolean>) value -> draft[index] = value);
                call(category, "addEntry", call(entry, "build"));
            }
            Object espCategory = call(builder, "getOrCreateCategory", text("Player ESP"));
            boolean[] esp = {current.espEnabled(), current.espNames(), current.espInvisibleOnly(), current.preserveServerNameColors()};
            boolean[] defaults = {false, false, true, true};
            String[] espLabels = {"Enable Player ESP", "Enable Player Names", "Only invisible players", "Keep server name colors"};
            for (int i = 0; i < esp.length; i++) {
                final int index = i;
                Object entry = call(entries, "startBooleanToggle", text(espLabels[i]), esp[i]);
                call(entry, "setDefaultValue", defaults[i]);
                call(entry, "setSaveConsumer", (Consumer<Boolean>) value -> esp[index] = value);
                call(espCategory, "addEntry", call(entry, "build"));
            }
            int[] appearance = {current.espColor(), current.espRange(), current.nameColor()};
            Object color = call(entries, "startColorField", text("Outline color"), appearance[0]);
            call(color, "setDefaultValue", ColdUtilsSettings.DEFAULT.espColor());
            call(color, "setSaveConsumer", (Consumer<Integer>) value -> appearance[0] = value & 0xFFFFFF);
            call(espCategory, "addEntry", call(color, "build"));
            Object nameColor = call(entries, "startColorField", text("Player name color (server colors OFF)"), appearance[2]);
            call(nameColor, "setDefaultValue", ColdUtilsSettings.DEFAULT.nameColor());
            call(nameColor, "setSaveConsumer", (Consumer<Integer>) value -> appearance[2] = value & 0xFFFFFF);
            call(espCategory, "addEntry", call(nameColor, "build"));
            Object range = call(entries, "startIntSlider", text("Range (blocks)"), appearance[1], 1, 256);
            call(range, "setDefaultValue", ColdUtilsSettings.DEFAULT.espRange());
            call(range, "setSaveConsumer", (Consumer<Integer>) value -> appearance[1] = value);
            call(espCategory, "addEntry", call(range, "build"));
            call(builder, "setSavingRunnable", (Runnable) () -> ColdUtils.saveSettings(
                    new ColdUtilsSettings(draft[0], draft[1], draft[2], esp[0], esp[1], esp[2],
                            appearance[0], appearance[1], appearance[2], esp[3])));
            return call(builder, "build");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot open ColdUtils settings. Check Cloth Config compatibility.", e);
        }
    }

    private static Object text(String value) throws ReflectiveOperationException {
        // 26.x uses Mojang names; older production Fabric uses intermediary names.
        var mappings = FabricLoader.getInstance().getMappingResolver();
        if (!mappings.getNamespaces().contains("intermediary")) {
            return Class.forName("net.minecraft.network.chat.Component")
                    .getMethod("literal", String.class).invoke(null, value);
        }
        String name = mappings.mapClassName("intermediary", "net.minecraft.class_2561");
        try {
            Class<?> component = Class.forName(name);
            String method = mappings.mapMethodName("intermediary", "net.minecraft.class_2561",
                    "method_43470", "(Ljava/lang/String;)Lnet/minecraft/class_5250;");
            return component.getMethod(method, String.class).invoke(null, value);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return Class.forName("net.minecraft.network.chat.Component")
                    .getMethod("literal", String.class).invoke(null, value);
        }
    }

    private static Object call(Object target, String name, Object... args) throws ReflectiveOperationException {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            Class<?>[] types = method.getParameterTypes();
            boolean matches = true;
            for (int i = 0; i < types.length; i++) {
                Class<?> type = types[i] == boolean.class ? Boolean.class
                        : types[i] == int.class ? Integer.class : types[i];
                if (args[i] != null && !type.isInstance(args[i])) matches = false;
            }
            if (matches) return method.invoke(target, args);
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }
}
