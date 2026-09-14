package dev.padrewin.coldutils;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/** Keeps version-specific Minecraft Screen descriptors out of the shared JAR. */
public final class MenuAdapter implements LanguageAdapter {
    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        if (!MenuAdapter.class.getName().equals(value)) {
            throw new LanguageAdapterException("Unknown ColdUtils entrypoint: " + value);
        }
        if (!type.getName().equals("com.terraformersmc.modmenu.api.ModMenuApi")) {
            throw new LanguageAdapterException("Unsupported ColdUtils entrypoint: " + type.getName());
        }
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getName().equals("getModConfigScreenFactory")) {
                        Class<?> factory = method.getReturnType();
                        return Proxy.newProxyInstance(factory.getClassLoader(), new Class<?>[]{factory},
                                (instance, factoryMethod, parameters) -> {
                                    if (factoryMethod.getName().equals("create")) {
                                        return HubScreen.create(parameters[0]);
                                    }
                                    return objectMethod(instance, factoryMethod.getName(), parameters);
                                });
                    }
                    if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method,
                            args == null ? new Object[0] : args);
                    return objectMethod(proxy, method.getName(), args);
                }));
    }

    private static Object objectMethod(Object proxy, String name, Object[] args) {
        return switch (name) {
            case "toString" -> "ColdUtils Mod Menu integration";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(name);
        };
    }
}
