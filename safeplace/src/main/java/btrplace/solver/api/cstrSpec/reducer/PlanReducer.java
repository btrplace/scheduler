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
public class PlanReducer implements Reducer {

    private boolean consistent(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) {
        TestCase tc = new TestCase(v, cstr, p, in, d);
        return tc.succeed();
    }

    @Override
    public TestCase reduce(TestCase tc) {
        if (tc.succeed()) {
            return tc;
        }
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getArguments();
        Verifier v = tc.getVerifier();

        List<ReconfigurationPlan> mins = new ArrayList<>();
        _reduce(v, p, cstr, in, mins, tc.isDiscrete());
        TestCase r = new TestCase(tc.getVerifier(), tc.getConstraint(), mins.get(0), tc.getArguments(), tc.isDiscrete());
        if (r.succeed()) {
            System.err.println("BUG while reducing plan was:");
            System.err.println(tc.pretty(true));
            System.err.println("Now: " + r.pretty(true));
            System.err.println(tc.getPlan().equals(r.getPlan()));
            //System.exit(1);
        }
        return r;
    }

    private ReconfigurationPlan[] splits(ReconfigurationPlan p, int sep) {
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

    private boolean _reduce(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List<ReconfigurationPlan> mins, boolean d) {
        //System.err.println("Working on " + p);
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
            e1 = consistent(v, subs[0], cstr, in, d);
            e2 = consistent(v, subs[1], cstr, in, d);
            if (e1 ^ e2) {
                //System.err.println("Valid split with " + Arrays.toString(subs));
                //Got a valid split
                break;
            }
            //Not a valid split, we adapt the separator
            sep = (sep + 1) % (max - 1);
            if (sep == 0) {
                sep = 1;
            }
            if (sep == middle) {
                //Not decidable, so minimal form
                mins.add(p);
                return true;
            }
        }
        //Investigate the right sub plan
        return _reduce(v, !e1 ? subs[0] : subs[1], cstr, in, mins, d);
    }
}
