package villager;

/** Offset operations that advance the villager's RNG by specific game actions. */
public final class Offsets {

    public static long baseTick(long seed, int n) {
        // nextInt(1000) + nextInt(100) per tick
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) { r.nextInt(1000); r.nextInt(100); }
        return r.getActualSeed();
    }

    public static long hurt(long seed, int n) {
        // markHurt(nextDouble x2) + getVoicePitch x2(nextFloat x1 each) = 4 steps
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) { r.nextDouble(); r.nextFloat(); r.nextFloat(); }
        return r.getActualSeed();
    }

    /** LEVEL_UP_SLOT_SELECT: addOffersFromItemListings picks 2 slots via nextInt(listSize).
     *  listSize varies by profession+level; pass the actual listing count. */
    public static long levelUpSlotSelect(long seed, int listSize, int n) {
        // 2 nextInt(listSize) calls per updateTrades invocation
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) { r.nextInt(listSize); r.nextInt(listSize); }
        return r.getActualSeed();
    }

    /** LEVEL_UP_OFFER_PARAM: simulate full updateTrades (slot select + offer generation).
     *  Returns seed after n complete updateTrades cycles. */
    public static long levelUpOfferParam(long seed, String profession, int level, int n) {
        for (int i = 0; i < n; i++) {
            Rand r = Rand.ofActual(seed);
            Simulator.simulateUpdateTrades(r, profession, level);
            seed = r.getActualSeed();
        }
        return seed;
    }

    public static long rewardTradeXp(long seed, int n) {
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) r.nextInt(4);
        return r.getActualSeed();
    }

    public static long frostWalk(long seed, int n) {
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) r.nextInt(61);
        return r.getActualSeed();
    }

    public static long ambientSound(long seed, int n) {
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) r.nextInt(1000);
        return r.getActualSeed();
    }

    public static long raidCheck(long seed, int n) {
        Rand r = Rand.ofActual(seed);
        for (int i = 0; i < n; i++) r.nextInt(100);
        return r.getActualSeed();
    }

    public static long rawSteps(long seed, int n) {
        Rand r = Rand.ofActual(seed);
        r.advance(n);
        return r.getActualSeed();
    }

    private Offsets() {}
}
