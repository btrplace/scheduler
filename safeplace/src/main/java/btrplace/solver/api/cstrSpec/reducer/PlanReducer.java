package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.CstrSpecEvaluator;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Reduce the number of actions in the plan to the possibel.
 *
 * @author Fabien Hermenier
 */
public class PlanReducer implements TestCaseReducer {

    private ImplVerifier verif;

    private CstrSpecEvaluator cVerif;

    public PlanReducer() {
        verif = new ImplVerifier();
        cVerif = new CstrSpecEvaluator();
    }

    @Override
    public List<TestCase> reduce(TestCase c, Constraint cstr, List<Constant> in) {
        List<TestCase> mins = new ArrayList<>();
        reduce(0, c, cstr, in, mins);
        return mins;
    }

    private boolean reduce(int lvl, TestCase t, Constraint cstr, List<Constant> in, List<TestCase> mins) {
        TestResult res = verif.verify(t, false);
        if (res.succeeded()) {
            return true; //On a enlevé qqch qui crééait l'erreur.
        } else if (res.errorType() == TestResult.ErrorType.bug) {
            //Skip, it's because the tested constraint in no longer in the plan and it's a state change
            return false;
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
                System.out.println("Split 1:\n" + p1);
                System.out.println("Split 2:\n" + p2);
                TestCase c1 = new TestCase(t.num(), p1, t.getSatConstraint(), cVerif.eval(cstr, p1, in));
                TestCase c2 = new TestCase(t.num(), p2, t.getSatConstraint(), cVerif.eval(cstr, p2, in));
                decidable = reduce(lvl + 1, c1, cstr, in, mins);
                decidable &= reduce(lvl + 1, c2, cstr, in, mins);
                decidable = !decidable;
                sep = (sep + 1) % max;
                if (sep == middle) {
                    break;
                }
                System.out.println("Not decidable !");
            }
        }
        return false;
    }
}
