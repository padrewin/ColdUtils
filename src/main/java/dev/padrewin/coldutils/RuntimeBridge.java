package dev.padrewin.coldutils;

import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Small cached reflection bridge: production intermediary 1.21.11 and Mojang 26.x. */
public final class RuntimeBridge {
    private static final Properties NAMES = new Properties();
    private static final Map<String,String> LOGICAL = new HashMap<>();
    private static final Map<String,Method> METHODS = new ConcurrentHashMap<>();
    private static final Map<String,Field> FIELDS = new ConcurrentHashMap<>();
    private static final boolean MAPPED = FabricLoader.getInstance().getMappingResolver().getNamespaces().contains("intermediary");
    static {
        try (var in = RuntimeBridge.class.getResourceAsStream("/coldutils-runtime-1.21.11.properties")) { NAMES.load(in); }
        catch (Exception e) { throw new ExceptionInInitializerError(e); }
        NAMES.forEach((k,v) -> { if (k.toString().startsWith("C.")) LOGICAL.put(v.toString(),k.toString().substring(2)); });
    }
    // Class.forName goes through the mod class loader; these lookups run several times per frame.
    private static final Map<String,Class<?>> TYPES = new ConcurrentHashMap<>();
    private static volatile Object client;
    private static volatile long window;
    private RuntimeBridge() {}
    public static Class<?> type(String name) throws ReflectiveOperationException {
        Class<?> cached = TYPES.get(name); if (cached != null) return cached;
        Class<?> loaded = Class.forName(MAPPED ? NAMES.getProperty("C." + name,name) : name);
        TYPES.put(name,loaded); return loaded;
    }
    private static Set<String> names(Class<?> type, String kind, String name) {
        Set<String> result = new HashSet<>(List.of(name.split("\\|")));
        if (MAPPED) for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            String logical = LOGICAL.get(c.getName());
            if (logical != null) for (String n : name.split("\\|")) result.addAll(List.of(NAMES.getProperty(kind + logical + "." + n,n).split(",")));
        }
        return result;
    }
    public static Method method(Class<?> cls, String name, Object... args) throws ReflectiveOperationException {
        StringBuilder signature = new StringBuilder(cls.getName()).append('.').append(name);
        for (Object a : args) signature.append(',').append(a == null ? "null" : a.getClass().getName());
        String key = signature.toString();
        Method cached = METHODS.get(key); if (cached != null) return cached;
        Set<String> candidates = names(cls,"M.",name);
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) for (Method m : c.getDeclaredMethods()) {
            if (!candidates.contains(m.getName()) || !accepts(m.getParameterTypes(),args)) continue;
            m.setAccessible(true); METHODS.put(key,m); return m;
        }
        for (Method m : cls.getMethods()) if (candidates.contains(m.getName()) && accepts(m.getParameterTypes(),args)) {
            m.setAccessible(true); METHODS.put(key,m); return m;
        }
        throw new NoSuchMethodException(cls.getName() + "." + name + Arrays.toString(args));
    }
    private static boolean accepts(Class<?>[] types, Object[] args) {
        if (types.length != args.length) return false;
        for (int i=0;i<types.length;i++) {
            Class<?> t = types[i]; if (t.isPrimitive()) t = switch(t.getName()) {
                case "int" -> Integer.class; case "boolean" -> Boolean.class; case "double" -> Double.class;
                case "float" -> Float.class; case "long" -> Long.class; default -> t;
            };
            if (args[i] != null && !t.isInstance(args[i])) return false;
        }
        return true;
    }
    public static Object call(Object target, String name, Object... args) throws ReflectiveOperationException {
        Class<?> cls = target instanceof Class<?> c ? c : target.getClass();
        return method(cls,name,args).invoke(target instanceof Class<?> ? null : target,args);
    }
    public static Field field(Class<?> cls, String name) throws ReflectiveOperationException {
        String key = cls.getName()+"."+name; Field cached=FIELDS.get(key); if(cached!=null)return cached;
        Set<String> candidates=names(cls,"F.",name);
        for(Class<?> c=cls;c!=null;c=c.getSuperclass()) for(Field f:c.getDeclaredFields()) if(candidates.contains(f.getName())) {
            f.setAccessible(true); FIELDS.put(key,f); return f;
        }
        throw new NoSuchFieldException(key);
    }
    public static Object get(Object target,String name) throws ReflectiveOperationException {
        return field(target instanceof Class<?> c ? c : target.getClass(),name).get(target instanceof Class<?> ? null : target);
    }
    public static void set(Object target,String name,Object value) throws ReflectiveOperationException { field(target.getClass(),name).set(target,value); }
    public static Object client() throws ReflectiveOperationException {
        Object mc=client; if(mc==null)client=mc=call(type("net.minecraft.client.Minecraft"),"getInstance"); return mc;
    }
    public static Object player() throws ReflectiveOperationException { return get(client(),"player"); }
    public static Object world() throws ReflectiveOperationException { return get(client(),"level"); }
    public static Object screen() throws ReflectiveOperationException {
        Object mc=client(); try { return get(mc,"screen"); } catch(NoSuchFieldException e) { return call(get(mc,"gui"),"screen"); }
    }
    public static void screen(Object value) throws ReflectiveOperationException {
        Object mc=client(); try { call(mc,"setScreen",value); } catch(NoSuchMethodException e) { call(get(mc,"gui"),"setScreen",value); }
    }
    public static Object text(String text) throws ReflectiveOperationException { return call(type("net.minecraft.network.chat.Component"),"literal",text); }
    public static long window() throws ReflectiveOperationException {
        long handle=window; if(handle==0)window=handle=((Number)call(call(client(),"getWindow"),"getWindow|handle")).longValue(); return handle;
    }
    public static boolean key(int key) throws ReflectiveOperationException { return key >= 32 && (int)call(type("org.lwjgl.glfw.GLFW"),"glfwGetKey",window(),key)==1; }
    public static String keyName(int key) {
        if(key<0)return "Unbound";
        try { Object name=call(Class.forName("org.lwjgl.glfw.GLFW"),"glfwGetKeyName",key,0); if(name!=null)return name.toString().toUpperCase(Locale.ROOT); }
        catch(ReflectiveOperationException ignored) {}
        return switch(key){case 32->"Space";case 340->"Left Shift";case 344->"Right Shift";case 256->"Escape";default->"Key "+key;};
    }
    public static double number(Object o,String name) throws ReflectiveOperationException { return ((Number)call(o,name)).doubleValue(); }
    public static SpatialState.Point point(Object entity) throws ReflectiveOperationException {
        return new SpatialState.Point(number(entity,"getX"),number(entity,"getY"),number(entity,"getZ"));
    }
    public static Object camera() throws ReflectiveOperationException { return call(get(client(),"gameRenderer"),"getMainCamera|mainCamera"); }
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    public static void fail(String feature,Throwable e) { if(WARNED.add(feature))System.getLogger("ColdUtils").log(System.Logger.Level.ERROR,feature+" failed",e); }
}
