package dev.padrewin.coldutils;

import java.lang.reflect.Constructor;
import java.util.*;
import static dev.padrewin.coldutils.RuntimeBridge.*;

/** Projects loaded world observations using Minecraft's own camera projection. */
public final class WorldOverlay {
    private static Constructor<?> vector;
    private WorldOverlay() {}
    private record Pixel(double x,double y) {}
    private static Pixel project(Canvas d,Object renderer,double x,double y,double z) throws ReflectiveOperationException {
        if(vector==null)vector=type("net.minecraft.world.phys.Vec3").getConstructor(double.class,double.class,double.class);
        Object projected=call(renderer,"projectPointToScreen",vector.newInstance(x,y,z));
        double px=((Number)get(projected,"x")).doubleValue(),py=((Number)get(projected,"y")).doubleValue(),pz=((Number)get(projected,"z")).doubleValue();
        if(!Double.isFinite(px)||!Double.isFinite(py)||pz < -1 || pz > 1)return null;
        return new Pixel((px+1)*d.width/2,(1-py)*d.height/2);
    }
    public static void render(Object graphics) {
        ModuleConfig c=ColdUtils.modules();
        if(c.panic() || (!c.active("storage")&&!c.active("trails")&&!c.active("logout")&&!c.active("freecam")))return;
        try {
            if(world()==null||player()==null||screen()!=null)return;
            Canvas d=new Canvas(graphics);Object renderer=get(client(),"gameRenderer");SpatialState.Point local=point(player());
            if(c.active("storage")) {
                List<ModuleRuntime.Storage> nearest=ModuleRuntime.storage.values().stream().flatMap(Collection::stream)
                        .filter(s->c.flag("storage."+s.kind())&&s.point().distanceSquared(local)<=Math.pow(c.get("storage.range"),2))
                        .sorted(Comparator.comparingDouble(s->s.point().distanceSquared(local))).limit(96).toList();
                for(ModuleRuntime.Storage s:nearest){var p=s.point();int color=c.get("storage."+s.kind()+"Color");box(d,renderer,p.x(),p.y(),p.z(),1,1,color,c.flag("storage.fill"));
                    if(c.flag("storage.labels"))label(d,renderer,p.x()+0.5,p.y()+1.1,p.z()+0.5,s.kind(),color);}
            }
            if(c.active("logout"))for(SpatialState.Mark m:ModuleRuntime.spatial.marks.values()) {
                var p=m.player().point();if(p.distanceSquared(local)>Math.pow(c.get("logout.range"),2))continue;
                box(d,renderer,p.x()-0.3,p.y(),p.z()-0.3,0.6,1.8,c.get("logout.color"),false);
                if(c.flag("logout.labels"))label(d,renderer,p.x(),p.y()+2,p.z(),m.player().name()+" | last seen "+Math.max(0,(System.currentTimeMillis()-m.time())/1000)+"s",c.get("logout.color"));
            }
            if(c.active("trails")) {
                int budget=1800;
                for(Deque<SpatialState.Trail> path:ModuleRuntime.spatial.trails.values()) {
                    Pixel previous=null;for(SpatialState.Trail trail:path) {
                        if(--budget<0)break;
                        var p=trail.point();Pixel next=p.distanceSquared(local)<=Math.pow(c.get("trails.range"),2)?project(d,renderer,p.x(),p.y()+0.05,p.z()):null;
                        if(previous!=null&&next!=null)d.line(previous.x,previous.y,next.x,next.y,c.get("trails.width"),0xCC000000|c.get("trails.color"));previous=next;
                    }
                    if(budget<0)break;
                }
            }
            if(ModuleRuntime.freecam()){d.fill(6,6,134,17,0xC0111114);d.text("FREECAM - body stays put",11,11,0xFFFF354B);}
        }catch(ReflectiveOperationException|RuntimeException e){fail("World overlays",e);}
    }
    private static void label(Canvas d,Object r,double x,double y,double z,String label,int color)throws ReflectiveOperationException {
        Pixel p=project(d,r,x,y,z);if(p==null || p.x<0 || p.x>d.width || p.y<0 || p.y>d.height)return;
        String text=d.fit(label,200);int width=d.textWidth(text),left=(int)p.x-width/2,top=(int)p.y-9;
        d.fill(left-3,top-2,width+6,13,0xB0111114);d.text(text,left,top,0xFF000000|color);
    }
    private static void box(Canvas d,Object r,double x,double y,double z,double size,double height,int color,boolean fill)throws ReflectiveOperationException {
        Pixel[] v=new Pixel[8];for(int i=0;i<8;i++)v[i]=project(d,r,x+((i&1)!=0?size:0),y+((i&2)!=0?height:0),z+((i&4)!=0?size:0));
        if(fill && Arrays.stream(v).allMatch(Objects::nonNull)) {
            int minX=(int)Math.max(0,Arrays.stream(v).mapToDouble(p->p.x).min().orElse(0)),maxX=(int)Math.min(d.width,Arrays.stream(v).mapToDouble(p->p.x).max().orElse(0));
            int minY=(int)Math.max(0,Arrays.stream(v).mapToDouble(p->p.y).min().orElse(0)),maxY=(int)Math.min(d.height,Arrays.stream(v).mapToDouble(p->p.y).max().orElse(0));
            d.fill(minX,minY,maxX-minX,maxY-minY,0x18000000|color);
        }
        for(int i=0;i<8;i++)for(int bit:new int[]{1,2,4})if((i&bit)==0&&v[i]!=null&&v[i|bit]!=null)d.line(v[i].x,v[i].y,v[i|bit].x,v[i|bit].y,1,0xDD000000|color);
    }
}
