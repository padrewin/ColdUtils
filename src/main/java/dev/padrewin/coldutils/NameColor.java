package dev.padrewin.coldutils;

import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.Method;

/** Builds a separate display component so server/team text is never mutated. */
public final class NameColor {
    private static Access access;
    private static boolean failed;

    private NameColor() {}

    public static Object apply(Object original, int color) {
        if (original == null || failed) return original;
        try {
            if (access == null) access = new Access();
            // Flatten server prefixes too, so their nested styles cannot override the chosen RGB.
            Object result = access.literal.invoke(null, access.string.invoke(original));
            return access.color.invoke(result, color);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            failed = true;
            System.getLogger("ColdUtils").log(System.Logger.Level.ERROR,
                    "Player name coloring unavailable on this Minecraft runtime", e);
            return original;
        }
    }

    private static final class Access {
        final Method string;
        final Method literal;
        final Method color;

        Access() throws ReflectiveOperationException {
            var mappings = FabricLoader.getInstance().getMappingResolver();
            boolean intermediary = mappings.getNamespaces().contains("intermediary");
            Class<?> text = Class.forName(intermediary
                    ? mappings.mapClassName("intermediary", "net.minecraft.class_2561")
                    : "net.minecraft.network.chat.Component");
            Class<?> mutable = Class.forName(intermediary
                    ? mappings.mapClassName("intermediary", "net.minecraft.class_5250")
                    : "net.minecraft.network.chat.MutableComponent");
            string = text.getMethod("getString");
            literal = text.getMethod(intermediary
                    ? mappings.mapMethodName("intermediary", "net.minecraft.class_2561", "method_43470",
                    "(Ljava/lang/String;)Lnet/minecraft/class_5250;") : "literal", String.class);
            color = mutable.getMethod(intermediary
                    ? mappings.mapMethodName("intermediary", "net.minecraft.class_5250", "method_54663",
                    "(I)Lnet/minecraft/class_5250;") : "withColor", int.class);
        }
    }
}
