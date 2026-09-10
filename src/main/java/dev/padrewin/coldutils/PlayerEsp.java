package dev.padrewin.coldutils;

import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Read-only client entity queries. Never modifies entity flags or scoreboard teams. */
public final class PlayerEsp {
    private static Access access;
    private static boolean failed;

    private PlayerEsp() {}

    public static boolean matches(Object entity) {
        ColdUtilsSettings settings = ColdUtils.settings();
        if ((!settings.espEnabled() && !settings.espNames()) || entity == null || failed) return false;
        try {
            if (access == null) access = new Access();
            if (!access.playerType.isInstance(entity)) return false;
            Object local = access.localPlayer.get(access.client.invoke(null));
            if (local == null || entity == local) return false;
            if (settings.espInvisibleOnly() && !(boolean) access.invisible.invoke(entity)) return false;
            double distance = (double) access.distance.invoke(entity, local);
            return distance <= (double) settings.espRange() * settings.espRange();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            failed = true;
            System.getLogger("ColdUtils").log(System.Logger.Level.ERROR,
                    "Player ESP disabled: incompatible Minecraft runtime", e);
            return false;
        }
    }

    private static final class Access {
        final Class<?> playerType;
        final Method client;
        final Field localPlayer;
        final Method invisible;
        final Method distance;

        Access() throws ReflectiveOperationException {
            var mappings = FabricLoader.getInstance().getMappingResolver();
            boolean intermediary = mappings.getNamespaces().contains("intermediary");
            Class<?> entityType = Class.forName(intermediary
                    ? mappings.mapClassName("intermediary", "net.minecraft.class_1297")
                    : "net.minecraft.world.entity.Entity");
            playerType = Class.forName(intermediary
                    ? mappings.mapClassName("intermediary", "net.minecraft.class_742")
                    : "net.minecraft.client.player.AbstractClientPlayer");
            Class<?> minecraft = Class.forName(intermediary
                    ? mappings.mapClassName("intermediary", "net.minecraft.class_310")
                    : "net.minecraft.client.Minecraft");
            client = minecraft.getMethod(intermediary
                    ? mappings.mapMethodName("intermediary", "net.minecraft.class_310", "method_1551",
                    "()Lnet/minecraft/class_310;") : "getInstance");
            localPlayer = minecraft.getField(intermediary
                    ? mappings.mapFieldName("intermediary", "net.minecraft.class_310", "field_1724",
                    "Lnet/minecraft/class_746;") : "player");
            invisible = entityType.getMethod(intermediary
                    ? mappings.mapMethodName("intermediary", "net.minecraft.class_1297", "method_5767", "()Z")
                    : "isInvisible");
            distance = entityType.getMethod(intermediary
                    ? mappings.mapMethodName("intermediary", "net.minecraft.class_1297", "method_5858",
                    "(Lnet/minecraft/class_1297;)D") : "distanceToSqr", entityType);
        }
    }
}
