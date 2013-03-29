package btrplace.plan;

import btrplace.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An applier that rely on the estimated start moment and duration of the actions to execute a plan.
 *
 * @author Fabien Hermenier
 */
public final class TimeBasedPlanApplier implements ReconfigurationPlanApplier {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    private static final TimeBasedPlanApplier instance = new TimeBasedPlanApplier();

    private TimeBasedPlanApplier() {
    }

    /**
     * Get the unique instance of this applier.
     *
     * @return the singleton
     */
    public static TimeBasedPlanApplier getInstance() {
        return instance;
    }

    @Override
    public Model apply(ReconfigurationPlan p) {
        Model res = p.getOrigin().clone();
        List<Action> actions = new ArrayList<Action>(p.getActions());
        Collections.sort(actions, startFirstComparator);
        for (Action a : actions) {
            if (!a.apply(res)) {
                return null;
            }
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
