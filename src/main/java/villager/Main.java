package villager;

import java.util.*;
import java.util.function.Predicate;

/**
 * Villager RNG Calculator (1.17.1) — Java port
 * Modes:
 *   1 - Crack + Forward + Predict
 *   2 - Crack seed from frost-ice observations (LattiCG, milliseconds)
 *   3 - Forward simulate (apply offsets)
 *   4 - Predict: find target trade from current seed
 */
public class Main {
    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println("  Villager RNG Calculator  (1.17.1)");
        System.out.println("=======================================================");
        while (true) {
            System.out.println("\nModes:");
            System.out.println("  1. Crack + Forward + Predict");
            System.out.println("  2. Crack seed (frost-ice observations)");
            System.out.println("  3. Forward simulate (apply offsets)");
            System.out.println("  4. Predict target trade");
            System.out.println("  0. Exit");
            String choice = prompt("Select mode: ").trim();
            switch (choice) {
                case "0": return;
                case "1": modeAll(); break;
                case "2": modeCrack(); break;
                case "3": modeForward(null); break;
                case "4": modePredict(null); break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ── Mode 2: Crack ────────────────────────────────────────────────────────
    static List<Long> modeCrack() {
        System.out.println("\n--- Mode 2: Crack seed from frost-ice observations ---");
        System.out.println("Enter elapsed ticks for each ice block (range 60-120).");
        System.out.println("Minimum 2 observations; 8-10 recommended.");
        List<Integer> obs = new ArrayList<>();
        while (true) {
            String s = prompt("  Observation " + (obs.size() + 1) + " elapsed ticks (Enter to stop): ").trim();
            if (s.isEmpty()) {
                if (obs.size() < 2) { System.out.println("Need at least 2."); continue; }
                break;
            }
            try {
                int v = Integer.parseInt(s);
                if (v < 60 || v > 120) { System.out.println("Must be 60-120."); continue; }
                obs.add(v - 60);
            } catch (NumberFormatException e) { System.out.println("Invalid."); }
        }
        System.out.println("\nCracking with " + obs.size() + " observations (LattiCG lattice attack)...");
        long t0 = System.currentTimeMillis();
        List<Long> seeds = SeedCracker.crack(obs);
        long ms = System.currentTimeMillis() - t0;
        if (seeds.isEmpty()) {
            System.out.println("No matching seed found.");
        } else {
            System.out.printf("Found %d seed(s) in %d ms:%n", seeds.size(), ms);
            for (long s : seeds) System.out.printf("  0x%012X  (%d)%n", s, s);
            if (seeds.size() > 1)
                System.out.println("  (Multiple candidates — add more observations to narrow down)");
        }
        return seeds;
    }

    // ── Mode 3: Forward ──────────────────────────────────────────────────────
    static long modeForward(Long seed) {
        System.out.println("\n--- Mode 3: Forward simulate (apply offsets) ---");
        if (seed == null) seed = inputSeed("Enter starting seed (hex or decimal): ");
        System.out.printf("Current seed: 0x%012X%n", seed);

        String[][] fns = {
            {"base_tick",              "nextInt(1000)+nextInt(100)",              "2 steps/call"},
            {"hurt",                   "nextDouble[2]+nextFloat[1]+nextFloat[1]",  "4 steps/call"},
            {"reward_trade_xp",        "nextInt(4)",                              "1 step/call"},
            {"frost_walk",             "nextInt(61)",                             "1 step/call"},
            {"ambient_sound",          "nextInt(1000)",                           "1 step/call"},
            {"raid_check",             "nextInt(100)",                            "1 step/call"},
            {"level_up_slot_select",   "nextInt(listSize) x2",                    "2 steps/call"},
            {"level_up_offer_param",   "full updateTrades simulation",            "variable"},
            {"raw_lcg_steps",          "direct LCG advance",                      "1 step/call"},
        };
        while (true) {
            System.out.println("\nOffset functions:");
            System.out.println("  #  Name                       Calls                              LCG steps");
            System.out.println("  ───────────────────────────────────────────────────────────────────────────");
            for (int i = 0; i < fns.length; i++)
                System.out.printf("  %d. %-26s %-34s %s%n", i, fns[i][0], fns[i][1], fns[i][2]);
            System.out.println("  q. Done");
            String c = prompt("Select function: ").trim();
            if (c.equalsIgnoreCase("q")) break;
            int idx;
            try { idx = Integer.parseInt(c); if (idx < 0 || idx >= fns.length) throw new NumberFormatException(); }
            catch (NumberFormatException e) { System.out.println("Invalid."); continue; }
            int count;
            try { count = Integer.parseInt(prompt("How many times: ").trim()); }
            catch (NumberFormatException e) { System.out.println("Invalid."); continue; }
            seed = applyOffset(idx, seed, count);
            System.out.printf("Applied %s x%d -> seed: 0x%012X%n", fns[idx][0], count, seed);
        }
        System.out.printf("Final seed: 0x%012X  (%d)%n", seed, seed);
        return seed;
    }

    private static long applyOffset(int idx, long seed, int n) {
        switch (idx) {
            case 0: return Offsets.baseTick(seed, n);
            case 1: return Offsets.hurt(seed, n);
            case 2: return Offsets.rewardTradeXp(seed, n);
            case 3: return Offsets.frostWalk(seed, n);
            case 4: return Offsets.ambientSound(seed, n);
            case 5: return Offsets.raidCheck(seed, n);
            case 6: { // level_up_slot_select: needs listSize
                int listSize;
                try { listSize = Integer.parseInt(prompt("  Listing count for this profession+level: ").trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid."); return seed; }
                return Offsets.levelUpSlotSelect(seed, listSize, n);
            }
            case 7: { // level_up_offer_param: needs profession+level
                String prof = selectProfession();
                int lvl = selectLevel(prof);
                return Offsets.levelUpOfferParam(seed, prof, lvl, n);
            }
            case 8: return Offsets.rawSteps(seed, n);
            default: return seed;
        }
    }

    // ── Mode 4: Predict ──────────────────────────────────────────────────────
    static void modePredict(Long seed) {
        System.out.println("\n--- Mode 4: Predict target trade ---");
        if (seed == null) seed = inputSeed("Enter current seed (hex or decimal): ");

        String profession = selectProfession();
        int level = selectLevel(profession);
        Predicate<TradeResult[]> matchFn = buildMatchFn(profession, level);

        String ms = prompt("Max ticks to search (1 tick = 1 updateTrades cycle, NOT raw LCG steps, Enter=16M): ").trim();
        int maxSteps = ms.isEmpty() ? 1 << 24 : Integer.parseInt(ms);

        while (true) {
            System.out.println("\nSearching up to " + maxSteps + " steps...");
            List<long[]> results = searchForTarget(seed, profession, level, matchFn, maxSteps);

            if (results.isEmpty()) {
                System.out.println("No match found.");
                String c = prompt("  0. Back to mode select  1. Search more steps: ").trim();
                if (!c.equals("1")) return;
                try { maxSteps = Integer.parseInt(prompt("  New max steps: ").trim()); }
                catch (NumberFormatException e) { return; }
                continue;
            }

            System.out.printf("%nFound %d match(es):%n%n", results.size());
            for (long[] r : results) {
                long steps = r[0];
                long triggerSeed = r[1];
                System.out.printf("  raw LCG steps to trigger = %d  |  seed at updateTrades entry = 0x%012X%n", steps, triggerSeed);
                // print 3 seeds leading up to trigger
                if (steps >= 3) {
                    Rand pre = Rand.ofActual(seed);
                    pre.advance(steps - 3); System.out.printf("    step %-6d seed = 0x%012X%n", steps - 3, pre.getActualSeed());
                    pre.advance(1);         System.out.printf("    step %-6d seed = 0x%012X%n", steps - 2, pre.getActualSeed());
                    pre.advance(1);         System.out.printf("    step %-6d seed = 0x%012X%n", steps - 1, pre.getActualSeed());
                }
                System.out.printf("    step %-6d seed = 0x%012X  <-- updateTrades entry%n", steps, triggerSeed);
                Rand rr = Rand.ofActual(triggerSeed);
                TradeResult[] trades = Simulator.simulateUpdateTrades(rr, profession, level);
                for (int i = 0; i < trades.length; i++)
                    System.out.println("    slot " + i + ": " + trades[i]);
                System.out.println();
            }
            return;
        }
    }

    // ── Mode 1: All ──────────────────────────────────────────────────────────
    static void modeAll() {
        System.out.println("\n--- Mode 1: Crack + Forward + Predict ---");
        List<Long> seeds = modeCrack();
        if (seeds.isEmpty()) return;
        if (seeds.size() > 1) System.out.println("Multiple seeds found. Using first.");
        long seed = seeds.get(0);
        seed = modeForward(seed);
        modePredict(seed);
    }

    // ── Search ───────────────────────────────────────────────────────────────
    private static List<long[]> searchForTarget(long seed, String profession, int level,
            Predicate<TradeResult[]> matchFn, int maxSteps) {
        List<long[]> results = new ArrayList<>();
        Rand r = Rand.ofActual(seed);
        for (int tick = 0; tick < maxSteps; tick++) {
            r.nextInt(1000); // ambient, 1 step
            long seedAfterAmbient = r.getActualSeed();
            long stepsToAmbient = r.getStepCount();
            TradeResult[] trades = Simulator.simulateUpdateTrades(r, profession, level);
            if (matchFn.test(trades)) {
                results.add(new long[]{stepsToAmbient, seedAfterAmbient});
                if (results.size() >= 5) break;
            }
            r.nextInt(100); // raid_check
        }
        return results;
    }

    // ── UI helpers ───────────────────────────────────────────────────────────
    private static String selectProfession() {
        String[] profs = Simulator.getProfessions();
        System.out.println("\nProfessions:");
        for (int i = 0; i < profs.length; i++) System.out.println("  " + i + ". " + profs[i]);
        int idx = inputInt("Select: ", 0, profs.length - 1);
        return profs[idx];
    }

    private static int selectLevel(String profession) {
        int[] levels = Simulator.getLevels(profession);
        System.out.print("Levels: ");
        for (int l : levels) System.out.print(l + " ");
        System.out.println();
        return inputInt("Level: ", levels[0], levels[levels.length - 1]);
    }

    private static Predicate<TradeResult[]> buildMatchFn(String profession, int level) {
        System.out.println("\nTarget trade type:");
        System.out.println("  0. enchant_book  - match by enchantment name [+level] [+max_price]");
        System.out.println("  1. enchanted_item - match by enchantment name on item");
        System.out.println("  2. tipped_arrow  - match by potion name");
        System.out.println("  3. any           - show all results");
        int choice = inputInt("Type: ", 0, 3);

        if (choice == 3) return trades -> true;

        if (choice == 0) {
            printTradeableEnchants();
            int idx = inputInt("Enchantment index: ", 0, Enchantments.TRADEABLE_COUNT - 1);
            String targetName = Enchantments.TRADEABLE_NAMES[idx];
            int mn = Enchantments.TRADEABLE_MIN[idx], mx = Enchantments.TRADEABLE_MAX[idx];
            Integer targetLvl = null;
            if (mn != mx) {
                String s = prompt("Level (" + mn + "-" + mx + ", Enter=any): ").trim();
                if (!s.isEmpty()) targetLvl = Integer.parseInt(s);
            }
            String ps = prompt("Max price in emeralds (Enter=any): ").trim();
            Integer maxPrice = ps.isEmpty() ? null : Integer.parseInt(ps);
            final Integer tl = targetLvl; final Integer mp = maxPrice;
            return trades -> {
                for (TradeResult t : trades) {
                    if (t.type == TradeResult.Type.ENCHANT_BOOK) {
                        for (String[] e : t.enchantments) {
                            if (e[0].equals(targetName)) {
                                if (tl != null && Integer.parseInt(e[1]) != tl) continue;
                                if (mp != null && t.price > mp) continue;
                                return true;
                            }
                        }
                    }
                }
                return false;
            };
        }

        if (choice == 1) {
            printTradeableEnchants();
            int idx = inputInt("Enchantment index: ", 0, Enchantments.TRADEABLE_COUNT - 1);
            String targetName = Enchantments.TRADEABLE_NAMES[idx];
            return trades -> {
                for (TradeResult t : trades)
                    if (t.type == TradeResult.Type.ENCHANTED_ITEM)
                        for (String[] e : t.enchantments)
                            if (e[0].equals(targetName)) return true;
                return false;
            };
        }

        // choice == 2: tipped_arrow
        System.out.println("\nBrewable potions:");
        for (int i = 0; i < Enchantments.BREWABLE_COUNT; i++)
            System.out.println("  " + i + ". " + Enchantments.BREWABLE_POTIONS[i]);
        int idx = inputInt("Potion index: ", 0, Enchantments.BREWABLE_COUNT - 1);
        String targetPotion = Enchantments.BREWABLE_POTIONS[idx];
        return trades -> {
            for (TradeResult t : trades)
                if (t.type == TradeResult.Type.TIPPED_ARROW && targetPotion.equals(t.potion)) return true;
            return false;
        };
    }

    private static void printTradeableEnchants() {
        System.out.println("\nTradeable enchantments:");
        for (int i = 0; i < Enchantments.TRADEABLE_COUNT; i++) {
            int mn = Enchantments.TRADEABLE_MIN[i], mx = Enchantments.TRADEABLE_MAX[i];
            String lvl = (mn == mx) ? "Lv" + mn : "Lv" + mn + "-" + mx;
            System.out.printf("  %2d. %-25s %s%n", i, Enchantments.TRADEABLE_NAMES[i], lvl);
        }
    }

    private static long inputSeed(String prompt) {
        while (true) {
            String s = prompt(prompt).trim();
            try {
                return s.startsWith("0x") || s.startsWith("0X")
                    ? Long.parseUnsignedLong(s.substring(2), 16)
                    : Long.parseLong(s);
            } catch (NumberFormatException e) { System.out.println("Enter decimal or hex (0x...) seed."); }
        }
    }

    private static int inputInt(String prompt, int lo, int hi) {
        while (true) {
            try {
                int v = Integer.parseInt(prompt(prompt).trim());
                if (v >= lo && v <= hi) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid input (" + lo + "-" + hi + ").");
        }
    }

    private static String prompt(String msg) {
        System.out.print(msg);
        return SC.hasNextLine() ? SC.nextLine() : "";
    }
}
