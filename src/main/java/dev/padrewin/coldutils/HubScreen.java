package dev.padrewin.coldutils;

import org.objectweb.asm.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import static dev.padrewin.coldutils.RuntimeBridge.*;
import static org.objectweb.asm.Opcodes.*;

/** A native Minecraft Screen generated against the current runtime's screen descriptors. */
public final class HubScreen {
    private static Class<?> generated;
    private static Object instance,parent;
    private static String tab="Logout spots",binding,colorKey;
    private static int page,focus;
    private static final List<Hit> hits=new ArrayList<>();
    private record Hit(int x,int y,int w,int h,Runnable action) {}
    private static final String[] TABS={"Logout spots","Trails","Storage ESP","Freecam","Fullbright","Player ESP","Chat","General"};
    private static final int RED=0xFFFF354B,WHITE=0xFFF2F2F4,MUTED=0xFFA4A4AF;
    private HubScreen() {}
    public static boolean owns(Object screen){return screen!=null && generated!=null && generated.isInstance(screen);}
    public static boolean capturing(){return binding!=null || colorKey!=null;}
    public static Object create(Object previous) {
        try {
            if(generated==null)generated=generate();
            parent=previous;binding=null;colorKey=null;page=0;focus=0;
            instance=generated.getConstructor(type("net.minecraft.network.chat.Component")).newInstance(text("ColdUtils"));
            return instance;
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot create ColdUtils GUI",e);}
    }
    private static Class<?> generate() throws ReflectiveOperationException {
        Class<?> screen=type("net.minecraft.client.gui.screens.Screen"),component=type("net.minecraft.network.chat.Component");
        String name="dev/padrewin/coldutils/GeneratedHubScreen",base=org.objectweb.asm.Type.getInternalName(screen),hub=org.objectweb.asm.Type.getInternalName(HubScreen.class);
        ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);
        w.visit(V21,ACC_PUBLIC|ACC_FINAL,name,null,base,null);
        MethodVisitor ctor=w.visitMethod(ACC_PUBLIC,"<init>","("+org.objectweb.asm.Type.getDescriptor(component)+")V",null,null);
        ctor.visitCode();ctor.visitVarInsn(ALOAD,0);ctor.visitVarInsn(ALOAD,1);ctor.visitMethodInsn(INVOKESPECIAL,base,"<init>","("+org.objectweb.asm.Type.getDescriptor(component)+")V",false);ctor.visitInsn(RETURN);ctor.visitMaxs(0,0);ctor.visitEnd();
        // Both supported families use one graphics object, mouse X/Y and frame delta.
        Method render=Arrays.stream(screen.getMethods()).filter(m -> m.getParameterCount()==4 && m.getParameterTypes()[1]==int.class && m.getParameterTypes()[2]==int.class && m.getParameterTypes()[3]==float.class && m.getReturnType()==void.class && !Modifier.isFinal(m.getModifiers()))
                .filter(m -> m.getName().equals("render") || m.getName().equals("extractRenderState") || m.getName().equals("method_25394")).findFirst().orElseThrow(()->new NoSuchMethodException("Screen render"));
        MethodVisitor draw=w.visitMethod(ACC_PUBLIC,render.getName(),org.objectweb.asm.Type.getMethodDescriptor(render),null,null);
        draw.visitCode();draw.visitVarInsn(ALOAD,0);draw.visitVarInsn(ALOAD,1);draw.visitVarInsn(ILOAD,2);draw.visitVarInsn(ILOAD,3);draw.visitVarInsn(FLOAD,4);
        draw.visitMethodInsn(INVOKESTATIC,hub,"render","(Ljava/lang/Object;Ljava/lang/Object;IIF)V",false);draw.visitInsn(RETURN);draw.visitMaxs(0,0);draw.visitEnd();
        Method pause=method(screen,"isPauseScreen");MethodVisitor p=w.visitMethod(ACC_PUBLIC,pause.getName(),"()Z",null,null);p.visitCode();p.visitInsn(ICONST_0);p.visitInsn(IRETURN);p.visitMaxs(0,0);p.visitEnd();
        Class<?> key=type("net.minecraft.client.input.KeyEvent"),mouse=type("net.minecraft.client.input.MouseButtonEvent");
        Method km=method(screen,"keyPressed",key.getConstructors()[0].newInstance(0,0,0));
        delegateEvent(w,hub,km,"key",false);
        // Resolve mouse callback by the exact event class; its record constructor differs between releases.
        Method mm=Arrays.stream(screen.getMethods()).filter(m->m.getParameterCount()==2 && m.getParameterTypes()[0]==mouse && m.getParameterTypes()[1]==boolean.class && m.getReturnType()==boolean.class).findFirst().orElseThrow();
        delegateEvent(w,hub,mm,"click",true);
        Method close=method(screen,"onClose");MethodVisitor cl=w.visitMethod(ACC_PUBLIC,close.getName(),"()V",null,null);cl.visitCode();cl.visitMethodInsn(INVOKESTATIC,hub,"close","()V",false);cl.visitInsn(RETURN);cl.visitMaxs(0,0);cl.visitEnd();
        w.visitEnd();return MethodHandles.lookup().defineClass(w.toByteArray());
    }
    private static void delegateEvent(ClassWriter w,String hub,Method source,String target,boolean ignoredSecond) {
        MethodVisitor m=w.visitMethod(ACC_PUBLIC,source.getName(),org.objectweb.asm.Type.getMethodDescriptor(source),null,null);
        m.visitCode();m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESTATIC,hub,target,"(Ljava/lang/Object;)Z",false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
    }
    public static void close(){binding=null;colorKey=null;try{screen(parent);}catch(ReflectiveOperationException e){fail("Close GUI",e);}}
    private static void button(Canvas d,int x,int y,int w,int h,String label,Runnable action,int mx,int my,boolean selected) {
        boolean hover=mx>=x&&my>=y&&mx<x+w&&my<y+h;
        d.fill(x,y,w,h,selected?0xFF46212B:hover?0xFF303039:0xFF222228);
        if(selected)d.fill(x,y,2,h,RED);
        d.text(d.fit(label,w-12),x+6,y+(h-8)/2,selected?WHITE:MUTED);
        hits.add(new Hit(x,y,w,h,action));
    }
    private static void change(ModuleConfig.Option o,int value){ColdUtils.modules().set(o.key(),value);ColdUtils.saveModules();}
    public static void render(Object self,Object graphics,int mx,int my,float delta) {
        try {
            Canvas d=new Canvas(graphics);hits.clear();
            int w=Math.min(570,d.width-12),h=Math.min(338,d.height-12),x=(d.width-w)/2,y=(d.height-h)/2;
            int side=Math.min(126,w/3),body=x+side+14,bw=w-side-26;
            d.fill(0,0,d.width,d.height,0x99000000);d.fill(x,y,w,h,0xFF111114);d.border(x,y,w,h,0xFF3D2028);d.fill(x,y,w,2,RED);
            d.text("COLD",x+12,y+14,WHITE);d.text("UTILS",x+39,y+14,RED);
            d.text(ColdUtils.modules().panic()?"SUSPENDED":"CONTROL PANEL",body,y+14,ColdUtils.modules().panic()?RED:MUTED);
            int tabHeight=Math.min(25,Math.max(12,(h-78)/TABS.length));
            for(int i=0;i<TABS.length;i++){String name=TABS[i];button(d,x+8,y+38+i*tabHeight,side-8,tabHeight-2,name,()->{tab=name;page=0;focus=0;},mx,my,tab.equals(name));}
            button(d,x+8,y+h-31,side-8,21,ColdUtils.modules().panic()?"Restore modules":"PANIC",ModuleRuntime::panic,mx,my,true);
            button(d,x+w-26,y+9,18,18,"X",HubScreen::close,mx,my,false);
            if(colorKey!=null){renderColor(d,body,y+43,bw,mx,my);return;}
            if(binding!=null){d.text("Press a key...",body,y+53,WHITE);d.text("Escape cancels",body,y+73,MUTED);return;}
            List<ModuleConfig.Option> options=ModuleConfig.OPTIONS.stream().filter(o->o.tab().equals(tab)).toList();
            int rowHeight=29,rows=Math.max(1,(h-107)/rowHeight),pages=Math.max(1,(options.size()+rows-1)/rows);
            page=Math.max(0,Math.min(page,pages-1));
            for(int i=page*rows;i<Math.min(options.size(),(page+1)*rows);i++) {
                ModuleConfig.Option o=options.get(i);int ry=y+39+(i-page*rows)*rowHeight;int val=ColdUtils.modules().get(o.key());
                d.text(d.fit(o.label(),bw-6),body,ry+2,i==focus?WHITE:MUTED);
                switch(o.kind()) {
                    case TOGGLE -> button(d,body,ry+12,54,14,val==1?"ON":"OFF",()->change(o,1-val),mx,my,val==1);
                    case KEY -> button(d,body,ry+12,bw-8,14,keyName(val),()->binding=o.key(),mx,my,false);
                    case COLOR -> {d.fill(body,ry+13,18,12,0xFF000000|val);button(d,body+23,ry+12,85,14,String.format("#%06X",val),()->colorKey=o.key(),mx,my,false);}
                    case NUMBER -> {
                        button(d,body,ry+12,20,14,"-",()->change(o,Math.max(o.min(),val-o.step())),mx,my,false);
                        d.text(Integer.toString(val),body+27,ry+16,WHITE);
                        button(d,body+75,ry+12,20,14,"+",()->change(o,Math.min(o.max(),val+o.step())),mx,my,false);
                        button(d,body+102,ry+12,45,14,"Reset",()->change(o,o.initial()),mx,my,false);
                    }
                }
            }
            int fy=y+h-57;
            button(d,body,fy,24,18,"<",()->page=Math.max(0,page-1),mx,my,false);
            d.text((page+1)+" / "+pages,body+31,fy+5,MUTED);
            button(d,body+75,fy,24,18,">",()->page=Math.min(pages-1,page+1),mx,my,false);
            if(tab.equals("Logout spots"))button(d,body+108,fy,Math.max(45,bw-108),18,"Clear markers",()->ModuleRuntime.spatial.marks.clear(),mx,my,false);
            d.text(d.fit(ColdUtils.saveError()==null?"Auto-saved  |  4 taps: panic / restore":ColdUtils.saveError(),bw),body,y+h-28,ColdUtils.saveError()==null?MUTED:RED);
            if(tab.equals("Freecam"))d.text(d.fit("Body stays in world. Loaded terrain only.",bw),body,y+h-15,MUTED);
            else if(tab.equals("Logout spots"))d.text(d.fit("Last seen does not confirm a logout.",bw),body,y+h-15,MUTED);
        }catch(ReflectiveOperationException|RuntimeException e){fail("GUI render",e);}
    }
    private static void renderColor(Canvas d,int x,int y,int w,int mx,int my) {
        int color=ColdUtils.modules().get(colorKey);
        d.fill(x,y,Math.min(w,120),22,0xFF000000|color);d.text(String.format("#%06X",color),x,y+31,WHITE);
        String[] labels={"Red","Green","Blue"};
        for(int i=0;i<3;i++) {
            int shift=(2-i)*8,val=(color>>shift)&255,cy=y+54+i*29;
            d.text(labels[i]+" "+val,x,cy,WHITE);
            for(int j=0;j<2;j++){int dir=j==0?-1:1;button(d,x+70+j*25,cy-3,22,16,j==0?"-":"+",()->setChannel(shift,Math.max(0,Math.min(255,val+dir))),mx,my,false);}
            int bar=Math.max(10,w-134);
            for(int px=0;px<bar;px++){int v=px*255/Math.max(1,bar-1);d.fill(x+128+px,cy-3,1,16,0xFF000000|(v<<shift));}
            hits.add(new Hit(x+128,cy-3,bar,16,()->setChannel(shift,Math.max(0,Math.min(255,(mx-x-128)*255/Math.max(1,bar-1))))));
        }
        button(d,x,y+147,90,20,"Done",()->colorKey=null,mx,my,true);
    }
    private static void setChannel(int shift,int value){int old=ColdUtils.modules().get(colorKey);ColdUtils.modules().set(colorKey,(old&~(255<<shift))|(value<<shift));ColdUtils.saveModules();}
    public static boolean click(Object event) {
        try {
            if(((Number)call(event,"button")).intValue()!=0)return true;
            double x=number(event,"x"),y=number(event,"y");
            for(Hit h:List.copyOf(hits))if(x>=h.x&&x<h.x+h.w&&y>=h.y&&y<h.y+h.h){h.action.run();break;}
        }catch(ReflectiveOperationException|RuntimeException e){fail("GUI click",e);}return true;
    }
    public static boolean key(Object event) {
        try {
            int key=((Number)call(event,"key")).intValue();
            if(binding!=null) {
                if(key==256){binding=null;return true;}
                if(key==261 && binding.equals("panic.key")){ColdUtils.modules().set(binding,-1);binding=null;ColdUtils.saveModules();return true;}
                if(key>=32 && key<=348){ColdUtils.modules().set(binding,key);binding=null;ColdUtils.saveModules();}return true;
            }
            if(key==256){if(colorKey!=null)colorKey=null;else close();return true;}
            if(colorKey!=null)return true;
            List<ModuleConfig.Option> options=ModuleConfig.OPTIONS.stream().filter(o->o.tab().equals(tab)).toList();
            if(key==258){int index=(Arrays.asList(TABS).indexOf(tab)+1)%TABS.length;tab=TABS[index];page=0;focus=0;return true;}
            if(key==264)focus=Math.min(options.size()-1,focus+1);
            if(key==265)focus=Math.max(0,focus-1);
            if(key==266)page=Math.max(0,page-1);if(key==267)page++;
            ModuleConfig.Option o=options.get(Math.min(focus,options.size()-1));int val=ColdUtils.modules().get(o.key());
            if(key==257 || key==32) {switch(o.kind()){case TOGGLE->change(o,1-val);case COLOR->colorKey=o.key();case KEY->binding=o.key();default->{}}}
            if(o.kind()==ModuleConfig.Kind.NUMBER && (key==263||key==262))change(o,Math.max(o.min(),Math.min(o.max(),val+(key==262?o.step():-o.step()))));
        }catch(ReflectiveOperationException|RuntimeException e){fail("GUI keyboard",e);}return true;
    }
}
