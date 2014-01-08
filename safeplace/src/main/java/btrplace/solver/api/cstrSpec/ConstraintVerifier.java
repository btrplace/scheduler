package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.TimedBasedActionComparator;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintVerifier {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    public Boolean eval(Constraint cstr, ReconfigurationPlan p, List<Object> values) {

        Model src = p.getOrigin();

        setInputs(cstr, src, values);

        Proposition good = cstr.getProposition();
        Proposition noGood = good.not();

        Model res = src.clone();
        List<Action> actions = new ArrayList<>(p.getActions());
        Collections.sort(actions, startFirstComparator);
        //System.out.println(p);
        for (Action a : actions) {
            Boolean bOk = cstr.getProposition().eval(src);
            Boolean bKO = cstr.getProposition().not().eval(src);

            if (bOk == null || bKO == null) {
                throw new RuntimeException("Both null !\ngood:" + good + "\nnotGood: " + noGood + "\n" + res.getMapping().toString());
            }
            if (bOk && bKO) {
                throw new RuntimeException(values + " good and bad !\ngood:" + good + "\nnotGood: " + noGood + "\n" + res.getMapping().toString());
            } else if (!(bOk || bKO)) {
                throw new RuntimeException("Nor good or bad !\ngood:" + good + "\nnotGood: " + noGood + "\n" + res.getMapping().toString());
            }
            //System.out.println("Good: " + bOk + " noGood: " + bKO);
            if (!bOk) {
                return false;
            }
            //System.out.println("Apply " + a + " on \n" + res.getMapping());
            if (!a.apply(res)) {
                System.out.println("Unable to apply " + a + " on \n" + res.getMapping());
                return false; //Not applyable
            }
            //System.out.println(a + " applied successfully");
        }
        cstr.reset();
        return true;
    }

    private void setInputs(Constraint c, Model src, List<Object> values) {
        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            if (!var.set(src, values.get(i))) {
                throw new IllegalArgumentException("Unable to set '" + var.label() + "' (type '" + var.type() + "') to '" + values.get(i) + "'");
            }
        }

    }
}
