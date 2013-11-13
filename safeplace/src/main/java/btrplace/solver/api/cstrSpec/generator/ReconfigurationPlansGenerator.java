package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generate all the possible reconfiguration plans.
 * @author Fabien Hermenier
 */
public class ReconfigurationPlansGenerator implements Iterable<ReconfigurationPlan>, Iterator<ReconfigurationPlan> {

    private TupleGenerator<Action> tg;

    private Model src;

    public ReconfigurationPlansGenerator(Model src) {
        List<List<Action>> possibles = makePossibleActions(src);
        tg = new TupleGenerator<>(Action.class, possibles);
        this.src = src;
    }

    private List<List<Action>> makePossibleActions(Model src) {
        List<List<Action>> l = new ArrayList<>();
        Mapping m = src.getMapping();
        for (Node n : m.getOnlineNodes()) {
             List<Action> as = new ArrayList<>(2);
             as.add(null);
             as.add(new ShutdownNode(n, 0, 3));
             l.add(as);
        }
        for (Node n : m.getOfflineNodes()) {
            List<Action> as = new ArrayList<>(2);
            as.add(null);
            as.add(new BootNode(n, 0, 3));
            l.add(as);
        }

        for (VM v : m.getRunningVMs()) {
            List<Action> dom = new ArrayList<>(m.getNbNodes() * 2 + 1);
            Node loc = m.getVMLocation(v);
            dom.add(new SuspendVM(v, loc, loc, 0, 3));
            dom.add(new ShutdownVM(v, loc, 0, 3));
            for (Node n : m.getAllNodes()) {
                if (n.equals(loc)) {
                    dom.add(null); //no action.
                } else {
                    dom.add(new MigrateVM(v, loc, n, 0, 3)); //Warning with staying node
                }
            }
            l.add(dom);
        }
        for (VM v : m.getReadyVMs()) {
            List<Action> dom = new ArrayList<>(m.getNbNodes() + 1);
            for (Node n : m.getAllNodes()) {
                dom.add(new BootVM(v, n, 0, 3)); //Warning with staying node
            }
            dom.add(null);
            l.add(dom);

        }
        for (VM v : m.getSleepingVMs()) {
            List<Action> dom = new ArrayList<>(2);
            dom.add(null);
            Node loc = m.getVMLocation(v);
            dom.add(new ResumeVM(v, loc, loc, 0, 3));
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
        ReconfigurationPlan p = new DefaultReconfigurationPlan(src);
        for (Action a : tg.next()) {
            if (a != null) {
                p.add(a);
            }
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
