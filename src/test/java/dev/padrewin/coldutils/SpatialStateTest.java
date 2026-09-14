package dev.padrewin.coldutils;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SpatialStateTest {
    private final UUID id=UUID.randomUUID();
    private final SpatialState.Point origin=new SpatialState.Point(0,0,0);
    private SpatialState.Player player(double x){return new SpatialState.Player(id,"Alex",new SpatialState.Point(x,0,0));}
    @Test void disappearanceExpiresReappearanceClearsAndWorldResetLeavesNoMarkers() {
        ModuleConfig c=new ModuleConfig();c.set("logout.enabled",1);c.set("logout.duration",10);
        SpatialState s=new SpatialState();s.update(List.of(player(5)),origin,0,c);s.update(List.of(),origin,1000,c);assertEquals(1,s.marks.size());
        s.update(List.of(player(6)),origin,2000,c);assertTrue(s.marks.isEmpty());s.update(List.of(),origin,3000,c);s.update(List.of(),origin,13001,c);assertTrue(s.marks.isEmpty());
        s.update(List.of(player(5)),origin,14000,c);s.clear();s.update(List.of(),origin,15000,c);assertTrue(s.marks.isEmpty());
    }
    @Test void trailsBreakOnTeleportAndExpireWithoutMovement() {
        ModuleConfig c=new ModuleConfig();c.set("trails.enabled",1);c.set("trails.duration",1);
        SpatialState s=new SpatialState();s.update(List.of(player(1)),origin,0,c);s.update(List.of(player(2)),origin,100,c);assertEquals(2,s.trails.get(id).size());
        s.update(List.of(player(50)),origin,200,c);assertEquals(1,s.trails.get(id).size());
        s.update(List.of(),origin,1201,c);assertTrue(s.trails.isEmpty());
    }
}
