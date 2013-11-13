package btrplace.solver.api.cstrSpec.generator;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generate all the possible reconfiguration plans.
 * @author Fabien Hermenier
 */
public class DelaysGenerator implements Iterable<ReconfigurationPlan>, Iterator<ReconfigurationPlan> {

    private TupleGenerator<Action> tg;

    private ReconfigurationPlan src;

    public DelaysGenerator(ReconfigurationPlan src) {
        this.src = src;
        List<List<Action>> possibles = makePossibleDelays(src);
        tg = new TupleGenerator<>(Action.class, possibles);
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


    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReconfigurationPlan next() {
        ReconfigurationPlan p = new DefaultReconfigurationPlan(src.getOrigin());
        for (Action a : tg.next()) {
            p.add(a);
        }
        return p;
    }

    @Override
    public boolean hasNext() {
        return tg.hasNext();
    }

    public void reset() {
        tg.reset();
    }

    @Override
    public Iterator<ReconfigurationPlan> iterator() {
        return this;
    }
}
