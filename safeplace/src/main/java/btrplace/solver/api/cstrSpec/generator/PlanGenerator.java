package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generate all the possible reconfiguration plans.
 * @author Fabien Hermenier
 */
public class PlanGenerator {

    public List<ReconfigurationPlan> anyAction(Model src) {
        List<ReconfigurationPlan> l = new ArrayList<>();
        for (ReconfigurationPlan p : plansWithNodeActions(src)) {
            l.addAll(planWithVMs(p));
        }
        return l;
    }
    public List<ReconfigurationPlan> plansWithNodeActions(Model src) {
        Mapping m = src.getMapping();
        int nbStates = (int) Math.pow(2, m.getNbNodes());
        Set<Node> nodes = m.getAllNodes();
        List<ReconfigurationPlan> plans = new ArrayList<>();
        for (int id = 0; id < nbStates; id++) {
            int st = id;
            ReconfigurationPlan plan = new DefaultReconfigurationPlan(src);
            for (Node n : nodes) {
                if (st % 2 == 0) {
                    if (m.isOnline(n)) {
                        plan.add(new ShutdownNode(n, 0, 3));
                    } else {
                        plan.add(new BootNode(n, 0, 3));
                    }
                }
                st = st >> 1;
            }
            plans.add(plan);
        }
        return plans;
    }

    public List<ReconfigurationPlan> planWithVMs(ReconfigurationPlan p) {
        List<ReconfigurationPlan> res = new ArrayList<>();
        List<List<Action>> possibleActions = new ArrayList<>();
        Mapping m = p.getOrigin().getMapping();
        for (VM v : m.getRunningVMs()) {
            List<Action> dom = new ArrayList<>();
            dom.add(new SuspendVM(v, m.getVMLocation(v), m.getVMLocation(v), 0, 3));
            dom.add(new ShutdownVM(v, m.getVMLocation(v), 0, 3));
            for (Node n : m.getAllNodes()) {
                if (n.equals(m.getVMLocation(v))) {
                    dom.add(null); //no action.
                } else {
                    dom.add(new MigrateVM(v, m.getVMLocation(v), n, 0, 3)); //Warning with staying node
                }
            }
            possibleActions.add(dom);
        }
        for (VM v : m.getReadyVMs()) {
            List<Action> dom = new ArrayList<>();
            for (Node n : m.getAllNodes()) {
                dom.add(new BootVM(v, n, 0, 3)); //Warning with staying node
            }
            dom.add(null);
            possibleActions.add(dom);

        }
        for (VM v : m.getSleepingVMs()) {
            List<Action> dom = new ArrayList<>();
            dom.add(null);
            dom.add(new ResumeVM(v, m.getVMLocation(v), m.getVMLocation(v), 0, 3));
            possibleActions.add(dom);
        }

        for (List<Action> useCase : Collections.allTuples(possibleActions)) {
            ReconfigurationPlan p2 = new DefaultReconfigurationPlan(p.getOrigin());
            for (Action a : p) {
                p2.add(a);
            }
            for (Action a: useCase) {
                if (a != null) {
                    p2.add(a);
                }
            }
            res.add(p2);
        }
        return res;
    }


    public List<ReconfigurationPlan> anyDuration(ReconfigurationPlan p, int lb, int ub) {
        List<ReconfigurationPlan> plans = new ArrayList<>();
        List<List<Action>> possibleDurations = new ArrayList<>();
        for (Action a : p) {
            List<Action> dom = new ArrayList<>();
            for (int i = lb; i <= ub; i++) {
                dom.add(newDuration(a, i));
            }
            possibleDurations.add(dom);
        }

        for (List<Action> possible : Collections.allTuples(possibleDurations)) {
            ReconfigurationPlan p2 = new DefaultReconfigurationPlan(p.getOrigin());
            for (Action a : possible) {
                p2.add(a);
            }
            plans.add(p2);
        }
        return plans;
    }

    public List<ReconfigurationPlan> anyDelay(ReconfigurationPlan p) {
        List<ReconfigurationPlan> res = new ArrayList<>();
        int horizon = 0;
        for (Action a : p) {
            horizon += (a.getEnd() - a.getStart());
        }
        List<List<Action>> possibleDelays = new ArrayList<>();
        for (Action a : p) {
            int duration = a.getEnd() - a.getStart();
            List<Action> dom = new ArrayList<>();
            for (int i = 0; i <= horizon - duration; i++) {
                dom.add(newDelay(a, i));
            }
            possibleDelays.add(dom);
        }

        for (List<Action> possible : Collections.allTuples(possibleDelays)) {
            ReconfigurationPlan p2 = new DefaultReconfigurationPlan(p.getOrigin());
            for (Action a : possible) {
                p2.add(a);
            }
            res.add(p2);
        }
        return res;
    }

    private Action newAction(Action a, int st, int ed) {
        if (a instanceof MigrateVM) {
            MigrateVM m = (MigrateVM) a;
            return new MigrateVM(m.getVM(), m.getSourceNode(), m.getDestinationNode(), st, ed);
        } else if (a instanceof BootVM) {
            BootVM m = (BootVM) a;
            return new BootVM(m.getVM(), m.getDestinationNode(), st, ed);
        } else if (a instanceof ShutdownVM) {
            ShutdownVM m = (ShutdownVM) a;
            return new ShutdownVM(m.getVM(), m.getNode(), st, ed);
        } else if (a instanceof BootNode) {
            BootNode m = (BootNode) a;
            return new BootNode(m.getNode(), st, ed);
        } else if (a instanceof ShutdownNode) {
            ShutdownNode m = (ShutdownNode) a;
            return new ShutdownNode(m.getNode(), st, ed);
        } else if (a instanceof SuspendVM) {
            SuspendVM m = (SuspendVM) a;
            return new SuspendVM(m.getVM(), m.getSourceNode(), m.getDestinationNode(), st, ed);
        } else if (a instanceof ResumeVM) {
            ResumeVM m = (ResumeVM) a;
            return new ResumeVM(m.getVM(), m.getSourceNode(), m.getDestinationNode(), st, ed);
        } else {
            throw new UnsupportedOperationException("Unsupported action '" + a + "'");
        }
    }

    private Action newDelay(Action a, int d) {
        return newAction(a, d, a.getEnd() - a.getStart() + d);
    }

    private Action newDuration(Action a, int d) {
        return newAction(a, a.getStart(), a.getStart() + d);
    }
}
