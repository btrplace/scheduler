package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

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
public class PlanReducer extends Reducer {

    @Override
    public CTestCase reduce(CTestCase tc, SpecVerifier v1, Verifier v2, CTestCaseResult.Result errType) {
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getParameters();
        if (consistent(v1, v2, tc, errType)) {
            System.err.println("ORIGINALLY SATISFIED");
        }
        List<ReconfigurationPlan> mins = new ArrayList<>();
        _reduce(v1, v2, p, cstr, in, mins, tc.continuous(), errType);
        CTestCase red = derive(tc, in, mins.get(0));
        if (consistent(v1, v2, red, errType)) {
            System.err.println("BUG while reducing plan was:");
            System.err.println(tc);
            System.err.println("Now: " + red);
            System.err.println(tc.getPlan().equals(mins.get(0)));
            //System.exit(1);
        }
        //System.out.println("Reduced from " + p.getSize() + " action(s) to " + mins.get(0).getSize());
        return derive(tc, in, mins.get(0));
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

    private boolean _reduce(SpecVerifier v1, Verifier v2, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List<ReconfigurationPlan> mins, boolean c, CTestCaseResult.Result errType) {
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
            e1 = consistent(v1, v2, cstr, in, subs[0], c, errType);
            e2 = consistent(v1, v2, cstr, in, subs[1], c, errType);
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
        return _reduce(v1, v2, !e1 ? subs[0] : subs[1], cstr, in, mins, c, errType);
    }
}
