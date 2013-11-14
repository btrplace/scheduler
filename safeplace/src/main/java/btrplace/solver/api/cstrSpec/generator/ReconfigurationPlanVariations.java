package btrplace.solver.api.cstrSpec.generator;

import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public abstract class ReconfigurationPlanVariations implements Generator<ReconfigurationPlan> {

    protected AllTuplesGenerator<Action> tg;

    private ReconfigurationPlan src;

    public ReconfigurationPlanVariations(ReconfigurationPlan src) {
        this.src = src;
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

    @Override
    public int count() {
        return tg.count();
    }

    @Override
    public int done() {
        return tg.done();
    }
}
