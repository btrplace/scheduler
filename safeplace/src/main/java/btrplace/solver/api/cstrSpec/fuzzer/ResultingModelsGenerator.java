package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
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
public class ResultingModelsGenerator extends DefaultGenerator<Model> {

    private Model src;

    public ResultingModelsGenerator(Model src) {
        List<List<Action>> possibles = makePossibleActions(src);
        tg = new AllTuplesGenerator<>(Action.class, possibles);
        this.src = src;

    }

    private List<List<Action>> makePossibleActions(Model src) {
        List<List<Action>> l = new ArrayList<>();
        Mapping m = src.getMapping();

        for (Node n : m.getOnlineNodes()) {
            List<Action> as = new ArrayList<>(2);
            as.add(null);
            as.add(new ShutdownNode(n, 0, 1));
            l.add(as);
        }
        for (Node n : m.getOfflineNodes()) {
            List<Action> as = new ArrayList<>(2);
            as.add(null);
            as.add(new BootNode(n, 0, 1));
            l.add(as);
        }

        for (VM v : m.getRunningVMs()) {
            List<Action> dom = new ArrayList<>(m.getOnlineNodes().size() + 2);
            Node loc = m.getVMLocation(v);
            dom.add(new SuspendVM(v, loc, loc, 0, 1));
            dom.add(new ShutdownVM(v, loc, 0, 1));
            for (Node n : m.getAllNodes()) {
                if (n.equals(loc)) {
                    dom.add(null); //no action.
                } else {
                    dom.add(new MigrateVM(v, loc, n, 0, 1)); //Warning with staying node
                }
            }
            l.add(dom);
        }
        for (VM v : m.getReadyVMs()) {
            List<Action> dom = new ArrayList<>(m.getOnlineNodes().size() + 1);
            for (Node n : m.getAllNodes()) {
                dom.add(new BootVM(v, n, 0, 1)); //Warning with staying node
            }
            dom.add(null);
            l.add(dom);

        }
        for (VM v : m.getSleepingVMs()) {
            List<Action> dom = new ArrayList<>(2);
            dom.add(null);
            Node loc = m.getVMLocation(v);
            dom.add(new ResumeVM(v, loc, loc, 0, 1));
            l.add(dom);
        }
        return l;
    }

    @Override
    public Model next() {
        Model dst = src.clone();
        Mapping m = dst.getMapping();
        for (Action a : (Action[]) tg.next()) {
            if (a != null) {
                if (a instanceof BootNode) {
                    m.addOnlineNode(((BootNode) a).getNode());
                } else if (a instanceof ShutdownNode) {
                    m.addOfflineNode(((ShutdownNode) a).getNode());
                } else if (a instanceof BootVM) {
                    m.addRunningVM(((BootVM) a).getVM(), ((BootVM) a).getDestinationNode());
                } else if (a instanceof ShutdownVM) {
                    m.addReadyVM(((ShutdownVM) a).getVM());
                } else if (a instanceof SuspendVM) {
                    m.addSleepingVM(((SuspendVM) a).getVM(), ((SuspendVM) a).getDestinationNode());
                } else if (a instanceof ResumeVM) {
                    m.addSleepingVM(((ResumeVM) a).getVM(), ((ResumeVM) a).getDestinationNode());
                } else if (a instanceof MigrateVM) {
                    m.addRunningVM(((MigrateVM) a).getVM(), ((MigrateVM) a).getDestinationNode());
                }
            }
        }
        return dst;
    }
}
