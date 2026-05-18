package villager;

/** Static data: tradeable enchantments and brewable potions (1.17.1). */
public final class Enchantments {

    // (name, minLvl, maxLvl, weight, isTreasure, isTradeable)
    public static final Object[][] ALL = {
        {"protection",            1, 4, 10, false, true},
        {"fire_protection",       1, 4,  5, false, true},
        {"feather_falling",       1, 4,  5, false, true},
        {"blast_protection",      1, 4,  2, false, true},
        {"projectile_protection", 1, 4,  5, false, true},
        {"respiration",           1, 3,  2, false, true},
        {"aqua_affinity",         1, 1,  2, false, true},
        {"thorns",                1, 3,  1, false, true},
        {"depth_strider",         1, 3,  2, false, true},
        {"frost_walker",          1, 2,  2, true,  true},
        {"binding_curse",         1, 1,  1, true,  true},
        {"soul_speed",            1, 3,  1, true,  false}, // not tradeable
        {"sharpness",             1, 5, 10, false, true},
        {"smite",                 1, 5,  5, false, true},
        {"bane_of_arthropods",    1, 5,  5, false, true},
        {"knockback",             1, 2,  5, false, true},
        {"fire_aspect",           1, 2,  2, false, true},
        {"looting",               1, 3,  2, false, true},
        {"sweeping",              1, 3,  2, false, true},
        {"efficiency",            1, 5, 10, false, true},
        {"silk_touch",            1, 1,  1, false, true},
        {"unbreaking",            1, 3,  5, false, true},
        {"fortune",               1, 3,  2, false, true},
        {"power",                 1, 5, 10, false, true},
        {"punch",                 1, 2,  2, false, true},
        {"flame",                 1, 1,  2, false, true},
        {"infinity",              1, 1,  1, false, true},
        {"luck_of_the_sea",       1, 3,  2, false, true},
        {"lure",                  1, 3,  2, false, true},
        {"loyalty",               1, 3,  5, false, true},
        {"impaling",              1, 5,  2, false, true},
        {"riptide",               1, 3,  2, false, true},
        {"channeling",            1, 1,  1, false, true},
        {"multishot",             1, 1,  2, false, true},
        {"quick_charge",          1, 3,  5, false, true},
        {"piercing",              1, 4, 10, false, true},
        {"mending",               1, 1,  2, true,  true},
        {"vanishing_curse",       1, 1,  1, true,  true},
    };

    // Tradeable subset (soul_speed excluded) — 37 entries, index = nextInt(37)
    public static final String[] TRADEABLE_NAMES;
    public static final int[]    TRADEABLE_MIN;
    public static final int[]    TRADEABLE_MAX;
    public static final boolean[] TRADEABLE_TREASURE;
    public static final int TRADEABLE_COUNT;

    static {
        int cnt = 0;
        for (Object[] e : ALL) if ((boolean) e[5]) cnt++;
        TRADEABLE_NAMES    = new String[cnt];
        TRADEABLE_MIN      = new int[cnt];
        TRADEABLE_MAX      = new int[cnt];
        TRADEABLE_TREASURE = new boolean[cnt];
        int i = 0;
        for (Object[] e : ALL) {
            if ((boolean) e[5]) {
                TRADEABLE_NAMES[i]    = (String)  e[0];
                TRADEABLE_MIN[i]      = (int)      e[1];
                TRADEABLE_MAX[i]      = (int)      e[2];
                TRADEABLE_TREASURE[i] = (boolean)  e[4];
                i++;
            }
        }
        TRADEABLE_COUNT = cnt;
    }

    public static final String[] BREWABLE_POTIONS = {
        "night_vision","long_night_vision","invisibility","long_invisibility",
        "leaping","long_leaping","strong_leaping",
        "fire_resistance","long_fire_resistance",
        "swiftness","long_swiftness","strong_swiftness",
        "slowness","long_slowness","strong_slowness",
        "turtle_master","long_turtle_master","strong_turtle_master",
        "water_breathing","long_water_breathing",
        "healing","strong_healing",
        "harming","strong_harming",
        "poison","long_poison","strong_poison",
        "regeneration","long_regeneration","strong_regeneration",
        "strength","long_strength","strong_strength",
        "weakness","long_weakness",
        "slow_falling","long_slow_falling",
    };
    public static final int BREWABLE_COUNT = BREWABLE_POTIONS.length;

    // Item enchantability values
    public static int enchantability(String item) {
        switch (item) {
            case "iron_sword": case "iron_axe": case "iron_shovel":
            case "iron_pickaxe": return 14;
            case "diamond_sword": case "diamond_axe": case "diamond_shovel":
            case "diamond_pickaxe": case "diamond_helmet": case "diamond_chestplate":
            case "diamond_leggings": case "diamond_boots": return 10;
            case "fishing_rod": case "bow": case "crossbow": return 1;
            default: return 1;
        }
    }

    private Enchantments() {}
}
