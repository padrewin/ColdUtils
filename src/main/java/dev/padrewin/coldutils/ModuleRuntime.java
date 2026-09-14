package dev.padrewin.coldutils;

import java.util.*;
import static dev.padrewin.coldutils.RuntimeBridge.*;

public final class ModuleRuntime {
    public static final SpatialState spatial=new SpatialState();
    public record Storage(SpatialState.Point point,String kind) {}
    public static final Map<String,List<Storage>> storage=new LinkedHashMap<>();
    private static Object lastWorld, lastPlayer, gammaOption, darknessOption;
    private static boolean cameraActive;
    private static double cameraX,cameraY,cameraZ;
    private static float cameraYaw,cameraPitch;
    private static long cameraFrame;
    private static int scanCursor;
    private static final PanicGesture gesture=new PanicGesture();
    private ModuleRuntime() {}
    public static boolean freecam() { return cameraActive && ColdUtils.modules().active("freecam"); }
    public static void suspend() { cameraActive=false; spatial.clear();storage.clear(); }
    public static void panic() { ColdUtils.modules().togglePanic(); suspend(); ColdUtils.saveModules(); }
    public static boolean keyboard(Object event,int action) {
        try {
            int key=((Number)call(event,"key")).intValue();
            Object current=screen();
            if(current!=null && !HubScreen.owns(current)) { gesture.reset(); return false; }
            if(HubScreen.capturing()) { gesture.reset(); return false; }
            ModuleConfig c=ColdUtils.modules();
            if(key!=c.get("gui.key") && (c.get("panic.key")<0 || key!=c.get("panic.key")))return false;
            if(action==1) {
                if(key==c.get("panic.key") || gesture.press(System.nanoTime()/1_000_000,c.get("panic.window"))) {
                    panic(); if(HubScreen.owns(current))screen(null);
                } else if(HubScreen.owns(current))screen(null); else screen(HubScreen.create(null));
            }
            return true;
        } catch(ReflectiveOperationException | RuntimeException e) { fail("Keyboard shortcut",e);return false; }
    }
    public static void tick() {
        try {
            Object w=world(),p=player();
            if(w!=lastWorld || p!=lastPlayer) { suspend();lastWorld=w;lastPlayer=p;scanCursor=0; }
            if(w==null || p==null)return;
            ModuleConfig c=ColdUtils.modules();
            Object options=get(client(),"options");
            gammaOption=call(options,"gamma"); darknessOption=call(options,"darknessEffectScale");
            if(c.panic()) { suspend(); return; }
            updateCamera(p,c);
            SpatialState.Point local=point(p);
            List<SpatialState.Player> players=new ArrayList<>();
            for(Object other:(List<?>)call(w,"players")) if(other!=p) {
                players.add(new SpatialState.Player((UUID)call(other,"getUUID"),String.valueOf(call(call(other,"getName"),"getString")),point(other)));
            }
            spatial.update(players,local,System.currentTimeMillis(),c);
            if(c.active("trails") && c.flag("trails.self")) {
                UUID id=(UUID)call(p,"getUUID");
                Deque<SpatialState.Trail> path=spatial.trails.computeIfAbsent(id,k->new ArrayDeque<>());
                if(!path.isEmpty() && path.getLast().point().distanceSquared(local)>256)path.clear();
                if(path.isEmpty() || path.getLast().point().distanceSquared(local)>0.01)path.addLast(new SpatialState.Trail(local,System.currentTimeMillis()));
            }
            if(c.active("storage"))scanStorage(w,local,c);else storage.clear();
        } catch(ReflectiveOperationException | RuntimeException e) { fail("Module tick",e); }
    }
    private static void updateCamera(Object p,ModuleConfig c) throws ReflectiveOperationException {
        if(!c.active("freecam") || (boolean)call(p,"isDeadOrDying")) { cameraActive=false;return; }
        if(cameraActive)return;
        SpatialState.Point pos=point(p);cameraX=pos.x();cameraY=number(p,"getEyeY");cameraZ=pos.z();
        cameraYaw=(float)number(p,"getYRot");cameraPitch=(float)number(p,"getXRot");cameraFrame=System.nanoTime();cameraActive=true;
    }
    // Integrated per rendered frame: stepping only at 20 ticks/s made the camera jump between positions.
    private static void moveCamera() throws ReflectiveOperationException {
        long now=System.nanoTime();double seconds=Math.min(0.1,(now-cameraFrame)/1e9);cameraFrame=now;
        if(seconds<=0 || screen()!=null || !(boolean)call(client(),"isWindowActive"))return;
        ModuleConfig c=ColdUtils.modules();
        double forward=(key(c.get("freecam.forward"))?1:0)-(key(c.get("freecam.back"))?1:0);
        double right=(key(c.get("freecam.right"))?1:0)-(key(c.get("freecam.left"))?1:0);
        double up=(key(c.get("freecam.up"))?1:0)-(key(c.get("freecam.down"))?1:0);
        double length=Math.sqrt(forward*forward+right*right+up*up);if(length==0)return;
        double speed=c.get("freecam.speed")*seconds/length,yaw=Math.toRadians(cameraYaw),pitch=Math.toRadians(cameraPitch);
        cameraX+=(-Math.sin(yaw)*Math.cos(pitch)*forward-Math.cos(yaw)*right)*speed;
        cameraY+=(-Math.sin(pitch)*forward+up)*speed;
        cameraZ+=(Math.cos(yaw)*Math.cos(pitch)*forward-Math.sin(yaw)*right)*speed;
    }
    public static boolean turn(Object entity,double dx,double dy) {
        if(!freecam())return false;
        try { if(entity!=player())return false; }
        catch(ReflectiveOperationException e){return false;}
        double sensitivity=ColdUtils.modules().get("freecam.sensitivity")/100.0;
        cameraYaw+=(float)(dx*0.15*sensitivity);cameraPitch=(float)Math.max(-90,Math.min(90,cameraPitch+dy*0.15*sensitivity));return true;
    }
    public static void camera(Object camera) {
        if(!freecam())return;
        try { moveCamera();call(camera,"setRotation",cameraYaw,cameraPitch);call(camera,"setPosition",cameraX,cameraY,cameraZ);set(camera,"detached",true); }
        catch(ReflectiveOperationException e){cameraActive=false;fail("Freecam camera",e);}
    }
    public static void clearInput(Object input) {
        if(!freecam())return;
        try {
            set(input,"keyPresses",get(type("net.minecraft.world.entity.player.Input"),"EMPTY"));
            set(input,"moveVector",get(type("net.minecraft.world.phys.Vec2"),"ZERO"));
        } catch(ReflectiveOperationException e){cameraActive=false;fail("Freecam input",e);}
    }
    public static Object optionValue(Object option,Object original) {
        if(lastWorld==null || !ColdUtils.modules().active("fullbright"))return original;
        if(option==gammaOption)return 1.0+15.0*ColdUtils.modules().get("fullbright.strength")/100.0;
        if(option==darknessOption)return 0.0;
        return original;
    }
    private static void scanStorage(Object world,SpatialState.Point local,ModuleConfig c) throws ReflectiveOperationException {
        int radius=(c.get("storage.range")+15)/16, cx=(int)Math.floor(local.x()/16),cz=(int)Math.floor(local.z()/16);
        int side=radius*2+1,total=side*side;
        storage.entrySet().removeIf(e->{String[] xy=e.getKey().split(":");return Math.abs(Integer.parseInt(xy[0])-cx)>radius || Math.abs(Integer.parseInt(xy[1])-cz)>radius;});
        for(int n=0;n<4;n++) {
            int index=scanCursor++%total,x=cx+index%side-radius,z=cz+index/side-radius;
            String key=x+":"+z;
            if(!(boolean)call(world,"hasChunk",x,z)){storage.remove(key);continue;}
            Object chunk=call(world,"getChunk",x,z);
            Map<?,?> entities=(Map<?,?>)call(chunk,"getBlockEntities");
            List<Storage> found=new ArrayList<>();
            for(Object be:entities.values()) {
                Object state=call(be,"getBlockState");String name=state.toString().toLowerCase(Locale.ROOT);
                String kind=name.contains("ender_chest")?"ender":name.contains("shulker_box")?"shulker":name.contains("chest")?"chest":name.contains("barrel")?"barrel":name.contains("hopper")?"hopper":(name.contains("furnace") || name.contains("smoker"))?"furnace":name.contains("spawner")?"spawner":null;
                if(kind!=null) {Object pos=call(be,"getBlockPos");found.add(new Storage(point(pos),kind));}
            }
            storage.put(key,found);
        }
    }
}
