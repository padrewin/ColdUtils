package dev.padrewin.coldutils;

import java.lang.reflect.*;
import static dev.padrewin.coldutils.RuntimeBridge.*;

/** Minecraft GUI primitives; also used for projected world overlays. */
public final class Canvas {
    private final Object graphics,font;
    private final Method fill,text,widthMethod;
    public final int width,height;
    public Canvas(Object graphics) throws ReflectiveOperationException {
        this.graphics=graphics;font=get(client(),"font");
        width=((Number)call(graphics,"guiWidth")).intValue();height=((Number)call(graphics,"guiHeight")).intValue();
        fill=method(graphics.getClass(),"fill",0,0,0,0,0);
        text=method(graphics.getClass(),"drawString|text",font,"",0,0,0,false);
        widthMethod=method(font.getClass(),"width","");
    }
    public void fill(int x,int y,int w,int h,int color) {
        if(w<=0||h<=0)return;
        try {fill.invoke(graphics,x,y,x+w,y+h,color);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    public void text(String value,int x,int y,int color) {
        try {text.invoke(graphics,font,value,x,y,color,false);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    public int textWidth(String value) {
        try{return ((Number)widthMethod.invoke(font,value)).intValue();}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    public String fit(String value,int max) { if(textWidth(value)<=max)return value;while(!value.isEmpty()&&textWidth(value+"...")>max)value=value.substring(0,value.length()-1);return value+"..."; }
    public void border(int x,int y,int w,int h,int color){fill(x,y,w,1,color);fill(x,y+h-1,w,1,color);fill(x,y,1,h,color);fill(x+w-1,y,1,h,color);}
    public void line(double ax,double ay,double bx,double by,int thickness,int color) {
        // Clip before rasterization, so off-screen projections cannot generate huge draw loops.
        double dx=bx-ax,dy=by-ay,t0=0,t1=1;
        double[] p={-dx,dx,-dy,dy},q={ax,width-ax,ay,height-ay};
        for(int i=0;i<4;i++) {
            if(p[i]==0){if(q[i]<0)return;}else {double t=q[i]/p[i];if(p[i]<0)t0=Math.max(t0,t);else t1=Math.min(t1,t);}
        }
        if(t0>t1)return;
        double x=ax+t0*dx,y=ay+t0*dy,ex=ax+t1*dx,ey=ay+t1*dy;
        int steps=(int)Math.ceil(Math.max(Math.abs(ex-x),Math.abs(ey-y)));
        // Pixels on the same row (or column) are merged into one fill, so a line costs one call per minor-axis step.
        boolean horizontal=Math.abs(ex-x)>=Math.abs(ey-y);
        int sx=(int)x,sy=(int)y,lx=sx,ly=sy;
        for(int i=1;i<=steps;i++) {
            double t=(double)i/steps;int px=(int)(x+(ex-x)*t),py=(int)(y+(ey-y)*t);
            if(horizontal?py!=sy:px!=sx){run(sx,sy,lx,ly,thickness,color);sx=px;sy=py;}
            lx=px;ly=py;
        }
        run(sx,sy,lx,ly,thickness,color);
    }
    private void run(int ax,int ay,int bx,int by,int thickness,int color){fill(Math.min(ax,bx),Math.min(ay,by),Math.abs(bx-ax)+thickness,Math.abs(by-ay)+thickness,color);}
}
