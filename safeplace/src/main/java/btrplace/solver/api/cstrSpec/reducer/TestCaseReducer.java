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
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class TestCaseReducer {

    private ImplVerifier verif;

    private List<TestCase> mins;

    public TestCaseReducer() {
        verif = new ImplVerifier();
    }

    public List<TestCase> reduce(TestCase c, Constraint cstr, Map<String, Object> in) {
        mins = new ArrayList<>();
        reduce(0, c, cstr, in);
        return mins;
    }

    private boolean reduce(int lvl, TestCase t, btrplace.solver.api.cstrSpec.Constraint cstr, Map<String, Object> in) {
        System.out.println(indent(lvl) + "Reduce " + t.getPlan().getActions());
        TestResult res = verif.verify(t, false);
        if (res.succeeded()) {
            System.out.println(indent(lvl) + "-> Succeeded. Throw away");
            return true;
        }
        if (t.getPlan().getSize() <= 1) {
            System.out.println(indent(lvl) + "-> Minimal");
            mins.add(t);
            return false;
        } else {
            System.out.println(indent(lvl) + "-> Splittable");
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
                System.out.println(indent(lvl) + "split 1: " + p1.getActions());
                System.out.println(indent(lvl) + "split 2: " + p2.getActions());
                TestCase c1 = new TestCase(t.num(), p1, t.getSatConstraint(), cstr.instantiate(in, p1));
                TestCase c2 = new TestCase(t.num(), p2, t.getSatConstraint(), cstr.instantiate(in, p2));
                decidable = reduce(lvl + 1, c1, cstr, in);
                decidable &= reduce(lvl + 1, c2, cstr, in);
                decidable = !decidable;
                sep = (sep + 1) % max;
                if (sep == middle) {
                    System.out.println(indent(lvl) + "unable to make a valuable split");
                    break;
                }
            }
        }
        return false;
    }

    private String indent(int l) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < l; i++) {
            b.append("\t");
        }
        return b.toString();
    }
}
