package dev.padrewin.coldutils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ModuleConfigTest {
    @TempDir Path directory;
    @Test void firstInstallHasNoActiveModules() {
        ModuleConfig c=new ModuleConfig();
        for(var o:ModuleConfig.OPTIONS)if(o.key().endsWith(".enabled"))assertEquals(0,c.get(o.key()),o.key());
        assertFalse(c.legacy().enabled());assertFalse(c.legacy().espNames());assertEquals(39,c.get("gui.key"));
    }
    @Test void panicSurvivesRestartAndRestoresExactSelection() throws Exception {
        ModuleConfig c=new ModuleConfig();c.set("freecam.enabled",1);c.set("trails.enabled",1);c.set("esp.names",1);c.set("trails.color",0x123456);
        c.togglePanic();assertFalse(c.active("freecam"));assertFalse(c.legacy().espNames());
        Path path=directory.resolve("settings.properties");c.save(path);ModuleConfig loaded=ModuleConfig.load(path);
        assertTrue(loaded.panic());assertEquals(1,loaded.get("freecam.enabled"));
        loaded.togglePanic();assertTrue(loaded.active("freecam"));assertTrue(loaded.active("trails"));assertTrue(loaded.legacy().espNames());assertFalse(loaded.active("storage"));assertEquals(0x123456,loaded.get("trails.color"));
    }
    @Test void allOptionsRoundTripAndInvalidValuesDoNotOverwrite() throws Exception {
        ModuleConfig c=new ModuleConfig();for(var o:ModuleConfig.OPTIONS)c.set(o.key(),o.max());
        Path path=directory.resolve("settings.properties");c.save(path);ModuleConfig copy=ModuleConfig.load(path);
        for(var o:ModuleConfig.OPTIONS)assertEquals(c.get(o.key()),copy.get(o.key()),o.key());
        String original=Files.readString(path);assertThrows(IllegalArgumentException.class,()->c.set("trails.duration",0));assertEquals(original,Files.readString(path));
        assertThrows(IllegalArgumentException.class,()->c.set("gui.key",-1));
    }
    @Test void fourDistinctTapsHaveADeadlineAndReset() {
        PanicGesture g=new PanicGesture();assertFalse(g.press(0,1200));assertFalse(g.press(300,1200));assertFalse(g.press(600,1200));assertTrue(g.press(900,1200));
        assertFalse(g.press(1000,1200));assertFalse(g.press(2400,1200));assertFalse(g.press(2500,1200));assertFalse(g.press(2600,1200));assertTrue(g.press(2700,1200));
        g.press(2800,1200);g.reset();assertFalse(g.press(2900,1200));
    }
}
