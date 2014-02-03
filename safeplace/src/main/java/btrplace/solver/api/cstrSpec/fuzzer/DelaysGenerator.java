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
public class DelaysGenerator extends ReconfigurationPlanVariations {

    public DelaysGenerator(ReconfigurationPlan src, boolean fuzz) {
        super(src);
        List<List<Action>> possibles = makePossibleDelays(src);
        if (!fuzz) {
            tg = new AllTuplesGenerator<>(Action.class, possibles);
        } else {
            tg = new RandomTuplesGenerator<>(Action.class, possibles);
        }
    }

    public DelaysGenerator(ReconfigurationPlan src) {
        this(src, false);
    }

    private List<List<Action>> makePossibleDelays(ReconfigurationPlan src) {
        List<List<Action>> delays = new ArrayList<>(src.getSize());
        int horizonMax = 0;
        for (Action a : src) {
            horizonMax += (a.getEnd() - a.getStart());
        }

        for (Action a : src) {
            int duration = a.getEnd() - a.getStart();
            List<Action> dom = new ArrayList<>();
            for (int i = 0; i <= horizonMax - duration; i++) {
                dom.add(Actions.newDelay(a, i));
            }
            delays.add(dom);
        }
        return delays;
    }
}
