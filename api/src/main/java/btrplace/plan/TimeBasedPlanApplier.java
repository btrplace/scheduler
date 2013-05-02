package btrplace.plan;

import btrplace.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An applier that rely on the estimated consume moment and duration of the actions to execute a plan.
 *
 * @author Fabien Hermenier
 */
public class TimeBasedPlanApplier extends DefaultPlanApplier {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    /**
     * Make a new applier.
     */
    public TimeBasedPlanApplier() {
        super();
    }

    @Override
    public Model apply(ReconfigurationPlan p) {
        Model res = p.getOrigin().clone();
        List<Action> actions = new ArrayList<>(p.getActions());
        Collections.sort(actions, startFirstComparator);
        for (Action a : actions) {
            if (!a.apply(res)) {
                return null;
            }
            fireAction(a);
        }
        return res;
    }

    @Override
    public String toString(ReconfigurationPlan p) {
        StringBuilder b = new StringBuilder();
        for (Action a : p.getActions()) {
            b.append(a.getStart()).append(':').append(a.getEnd()).append(' ').append(a.toString()).append('\n');
        }
        return b.toString();
    }
}
