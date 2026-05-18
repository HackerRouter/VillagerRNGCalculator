package villager;

import java.util.*;

/**
 * Mirrors Python simulator.py — simulates updateTrades() for a villager.
 * Listing format: each entry is a String[] describing the trade type.
 *   ["fixed", label]
 *   ["enchant_book"]
 *   ["enchanted_item", item, baseCost]
 *   ["dyed_armor", item]
 *   ["tipped_arrow"]
 */
public final class Simulator {

    // TRADES[profession][level] = String[][]
    private static final Map<String, Map<Integer, String[][]>> TRADES = buildTrades();

    public static String[] getProfessions() {
        String[] p = TRADES.keySet().toArray(new String[0]);
        Arrays.sort(p);
        return p;
    }

    public static int[] getLevels(String profession) {
        Map<Integer, String[][]> m = TRADES.get(profession);
        if (m == null) return new int[0];
        return m.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    /** Simulate updateTrades(). seed is the seed AFTER nextInt(1000) ambient call. */
    public static TradeResult[] simulateUpdateTrades(Rand r, String profession, int level) {
        String[][] listings = TRADES.getOrDefault(profession, Collections.emptyMap())
                                    .getOrDefault(level, new String[0][]);
        int n = listings.length;
        if (n == 0) return new TradeResult[0];

        int[] indices;
        if (n <= 2) {
            indices = new int[n];
            for (int i = 0; i < n; i++) indices[i] = i;
        } else {
            // addOffersFromItemListings: draw without replacement, pick 2
            Set<Integer> set = new LinkedHashSet<>();
            while (set.size() < 2) set.add(r.nextInt(n));
            // HashSet iteration order: bucket = value & 15
            Integer[] arr = set.toArray(new Integer[0]);
            Arrays.sort(arr, Comparator.comparingInt(x -> x % 16));
            indices = new int[arr.length];
            for (int i = 0; i < arr.length; i++) indices[i] = arr[i];
        }

        TradeResult[] results = new TradeResult[indices.length];
        for (int i = 0; i < indices.length; i++) {
            results[i] = dispatch(r, listings[indices[i]]);
        }
        return results;
    }

    /** Full tick: nextInt(1000) + updateTrades + nextInt(100). */
    public static TradeResult[] simulateFullTick(Rand r, String profession, int level) {
        r.nextInt(1000);
        TradeResult[] trades = simulateUpdateTrades(r, profession, level);
        r.nextInt(100);
        return trades;
    }

    private static TradeResult dispatch(Rand r, String[] listing) {
        switch (listing[0]) {
            case "enchant_book":    return TradeSim.simEnchantBook(r);
            case "enchanted_item":  return TradeSim.simEnchantedItem(r, listing[1], Integer.parseInt(listing[2]));
            case "dyed_armor":      return TradeSim.simDyedArmor(r, listing[1]);
            case "tipped_arrow":    return TradeSim.simTippedArrow(r);
            default:                return TradeResult.fixed(listing[1]);
        }
    }

    // ── Trade table ──────────────────────────────────────────────────────────
    private static Map<String, Map<Integer, String[][]>> buildTrades() {
        Map<String, Map<Integer, String[][]>> t = new LinkedHashMap<>();
        t.put("farmer", map(
            1, arr(f("20 wheat->em"),f("26 potato->em"),f("22 carrot->em"),f("15 beetroot->em"),f("1em->6 bread")),
            2, arr(f("6 pumpkin->em"),f("1em->4 pumpkin_pie"),f("1em->4 apple")),
            3, arr(f("3em->18 cookie"),f("4 melon->em")),
            4, arr(f("1em->cake"),f("1em->stew(night_vision)"),f("1em->stew(jump)"),f("1em->stew(weakness)"),f("1em->stew(blindness)"),f("1em->stew(poison)"),f("1em->stew(saturation)")),
            5, arr(f("3em->golden_carrot"),f("4em->glistering_melon"))
        ));
        t.put("fisherman", map(
            1, arr(f("20 string->em"),f("10 coal->em"),f("6cod+1em->6cooked_cod"),f("3em->cod_bucket")),
            2, arr(f("15 cod->em"),f("6salmon+1em->6cooked_salmon"),f("2em->campfire")),
            3, arr(f("13 salmon->em"),ei("fishing_rod","3")),
            4, arr(f("6 tropical_fish->em")),
            5, arr(f("4 pufferfish->em"),f("1 boat->em"))
        ));
        t.put("shepherd", map(
            1, arr(f("18 white_wool->em"),f("18 brown_wool->em"),f("18 black_wool->em"),f("18 gray_wool->em"),f("2em->shears")),
            2, arr(f("12 white_dye->em"),f("12 gray_dye->em"),f("12 black_dye->em"),f("12 light_blue_dye->em"),f("12 lime_dye->em"),
                   f("1em->white_wool"),f("1em->orange_wool"),f("1em->magenta_wool"),f("1em->light_blue_wool"),f("1em->yellow_wool"),
                   f("1em->lime_wool"),f("1em->pink_wool"),f("1em->gray_wool"),f("1em->light_gray_wool"),f("1em->cyan_wool"),
                   f("1em->purple_wool"),f("1em->blue_wool"),f("1em->brown_wool"),f("1em->green_wool"),f("1em->red_wool"),
                   f("1em->black_wool"),f("1em->4white_carpet"),f("1em->4orange_carpet"),f("1em->4magenta_carpet"),
                   f("1em->4light_blue_carpet"),f("1em->4yellow_carpet"),f("1em->4lime_carpet"),f("1em->4pink_carpet"),
                   f("1em->4gray_carpet"),f("1em->4light_gray_carpet"),f("1em->4cyan_carpet"),f("1em->4purple_carpet"),
                   f("1em->4blue_carpet"),f("1em->4brown_carpet"),f("1em->4green_carpet"),f("1em->4red_carpet"),f("1em->4black_carpet")),
            3, arr(f("12 yellow_dye->em"),f("12 light_gray_dye->em"),f("12 orange_dye->em"),f("12 red_dye->em"),f("12 pink_dye->em"),
                   f("3em->white_bed"),f("3em->yellow_bed"),f("3em->red_bed"),f("3em->black_bed"),f("3em->blue_bed"),
                   f("3em->brown_bed"),f("3em->cyan_bed"),f("3em->gray_bed"),f("3em->green_bed"),f("3em->light_blue_bed"),
                   f("3em->light_gray_bed"),f("3em->lime_bed"),f("3em->magenta_bed"),f("3em->orange_bed"),f("3em->pink_bed"),f("3em->purple_bed")),
            4, arr(f("12 brown_dye->em"),f("12 purple_dye->em"),f("12 blue_dye->em"),f("12 green_dye->em"),f("12 magenta_dye->em"),
                   f("12 cyan_dye->em"),f("3em->white_banner"),f("3em->blue_banner"),f("3em->light_blue_banner"),
                   f("3em->red_banner"),f("3em->pink_banner"),f("3em->green_banner"),f("3em->lime_banner"),
                   f("3em->gray_banner"),f("3em->black_banner"),f("3em->purple_banner"),f("3em->magenta_banner"),
                   f("3em->cyan_banner"),f("3em->brown_banner"),f("3em->yellow_banner"),f("3em->orange_banner"),f("3em->light_gray_banner")),
            5, arr(f("2em->painting"))
        ));
        t.put("fletcher", map(
            1, arr(f("32 stick->em"),f("1em->16 arrow"),f("10gravel+1em->10flint")),
            2, arr(f("26 flint->em"),f("2em->bow")),
            3, arr(f("14 string->em"),f("3em->crossbow")),
            4, arr(f("24 feather->em"),ei("bow","2")),
            5, arr(f("8 tripwire_hook->em"),ei("crossbow","3"),ta())
        ));
        t.put("librarian", map(
            1, arr(f("24 paper->em"),eb(),f("9em->bookshelf")),
            2, arr(f("4 book->em"),eb(),f("1em->lantern")),
            3, arr(f("5 ink_sac->em"),eb(),f("1em->4glass")),
            4, arr(f("2 writable_book->em"),eb(),f("5em->clock"),f("4em->compass")),
            5, arr(f("20em->name_tag"))
        ));
        t.put("cartographer", map(
            1, arr(f("24 paper->em"),f("7em->map")),
            2, arr(f("11 glass_pane->em"),f("13em->ocean_map")),
            3, arr(f("1 compass->em"),f("14em->mansion_map")),
            4, arr(f("7em->item_frame"),f("3em->white_banner"),f("3em->blue_banner"),f("3em->light_blue_banner"),
                   f("3em->red_banner"),f("3em->pink_banner"),f("3em->green_banner"),f("3em->lime_banner"),
                   f("3em->gray_banner"),f("3em->black_banner"),f("3em->purple_banner"),f("3em->magenta_banner"),
                   f("3em->cyan_banner"),f("3em->brown_banner"),f("3em->yellow_banner"),f("3em->orange_banner"),f("3em->light_gray_banner")),
            5, arr(f("8em->globe_banner"))
        ));
        t.put("cleric", map(
            1, arr(f("32 rotten_flesh->em"),f("1em->2redstone")),
            2, arr(f("3 gold_ingot->em"),f("1em->lapis")),
            3, arr(f("2 rabbit_foot->em"),f("4em->glowstone")),
            4, arr(f("4 scute->em"),f("9 glass_bottle->em"),f("5em->ender_pearl")),
            5, arr(f("22 nether_wart->em"),f("3em->exp_bottle"))
        ));
        t.put("armorer", map(
            1, arr(f("15 coal->em"),f("7em->iron_leggings"),f("4em->iron_boots"),f("5em->iron_helmet"),f("9em->iron_chestplate")),
            2, arr(f("4 iron_ingot->em"),f("36em->bell"),f("1em->chainmail_boots"),f("3em->chainmail_leggings")),
            3, arr(f("1 lava_bucket->em"),f("1 diamond->em"),f("1em->chainmail_helmet"),f("4em->chainmail_chestplate"),f("5em->shield")),
            4, arr(ei("diamond_leggings","14"),ei("diamond_boots","8")),
            5, arr(ei("diamond_helmet","8"),ei("diamond_chestplate","16"))
        ));
        t.put("weaponsmith", map(
            1, arr(f("15 coal->em"),f("3em->iron_axe"),ei("iron_sword","2")),
            2, arr(f("4 iron_ingot->em"),f("36em->bell")),
            3, arr(f("24 flint->em")),
            4, arr(f("1 diamond->em"),ei("diamond_axe","12")),
            5, arr(ei("diamond_sword","8"))
        ));
        t.put("toolsmith", map(
            1, arr(f("15 coal->em"),f("1em->stone_axe"),f("1em->stone_shovel"),f("1em->stone_pickaxe"),f("1em->stone_hoe")),
            2, arr(f("4 iron_ingot->em"),f("36em->bell")),
            3, arr(f("30 flint->em"),ei("iron_axe","1"),ei("iron_shovel","2"),ei("iron_pickaxe","3"),f("4em->diamond_hoe")),
            4, arr(f("1 diamond->em"),ei("diamond_axe","12"),ei("diamond_shovel","5")),
            5, arr(ei("diamond_pickaxe","13"))
        ));
        t.put("butcher", map(
            1, arr(f("14 chicken->em"),f("7 porkchop->em"),f("4 rabbit->em"),f("1em->rabbit_stew")),
            2, arr(f("15 coal->em"),f("1em->5cooked_porkchop"),f("1em->8cooked_chicken")),
            3, arr(f("7 mutton->em"),f("10 beef->em")),
            4, arr(f("10 dried_kelp_block->em")),
            5, arr(f("10 sweet_berries->em"))
        ));
        t.put("leatherworker", map(
            1, arr(f("6 leather->em"),da("leather_leggings"),da("leather_chestplate")),
            2, arr(f("26 flint->em"),da("leather_helmet"),da("leather_boots")),
            3, arr(f("9 rabbit_hide->em"),da("leather_chestplate")),
            4, arr(f("4 scute->em"),da("leather_horse_armor")),
            5, arr(f("6em->saddle"),da("leather_helmet"))
        ));
        t.put("mason", map(
            1, arr(f("10 clay_ball->em"),f("1em->10brick")),
            2, arr(f("20 stone->em"),f("1em->4chiseled_stone_bricks")),
            3, arr(f("16 granite->em"),f("16 andesite->em"),f("16 diorite->em"),
                   f("1em->4dripstone"),f("1em->4polished_andesite"),f("1em->4polished_diorite"),f("1em->4polished_granite")),
            4, arr(f("12 quartz->em"),f("1em->orange_terracotta"),f("1em->white_terracotta"),f("1em->blue_terracotta"),
                   f("1em->light_blue_terracotta"),f("1em->gray_terracotta"),f("1em->light_gray_terracotta"),
                   f("1em->black_terracotta"),f("1em->red_terracotta"),f("1em->pink_terracotta"),f("1em->magenta_terracotta"),
                   f("1em->lime_terracotta"),f("1em->green_terracotta"),f("1em->cyan_terracotta"),f("1em->purple_terracotta"),
                   f("1em->yellow_terracotta"),f("1em->brown_terracotta"),f("1em->orange_glazed"),f("1em->white_glazed"),
                   f("1em->blue_glazed"),f("1em->light_blue_glazed"),f("1em->gray_glazed"),f("1em->light_gray_glazed"),
                   f("1em->black_glazed"),f("1em->red_glazed"),f("1em->pink_glazed"),f("1em->magenta_glazed"),
                   f("1em->lime_glazed"),f("1em->green_glazed"),f("1em->cyan_glazed"),f("1em->purple_glazed"),
                   f("1em->yellow_glazed"),f("1em->brown_glazed")),
            5, arr(f("1em->quartz_pillar"),f("1em->quartz_block"))
        ));
        return t;
    }

    // ── builder helpers ──────────────────────────────────────────────────────
    private static String[] f(String label)              { return new String[]{"fixed", label}; }
    private static String[] eb()                         { return new String[]{"enchant_book"}; }
    private static String[] ei(String item, String base) { return new String[]{"enchanted_item", item, base}; }
    private static String[] da(String item)              { return new String[]{"dyed_armor", item}; }
    private static String[] ta()                         { return new String[]{"tipped_arrow"}; }
    private static String[][] arr(String[]... entries)   { return entries; }

    @SafeVarargs
    private static <K, V> Map<K, V> map(Object... kv) {
        Map<K, V> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            @SuppressWarnings("unchecked") K k = (K) kv[i];
            @SuppressWarnings("unchecked") V v = (V) kv[i + 1];
            m.put(k, v);
        }
        return m;
    }

    private Simulator() {}
}
