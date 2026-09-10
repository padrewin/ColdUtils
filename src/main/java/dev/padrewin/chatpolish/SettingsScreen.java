package dev.padrewin.chatpolish;

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
            call(builder, "setTitle", text("Chat Polish"));
            Object category = call(builder, "getOrCreateCategory", text("General"));
            Object entries = call(builder, "entryBuilder");
            ChatSettings current = ChatPolish.settings();
            boolean[] draft = {current.enabled(), current.capitalization(), current.period()};
            String[] labels = {"Enable Chat Polish", "Capitalize first letter", "Add final period"};
            for (int i = 0; i < labels.length; i++) {
                final int index = i;
                Object entry = call(entries, "startBooleanToggle", text(labels[i]), draft[i]);
                call(entry, "setDefaultValue", true);
                call(entry, "setSaveConsumer", (Consumer<Boolean>) value -> draft[index] = value);
                call(category, "addEntry", call(entry, "build"));
            }
            call(builder, "setSavingRunnable", (Runnable) () -> ChatPolish.saveSettings(
                    new ChatSettings(draft[0], draft[1], draft[2])));
            return call(builder, "build");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot open Chat Polish settings. Check Cloth Config compatibility.", e);
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
                Class<?> type = types[i] == boolean.class ? Boolean.class : types[i];
                if (args[i] != null && !type.isInstance(args[i])) matches = false;
            }
            if (matches) return method.invoke(target, args);
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }
}
