package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate all the possible reconfiguration plans.
 *
 * @author Fabien Hermenier
 */
public class DurationsGenerator extends ReconfigurationPlanVariations {

    public DurationsGenerator(ReconfigurationPlan src, int lb, int ub) {
        this(src, lb, ub, true);
    }

    public DurationsGenerator(ReconfigurationPlan src, int lb, int ub, boolean fuzz) {
        super(src);
        List<List<Action>> possibles = makePossibleDurations(src, lb, ub);
        if (!fuzz) {
            tg = new AllTuplesGenerator<>(Action.class, possibles);
        } else {
            tg = new RandomTuplesGenerator<>(Action.class, possibles);
        }
    }

    private List<List<Action>> makePossibleDurations(ReconfigurationPlan src, int lb, int ub) {
        List<List<Action>> l = new ArrayList<>(src.getSize());
        for (Action a : src) {
            ArrayList<Action> dom = new ArrayList<>(ub - lb + 1);
            for (int i = lb; i <= ub; i++) {
                dom.add(Actions.newDuration(a, i));
            }
            l.add(dom);
        }
        return l;
    }
}
