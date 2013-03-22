package btrplace.plan;

import btrplace.model.Model;

import java.util.HashSet;
import java.util.Set;

/**
 * A reconfiguration plan applier that relies on the dependencies between the actions composing the plan.
 * Only unblocked actions are executed. Once executed, the unblocked actions are executed.
 * <p/>
 * This process is repeated until all the actions are executed. This process is ensure to finish
 * iff their is no cyclic dependencies.
 *
 * @author Fabien Hermenier
 */
public class DependencyBasedPlanApplier implements ReconfigurationPlanApplier {

    private static final DependencyBasedPlanApplier instance = new DependencyBasedPlanApplier();

    private DependencyBasedPlanApplier() {
    }

    @Override
    public Model apply(ReconfigurationPlan p) {
        int nbCommitted = 0;
        ReconfigurationPlanMonitor rpm = new DefaultReconfigurationPlanMonitor(p);
        Set<Action> feasible = new HashSet<Action>();
        for (Action a : p.getActions()) {
            if (!rpm.isBlocked(a)) {
                feasible.add(a);
            }
        }
        while (nbCommitted != p.getSize()) {
            Set<Action> newFeasibles = new HashSet<Action>();
            for (Action a : feasible) {
                Set<Action> s = rpm.commit(a);
                if (s == null) {
                    return null;
                }
                newFeasibles.addAll(s);
                nbCommitted++;
            }
            feasible = newFeasibles;
        }

        return rpm.getCurrentModel();
    }

    @Override
    public String toString(ReconfigurationPlan p) {
        StringBuilder b = new StringBuilder();
        for (Action a : p) {
            b.append(p.getDirectDependencies(a)).append(" -> ").append(a).append("\n");
        }
        return b.toString();
    }

    /**
     * Get the unique instance of this applier.
     *
     * @return the singleton
     */
    public static DependencyBasedPlanApplier getInstance() {
        return instance;
    }
}
