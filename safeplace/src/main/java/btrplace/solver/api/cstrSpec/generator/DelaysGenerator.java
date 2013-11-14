package btrplace.solver.api.cstrSpec.generator;

import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate all the possible reconfiguration plans.
 *
 * @author Fabien Hermenier
 */
public class DelaysGenerator extends ReconfigurationPlanVariations {


    public DelaysGenerator(ReconfigurationPlan src) {
        super(src);
        List<List<Action>> possibles = makePossibleDelays(src);
        tg = new AllTuplesGenerator<>(Action.class, possibles);
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
