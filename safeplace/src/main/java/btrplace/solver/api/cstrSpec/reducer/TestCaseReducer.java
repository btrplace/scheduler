package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCaseReducer {

    private ImplVerifier verif;

    private List<TestCase> mins;

    public TestCaseReducer() {
        verif = new ImplVerifier();
    }

    public List<TestCase> reducePlan(TestCase c, Constraint cstr, List<Object> in) {
        mins = new ArrayList<>();
        reducePlan(0, c, cstr, in);
        return mins;
    }

    private boolean reducePlan(int lvl, TestCase t, btrplace.solver.api.cstrSpec.Constraint cstr, List<Object> in) {
        TestResult res = verif.verify(t, false);
        if (res.succeeded()) {
            return true;
        }
        if (t.getPlan().getSize() <= 1) {
            mins.add(t);
            return false;
        } else {
            int middle = t.getPlan().getSize() / 2;
            int sep = middle;
            int max = t.getPlan().getSize();

            boolean decidable = false;
            while (!decidable) {
                ReconfigurationPlan p1 = new DefaultReconfigurationPlan(t.getPlan().getOrigin());
                ReconfigurationPlan p2 = new DefaultReconfigurationPlan(t.getPlan().getOrigin());
                int i = 0;
                for (Action a : t.getPlan()) {
                    if (i++ < sep) {
                        p1.add(a);
                    } else {
                        p2.add(a);
                    }
                }
                TestCase c1 = new TestCase(t.num(), p1, t.getSatConstraint(), cstr.eval(p1.getResult(), in));
                TestCase c2 = new TestCase(t.num(), p2, t.getSatConstraint(), cstr.eval(p2.getResult(), in));
                decidable = reducePlan(lvl + 1, c1, cstr, in);
                decidable &= reducePlan(lvl + 1, c2, cstr, in);
                decidable = !decidable;
                sep = (sep + 1) % max;
                if (sep == middle) {
                    break;
                }
            }
        }
        return false;
    }
}
