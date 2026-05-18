package villager;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import com.seedfinding.latticg.util.LCG;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

/**
 * Cracks 48-bit Java Random seed from nextInt(61) observations (frost-walker ice delays).
 * Uses LattiCG lattice attack — milliseconds instead of hours.
 *
 * observations[i] = elapsed_ticks - 60  (range [0, 60])
 */
public final class SeedCracker {

    /**
     * @param observations list of nextInt(61) values (elapsed - 60)
     * @return list of matching actual seeds (after last observation)
     */
    public static List<Long> crack(List<Integer> observations) {
        DynamicProgram dp = DynamicProgram.create(LCG.JAVA);
        for (int obs : observations) {
            dp.add(JavaCalls.nextInt(61).equalTo(obs));
        }
        LongStream stream = dp.reverse();

        // LattiCG returns internal seeds at the START of the sequence.
        // Advance by observation count to get seed after last nextInt(61).
        LCG skip = LCG.JAVA.combine(observations.size());
        List<Long> results = new ArrayList<>();
        stream.forEach(internalSeed -> {
            long afterInternal = skip.nextSeed(internalSeed);
            // actual seed = internal XOR multiplier
            results.add(afterInternal ^ LCG.JAVA.multiplier);
        });
        return results;
    }

    private SeedCracker() {}
}
