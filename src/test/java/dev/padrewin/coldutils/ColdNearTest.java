package dev.padrewin.coldutils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColdNearTest {
    @Test void defaultAndExplicitRadius() {
        assertEquals(256, ColdNear.radius(".coldnear"));
        assertEquals(1, ColdNear.radius(" .COLDNEAR 1 "));
        assertEquals(64, ColdNear.radius(".coldnear   64"));
        assertEquals(256, ColdNear.radius(".coldnear 256"));
    }
    @Test void rejectsInvalidArguments() {
        for (String input : new String[]{"0", "257", "-2", "1.5", "NaN", "999999999999", "32 extra"}) {
            assertThrows(IllegalArgumentException.class, () -> ColdNear.radius(".coldnear " + input));
        }
    }
    @Test void onlyExactCommandTokenIsIntercepted() {
        assertTrue(ColdNear.recognizes(".coldnear invalid"));
        for (String message : new String[]{"", "hello", ".coldnearby", "say .coldnear", "/coldnear"}) {
            assertFalse(ColdNear.recognizes(message));
            assertTrue(ColdNear.allowChat(message));
        }
    }
}