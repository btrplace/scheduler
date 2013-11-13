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
public class DurationsGenerator implements Iterable<ReconfigurationPlan>, Iterator<ReconfigurationPlan> {

    private TupleGenerator<Action> tg;

    private ReconfigurationPlan src;

    private int lb, ub;

    public DurationsGenerator(ReconfigurationPlan src, int lb, int ub) {
        this.src = src;
        this.lb = lb;
        this.ub = ub;
        List<List<Action>> possibles = makePossibleDurations(src);
        tg = new TupleGenerator<>(Action.class, possibles);
    }

    private List<List<Action>> makePossibleDurations(ReconfigurationPlan src) {
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
