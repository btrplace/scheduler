package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.util.DefaultGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate all the possible reconfiguration plans.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlansGenerator extends DefaultGenerator<ReconfigurationPlan> {

    private Model src;

    private int duration;

    public ReconfigurationPlansGenerator(Model src, int d) {
        this.duration = d;
        List<List<Action>> possibles = makePossibleActions(src);
        tg = new AllTuplesGenerator<>(Action.class, possibles);
        this.src = src;

    }

    public ReconfigurationPlansGenerator(Model src) {
        this(src, 3);
    }

    private List<List<Action>> makePossibleActions(Model src) {
        List<List<Action>> l = new ArrayList<>();
        Mapping m = src.getMapping();

        for (Node n : m.getOnlineNodes()) {
            List<Action> as = new ArrayList<>(2);
            as.add(null);
            as.add(new ShutdownNode(n, 0, duration));
            l.add(as);
        }
        for (Node n : m.getOfflineNodes()) {
            List<Action> as = new ArrayList<>(2);
            as.add(null);
            as.add(new BootNode(n, 0, duration));
            l.add(as);
        }

        for (VM v : m.getRunningVMs()) {
            List<Action> dom = new ArrayList<>(m.getOnlineNodes().size() + 2);
            Node loc = m.getVMLocation(v);
            dom.add(new SuspendVM(v, loc, loc, 0, duration));
            dom.add(new ShutdownVM(v, loc, 0, duration));
            for (Node n : m.getAllNodes()) {
                if (n.equals(loc)) {
                    dom.add(null); //no action.
                } else {
                    dom.add(new MigrateVM(v, loc, n, 0, duration)); //Warning with staying node
                }
            }
            l.add(dom);
        }
        for (VM v : m.getReadyVMs()) {
            List<Action> dom = new ArrayList<>(m.getOnlineNodes().size() + 1);
            for (Node n : m.getAllNodes()) {
                dom.add(new BootVM(v, n, 0, duration)); //Warning with staying node
            }
            dom.add(null);
            l.add(dom);

        }
        for (VM v : m.getSleepingVMs()) {
            List<Action> dom = new ArrayList<>(2);
            dom.add(null);
            Node loc = m.getVMLocation(v);
            dom.add(new ResumeVM(v, loc, loc, 0, duration));
            l.add(dom);
        }
        return l;
    }

    @Override
    public ReconfigurationPlan next() {
        ReconfigurationPlan p = new DefaultReconfigurationPlan(src);
        for (Action a : (Action[]) tg.next()) {
            if (a != null) {
                p.add(a);
            }
        }
        return p;
    }
}
