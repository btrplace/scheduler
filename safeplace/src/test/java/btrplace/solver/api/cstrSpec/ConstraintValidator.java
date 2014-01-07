package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.TimedBasedActionComparator;
import btrplace.plan.event.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintValidator {

    private Model current;

    private Constraint cstr;

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    public boolean check(ReconfigurationPlan p, Constraint cstr, List<Object> args) throws Exception {

        Model res = p.getOrigin().clone();
        List<Action> actions = new ArrayList<>(p.getActions());
        Collections.sort(actions, startFirstComparator);
        for (Action a : actions) {
            if (!a.apply(res)) {
                throw new Exception("Unable to apply action '" + a + "':\n---Plan---\n" + p + "\n---Current model---\n" + res);
            }
            Boolean b = cstr.eval(res, args);
            cstr.reset();
            if (b == null || b == Boolean.FALSE) {
                throw new Exception("Unable to apply action '" + a + "':\n---Plan---\n" + p + "\n---Current model---\n" + res + "\n--- Invariant ---\n" + cstr.getProposition());
            }
        }
        return true;
    }


}
