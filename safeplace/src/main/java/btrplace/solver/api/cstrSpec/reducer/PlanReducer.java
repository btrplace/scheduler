package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.ArrayList;
import java.util.Arrays;
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

    private ImplVerifier verif;

    private SpecVerifier cVerif;

    public PlanReducer() {
        verif = new ImplVerifier();
        cVerif = new SpecVerifier();
    }

    private boolean consistent(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws Exception {
        TestCase tc = new TestCase(Arrays.asList(verif, cVerif), cstr, p, in, true);
        return tc.succeed();
    }

    public ReconfigurationPlan reduce(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws Exception {

        if (consistent(p, cstr, in)) {
            return p;
        }

        List<ReconfigurationPlan> mins = new ArrayList<>();
        _reduce(p, cstr, in, mins);
        return mins.get(0);
    }

    private boolean _reduce(ReconfigurationPlan p, Constraint cstr, List<Constant> in, List<ReconfigurationPlan> mins) throws Exception {
        if (p.getSize() <= 1) {
            mins.add(p);
        } else {
            int middle = p.getSize() / 2;
            int sep = middle;
            int max = p.getSize();

            boolean e1, e2;
            while (true) {
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
                //System.out.println("Split 1:\n" + p1);
                //System.out.println("Split 2:\n" + p2);
                e1 = consistent(p1, cstr, in);
                e2 = consistent(p2, cstr, in);
                sep = (sep + 1) % max;
                if (sep == middle) {
                    break;
                }
                //Only one must have the same error
                //System.out.println("Want " + err + "\tSplit 1:" + e1 + "\tSplit 2: " + e2);
                if (e1 ^ e2) {
                    if (e1) {
                        return _reduce(p1, cstr, in, mins);
                    } else {
                        return _reduce(p2, cstr, in, mins);
                    }
                }
            }
            //Not decidable, this is the reduced form
            mins.add(p);
            return false;
            //We decided
        }
        return true;
    }
}
