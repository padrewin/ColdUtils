package dev.padrewin.coldutils;

import java.util.*;

/** Bounded, world-scoped observations; a disappeared entity is not proof of logout. */
public final class SpatialState {
    public record Point(double x,double y,double z) {
        public double distanceSquared(Point p) { double a=x-p.x,b=y-p.y,c=z-p.z; return a*a+b*b+c*c; }
    }
    public record Player(UUID id,String name,Point point) {}
    public record Mark(Player player,long time) {}
    public record Trail(Point point,long time) {}
    private Map<UUID,Player> previous = new HashMap<>();
    public final Map<UUID,Mark> marks = new LinkedHashMap<>();
    public final Map<UUID,Deque<Trail>> trails = new LinkedHashMap<>();
    public void clear() { previous.clear(); marks.clear(); trails.clear(); }
    public void update(Collection<Player> players,Point local,long now,ModuleConfig c) {
        Map<UUID,Player> current = new LinkedHashMap<>();
        players.stream().limit(256).forEach(p -> current.put(p.id,p));
        if(c.active("logout")) {
            for(Player p:previous.values()) if(!current.containsKey(p.id))marks.put(p.id,new Mark(p,now));
            current.keySet().forEach(marks::remove);
            marks.values().removeIf(m -> now-m.time > c.get("logout.duration")*1000L);
            while(marks.size()>256)marks.remove(marks.keySet().iterator().next());
        } else marks.clear();
        if(c.active("trails")) {
            for(Player p:current.values()) if(p.point.distanceSquared(local)<=Math.pow(c.get("trails.range"),2)) {
                Deque<Trail> path=trails.computeIfAbsent(p.id,k->new ArrayDeque<>());
                if(!path.isEmpty() && path.getLast().point.distanceSquared(p.point)>256)path.clear();
                if(path.isEmpty() || path.getLast().point.distanceSquared(p.point)>0.01)path.addLast(new Trail(p.point,now));
                while(path.size()>600)path.removeFirst();
            }
            trails.values().forEach(path -> { while(!path.isEmpty() && now-path.getFirst().time>c.get("trails.duration")*1000L)path.removeFirst(); });
            trails.values().removeIf(Deque::isEmpty);
            while(trails.size()>256)trails.remove(trails.keySet().iterator().next());
        } else trails.clear();
        previous=current;
    }
}
