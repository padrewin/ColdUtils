package dev.padrewin.coldutils;

/** Accepts distinct PRESS events only; repeats and releases are not taps. */
public final class PanicGesture {
    private int taps;
    private long first;
    public boolean press(long now, long windowMillis) {
        if (taps == 0 || now < first || now - first > windowMillis) { first = now; taps = 0; }
        if (++taps == 4) { reset(); return true; }
        return false;
    }
    public void reset() { taps = 0; }
}
