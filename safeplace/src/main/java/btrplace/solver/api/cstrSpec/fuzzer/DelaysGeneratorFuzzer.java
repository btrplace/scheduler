package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.util.Random;

/**
 * Generate all the possible reconfiguration plans.
 *
 * @author Fabien Hermenier
 */
public class DelaysGeneratorFuzzer {

    private int maxDuration;

    private static Random rnd = new Random();

    public static ReconfigurationPlan newDelayed(ReconfigurationPlan src) {
        int maxDuration = 0;
        for (Action a : src) {
            maxDuration += a.getEnd();
        }

        ReconfigurationPlan rp = new DefaultReconfigurationPlan(src.getOrigin());
        for (Action a : src) {
            int d = maxDuration - a.getEnd() + a.getStart();
            int st;
            if (d == 0) {
                st = 0;
            } else {
                st = rnd.nextInt(maxDuration - a.getEnd() + a.getStart());
            }
            int ed = st + (a.getEnd() - a.getStart());
            Action na = Actions.newAction(a, st, ed);
            rp.add(na);
        }
        return rp;
    }
}
