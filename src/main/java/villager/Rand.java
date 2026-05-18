package villager;

/** Minimal Java LCG wrapper (mirrors java.util.Random). */
public final class Rand {
    private static final long MULT = 0x5DEECE66DL;
    private static final long ADD  = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    private long seed;
    private long stepCount;

    public Rand(long internalSeed) { this.seed = internalSeed & MASK; }

    public static Rand ofActual(long actual) { return new Rand(actual ^ MULT); }

    public long getInternalSeed() { return seed; }
    public long getActualSeed()   { return seed ^ MULT; }
    public long getStepCount()    { return stepCount; }
    public void resetStepCount()  { stepCount = 0; }

    private int next(int bits) {
        seed = (seed * MULT + ADD) & MASK;
        stepCount++;
        return (int) (seed >>> (48 - bits));
    }

    public int nextInt(int bound) {
        if ((bound & -bound) == bound) return (int) ((bound * (long) next(31)) >> 31);
        int bits, val;
        do { bits = next(31); val = bits % bound; } while (bits - val + (bound - 1) < 0);
        return val;
    }

    public float nextFloat() { return next(24) / (float) (1 << 24); }

    public double nextDouble() {
        return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
    }

    public void advance(long n) {
        long m = MULT, a = ADD, rm = 1, ra = 0;
        long k = n;
        while (k != 0) {
            if ((k & 1) != 0) { rm = rm * m & MASK; ra = (ra * m + a) & MASK; }
            a = a * (m + 1) & MASK; m = m * m & MASK; k >>>= 1;
        }
        seed = (rm * seed + ra) & MASK;
        stepCount += n;
    }

    public Rand copy() { return new Rand(seed); }
}
