package villager;

import java.util.ArrayList;
import java.util.List;

/** Simulates all trade types (mirrors Python trades.py). */
public final class TradeSim {

    // ── EnchantBookForEmeralds ──────────────────────────────────────────────
    public static TradeResult simEnchantBook(Rand r) {
        int idx = r.nextInt(Enchantments.TRADEABLE_COUNT);
        String name = Enchantments.TRADEABLE_NAMES[idx];
        int min = Enchantments.TRADEABLE_MIN[idx];
        int max = Enchantments.TRADEABLE_MAX[idx];
        int level = (min == max) ? min : min + r.nextInt(max - min + 1);
        int priceRand = r.nextInt(5 + level * 10);
        int price = 2 + priceRand + 3 * level;
        if (Enchantments.TRADEABLE_TREASURE[idx]) price *= 2;
        price = Math.min(price, 64);
        List<String[]> enchs = new ArrayList<>();
        enchs.add(new String[]{name, String.valueOf(level)});
        return TradeResult.enchantBook(enchs, price);
    }

    // ── EnchantedItemForEmeralds ────────────────────────────────────────────
    public static TradeResult simEnchantedItem(Rand r, String item, int baseCost) {
        int encVal = Enchantments.enchantability(item);
        int r1 = r.nextInt(encVal / 4 + 1);
        int r2 = r.nextInt(encVal / 4 + 1);
        float f1 = r.nextFloat();
        float f2 = r.nextFloat();
        int cost = 5 + r1 + r2;
        int bonus = Math.round((f1 + f2 - 1.0f) * 0.15f * cost);
        cost = Math.max(1, cost + bonus);

        List<String[]> chosen = new ArrayList<>();
        List<int[]> available = getAvailableEnchants(cost, item);
        if (!available.isEmpty()) {
            int pickIdx = weightedPick(r, available);
            int[] e = available.get(pickIdx);
            chosen.add(new String[]{Enchantments.ALL[e[0]][0].toString(), String.valueOf(e[1])});
            int i = cost;
            while (true) {
                int roll = r.nextInt(50);
                if (roll > i) break;
                List<int[]> avail2 = filterCompatible(available, chosen);
                if (avail2.isEmpty()) break;
                int idx2 = weightedPick(r, avail2);
                int[] e2 = avail2.get(idx2);
                chosen.add(new String[]{Enchantments.ALL[e2[0]][0].toString(), String.valueOf(e2[1])});
                i /= 2;
            }
        }
        int price = Math.min(baseCost + cost, 64);
        return TradeResult.enchantedItem(item, chosen, price);
    }

    // ── DyedArmorForEmeralds ────────────────────────────────────────────────
    public static TradeResult simDyedArmor(Rand r, String item) {
        r.nextInt(16); // color 1
        if (r.nextFloat() > 0.7f) {
            r.nextInt(16); // color 2
            if (r.nextFloat() > 0.8f) r.nextInt(16); // color 3
        }
        return TradeResult.dyedArmor(item);
    }

    // ── TippedArrowForItemsAndEmeralds ──────────────────────────────────────
    public static TradeResult simTippedArrow(Rand r) {
        int idx = r.nextInt(Enchantments.BREWABLE_COUNT);
        return TradeResult.tippedArrow(Enchantments.BREWABLE_POTIONS[idx]);
    }

    // ── helpers ─────────────────────────────────────────────────────────────
    private static List<int[]> getAvailableEnchants(int cost, String item) {
        // Returns list of [allIdx, level, weight] applicable at given cost
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < Enchantments.ALL.length; i++) {
            Object[] e = Enchantments.ALL[i];
            if (!(boolean) e[5]) continue; // not tradeable/discoverable
            if ((boolean) e[4]) continue;  // skip treasure for item enchanting
            if (!itemCanHave(item, (String) e[0])) continue;
            int minLvl = (int) e[1], maxLvl = (int) e[2];
            for (int lvl = maxLvl; lvl >= minLvl; lvl--) {
                int[] range = enchantCostRange((String) e[0], lvl);
                if (range[0] <= cost && cost <= range[1]) {
                    result.add(new int[]{i, lvl, (int) e[3]});
                    break;
                }
            }
        }
        return result;
    }

    private static int weightedPick(Rand r, List<int[]> items) {
        int total = 0;
        for (int[] e : items) total += e[2];
        int roll = r.nextInt(total);
        int acc = 0;
        for (int i = 0; i < items.size(); i++) {
            acc += items.get(i)[2];
            if (roll < acc) return i;
        }
        return items.size() - 1;
    }

    private static List<int[]> filterCompatible(List<int[]> available, List<String[]> chosen) {
        List<int[]> result = new ArrayList<>();
        outer:
        for (int[] e : available) {
            String name = (String) Enchantments.ALL[e[0]][0];
            for (String[] c : chosen) {
                if (incompatible(name, c[0])) continue outer;
            }
            result.add(e);
        }
        return result;
    }

    private static boolean incompatible(String a, String b) {
        if (a.equals(b)) return true;
        String[][] pairs = {
            {"sharpness","smite"},{"sharpness","bane_of_arthropods"},{"smite","bane_of_arthropods"},
            {"protection","fire_protection"},{"protection","blast_protection"},{"protection","projectile_protection"},
            {"fire_protection","blast_protection"},{"fire_protection","projectile_protection"},
            {"blast_protection","projectile_protection"},
            {"depth_strider","frost_walker"},{"infinity","mending"},
            {"loyalty","riptide"},{"riptide","channeling"},{"multishot","piercing"},{"silk_touch","fortune"},
        };
        for (String[] p : pairs) {
            if ((a.equals(p[0]) && b.equals(p[1])) || (a.equals(p[1]) && b.equals(p[0]))) return true;
        }
        return false;
    }

    private static boolean itemCanHave(String item, String ench) {
        // Simplified: check category compatibility
        switch (ench) {
            case "sharpness": case "smite": case "bane_of_arthropods":
            case "knockback": case "fire_aspect": case "looting": case "sweeping":
                return item.contains("sword") || item.contains("axe");
            case "efficiency": case "silk_touch": case "fortune":
                return item.contains("pickaxe") || item.contains("axe") || item.contains("shovel");
            case "power": case "punch": case "flame": case "infinity":
                return item.equals("bow");
            case "multishot": case "quick_charge": case "piercing":
                return item.equals("crossbow");
            case "luck_of_the_sea": case "lure":
                return item.equals("fishing_rod");
            case "loyalty": case "impaling": case "riptide": case "channeling":
                return item.equals("trident");
            case "protection": case "fire_protection": case "blast_protection":
            case "projectile_protection": case "thorns":
                return item.contains("helmet") || item.contains("chestplate") ||
                       item.contains("leggings") || item.contains("boots");
            case "respiration": case "aqua_affinity":
                return item.contains("helmet");
            case "feather_falling": case "depth_strider": case "frost_walker":
                return item.contains("boots");
            case "unbreaking": case "mending": case "vanishing_curse":
                return true;
            case "binding_curse":
                return item.contains("helmet") || item.contains("chestplate") ||
                       item.contains("leggings") || item.contains("boots");
            default: return false;
        }
    }

    private static int[] enchantCostRange(String name, int lvl) {
        switch (name) {
            case "protection":            return new int[]{1+lvl*11, 1+lvl*11+11};
            case "fire_protection":       return new int[]{10+lvl*8, 10+lvl*8+8};
            case "feather_falling":       return new int[]{5+lvl*6, 5+lvl*6+6};
            case "blast_protection":      return new int[]{5+lvl*8, 5+lvl*8+8};
            case "projectile_protection": return new int[]{1+lvl*6, 1+lvl*6+6};
            case "respiration":           return new int[]{lvl*10, lvl*10+30};
            case "aqua_affinity":         return new int[]{1, 41};
            case "thorns":                return new int[]{10+lvl*20, 10+lvl*20+50};
            case "depth_strider":         return new int[]{lvl*10, lvl*10+15};
            case "frost_walker":          return new int[]{lvl*10, lvl*10+15};
            case "binding_curse":         return new int[]{25, 50};
            case "sharpness":             return new int[]{1+lvl*11, 1+lvl*11+20};
            case "smite":                 return new int[]{5+lvl*8, 5+lvl*8+20};
            case "bane_of_arthropods":    return new int[]{5+lvl*8, 5+lvl*8+20};
            case "knockback":             return new int[]{5+lvl*20, 5+lvl*20+50};
            case "fire_aspect":           return new int[]{10+lvl*20, 10+lvl*20+50};
            case "looting":               return new int[]{15+lvl*9, 15+lvl*9+50};
            case "sweeping":              return new int[]{5+lvl*9, 5+lvl*9+15};
            case "efficiency":            return new int[]{1+lvl*10, 1+lvl*10+50};
            case "silk_touch":            return new int[]{15, 65};
            case "unbreaking":            return new int[]{5+lvl*8, 5+lvl*8+50};
            case "fortune":               return new int[]{15+lvl*9, 15+lvl*9+50};
            case "power":                 return new int[]{1+lvl*10, 1+lvl*10+15};
            case "punch":                 return new int[]{12+lvl*20, 12+lvl*20+25};
            case "flame":                 return new int[]{20, 50};
            case "infinity":              return new int[]{20, 50};
            case "luck_of_the_sea":       return new int[]{15+lvl*9, 15+lvl*9+50};
            case "lure":                  return new int[]{15+lvl*9, 15+lvl*9+50};
            case "loyalty":               return new int[]{lvl*7, 50};
            case "impaling":              return new int[]{1+lvl*8, 1+lvl*8+20};
            case "riptide":               return new int[]{17+lvl*7, 50};
            case "channeling":            return new int[]{25, 50};
            case "multishot":             return new int[]{20, 50};
            case "quick_charge":          return new int[]{12+lvl*20, 12+lvl*20+50};
            case "piercing":              return new int[]{1+lvl*10, 1+lvl*10+15};
            case "mending":               return new int[]{25, 75};
            case "vanishing_curse":       return new int[]{25, 50};
            default:                      return new int[]{0, 100};
        }
    }

    private TradeSim() {}
}
