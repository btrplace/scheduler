package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Reduce the number of actions in the plan to the minimum possible.
 * <p/>
 * It is a dichotomic approach that split the set of actions into two.
 * It stops when none of the splits or the two of the splits have a different error
 * than the original one.
 *
 * @author Fabien Hermenier
 */
public class PlanReducer {

    public PlanReducer() {

    }

    private boolean consistent(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws Exception {
        TestCase tc = new TestCase(v, cstr, p, in, true);
        return tc.succeed();
    }

    public TestCase reduce(TestCase tc) throws Exception {
        if (tc.succeed()) {
            return tc;
        }
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getArguments();
        Verifier v = tc.getVerifier();

        List<ReconfigurationPlan> mins = new ArrayList<>();
        _reduce(v, p, cstr, in, mins);
        return new TestCase(v, cstr, mins.get(0), in, tc.isDiscrete());
    }

    private ReconfigurationPlan[] splits(ReconfigurationPlan p, int sep) throws Exception {
        ReconfigurationPlan p1 = new DefaultReconfigurationPlan(p.getOrigin());
        ReconfigurationPlan p2 = new DefaultReconfigurationPlan(p.getOrigin());
        int i = 0;
        for (Action a : p) {
            if (i++ < sep) {
                p1.add(a);
            } else {
                p2.add(a);
            }
        }
        return new ReconfigurationPlan[]{p1, p2};
    }

    private boolean _reduce(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List<ReconfigurationPlan> mins) throws Exception {
        if (p.getSize() <= 1) {
            mins.add(p);
            //Minimal form
            return true;
        }

            int middle = p.getSize() / 2;
            int sep = middle;
            int max = p.getSize();

            boolean e1, e2;
        ReconfigurationPlan[] subs;
            while (true) {
                subs = splits(p, sep);
                e1 = consistent(v, subs[0], cstr, in);
                e2 = consistent(v, subs[1], cstr, in);
                if (e1 ^ e2) {
                    //Got a valid split
                    break;
                }
                //Not a valid split, we adapt the separator
                sep = (sep + 1) % max;
                if (sep == middle) {
                    //Not decidable, so minimal form
                    mins.add(p);
                    return true;
                }

            }
        //Investigate the right sub plan
        return _reduce(v, !e1 ? subs[0] : subs[1], cstr, in, mins);
    }
}
