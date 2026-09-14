package dev.padrewin.coldutils;

import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.Method;
import java.util.List;

/** Local chat command: cancellation is unconditional once the command is recognized. */
public final class ColdNear {
    private ColdNear() {}

    static boolean recognizes(String message) {
        return message.strip().split("\\s+", 2)[0].equalsIgnoreCase(".coldnear");
    }

    static int radius(String message) {
        String[] parts = message.strip().split("\\s+");
        if (!recognizes(message) || parts.length > 2) throw new IllegalArgumentException();
        if (parts.length == 1) return 256;
        if (!parts[1].matches("[0-9]+")) throw new IllegalArgumentException();
        int radius = Integer.parseInt(parts[1]);
        if (radius < 1 || radius > 256) throw new IllegalArgumentException();
        return radius;
    }

    public static boolean allowChat(String message) {
        if (!recognizes(message)) return true;
        try {
            Access a = new Access();
            Object client = a.client.getMethod(a.method("net.minecraft.class_310", "method_1551",
                    "()Lnet/minecraft/class_310;", "getInstance")).invoke(null);
            Object local = a.client.getField(a.field("field_1724", "Lnet/minecraft/class_746;", "player")).get(client);
            Object world = a.client.getField(a.field("field_1687", "Lnet/minecraft/class_638;", "level")).get(client);
            if (local == null || world == null) return false;
            int radius;
            try { radius = radius(message); }
            catch (IllegalArgumentException e) {
                a.send(local, "Usage: .coldnear [1-256]");
                return false;
            }
            try {
                Method players = world.getClass().getMethod(a.method("net.minecraft.class_1924", "method_18456",
                        "()Ljava/util/List;", "players"));
                Method distance = a.entity.getMethod(a.method("net.minecraft.class_1297", "method_5858",
                        "(Lnet/minecraft/class_1297;)D", "distanceToSqr"), a.entity);
                int count = 0;
                for (Object player : (List<?>) players.invoke(world)) {
                    if (player != local && (double) distance.invoke(player, local) <= (double) radius * radius) count++;
                }
                a.send(local, "There are " + count + " player(s) near you.", radius + " blocks");
            } catch (ReflectiveOperationException | RuntimeException e) {
                a.send(local, "Nearby lookup unavailable on this Minecraft version.");
                throw e;
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            System.getLogger("ColdUtils").log(System.Logger.Level.ERROR, "Could not execute .coldnear", e);
        }
        return false;
    }

    private static final class Access {
        final net.fabricmc.loader.api.MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
        final boolean mapped = mappings.getNamespaces().contains("intermediary");
        final Class<?> client = type("net.minecraft.class_310", "net.minecraft.client.Minecraft");
        final Class<?> entity = type("net.minecraft.class_1297", "net.minecraft.world.entity.Entity");
        final Class<?> text = type("net.minecraft.class_2561", "net.minecraft.network.chat.Component");
        final Class<?> mutable = type("net.minecraft.class_5250", "net.minecraft.network.chat.MutableComponent");
        final Method literal = text.getMethod(method("net.minecraft.class_2561", "method_43470",
                "(Ljava/lang/String;)Lnet/minecraft/class_5250;", "literal"), String.class);
        final Method color = mutable.getMethod(method("net.minecraft.class_5250", "method_54663",
                "(I)Lnet/minecraft/class_5250;", "withColor"), int.class);
        final Method append = mutable.getMethod(method("net.minecraft.class_5250", "method_10852",
                "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_5250;", "append"), text);

        Access() throws ReflectiveOperationException {}

        Class<?> type(String intermediary, String mojang) throws ClassNotFoundException {
            return Class.forName(mapped ? mappings.mapClassName("intermediary", intermediary) : mojang);
        }
        String method(String owner, String intermediary, String descriptor, String mojang) {
            return mapped ? mappings.mapMethodName("intermediary", owner, intermediary, descriptor) : mojang;
        }
        String field(String intermediary, String descriptor, String mojang) {
            return mapped ? mappings.mapFieldName("intermediary", "net.minecraft.class_310", intermediary, descriptor) : mojang;
        }
        Object colored(String value, int rgb) throws ReflectiveOperationException {
            return color.invoke(literal.invoke(null, value), rgb);
        }
        void send(Object local, String body) throws ReflectiveOperationException { send(local, body, ""); }
        void send(Object local, String body, String suffix) throws ReflectiveOperationException {
            Object result = literal.invoke(null, "");
            append.invoke(result, colored("「", 0x555555));
            int[] gradient = {0x9BEFFF, 0x8CD8FF, 0x7EC0FF, 0x6FA9FF, 0x6091FF,
                    0x517AFF, 0x4362FF, 0x344BFF, 0x2533FF};
            for (int i = 0; i < gradient.length; i++) {
                append.invoke(result, colored("ColdUtils".substring(i, i + 1), gradient[i]));
            }
            append.invoke(result, colored("」", 0x555555));
            append.invoke(result, colored("» ", 0xAAAAAA));
            append.invoke(result, colored(body, 0xFFFFFF));
            if (!suffix.isEmpty()) {
                append.invoke(result, colored(" (", 0x555555));
                append.invoke(result, colored(suffix, 0xAAAAAA));
                append.invoke(result, colored(")", 0x555555));
            }
            if (mapped) {
                local.getClass().getMethod(method("net.minecraft.class_1657", "method_7353",
                        "(Lnet/minecraft/class_2561;Z)V", "displayClientMessage"), text, boolean.class)
                        .invoke(local, result, false);
            } else {
                local.getClass().getMethod("sendSystemMessage", text).invoke(local, result);
            }
        }
    }
}
