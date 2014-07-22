package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.plan.event.NodeEvent;
import btrplace.plan.event.VMEvent;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.Constraint2BtrPlace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Remove supposed useless VMs or nodes.
 * <p/>
 * This try to remove one by one VMs or nodes that are not involved
 * in the constraint. The actions that manipulate these elements are removed too.
 * An element is maintained in the plan if its removal changes the error
 *
 * @author Fabien Hermenier
 */
public class ElementsReducer implements Reducer {


    private boolean consistent(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) {
        TestCase tc = new TestCase(v, cstr, p, in, d);
        return tc.succeed();
    }

    @Override
    public TestCase reduce(TestCase tc) throws Exception {
        if (tc.succeed()) {
            return tc;
        }
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getArguments();
        Verifier v = tc.getVerifier();

        ReconfigurationPlan r1 = reduceVMs(v, p, cstr, in, tc.isDiscrete());
        ReconfigurationPlan res = reduceNodes(v, r1, cstr, in, tc.isDiscrete());
        TestCase r = new TestCase(v, cstr, res, in, tc.isDiscrete());
        if (r.succeed()) {
            System.err.println("BUG while reducing element(s):");
            System.err.println(tc.pretty(true));
            System.err.println("Now: " + r.pretty(true));
            System.err.println(tc.getPlan().equals(r.getPlan()));
            System.exit(1);
        }
        //System.out.println("From " + tc.pretty(true));
        //System.out.println("to " + r.pretty(true));
        return r;
    }

    public ReconfigurationPlan reduceVMs(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) throws Exception {
        SatConstraint s = Constraint2BtrPlace.build(cstr, in);
        List<VM> l = new ArrayList<>(p.getOrigin().getMapping().getAllVMs());
        l.removeAll(s.getInvolvedVMs());
        Iterator<VM> ite = l.iterator();

        Model mo = p.getOrigin().clone();
        ReconfigurationPlan red = new DefaultReconfigurationPlan(mo);
        for (Action a : p) {
            red.add(a);
        }
        while (ite.hasNext()) {
            VM vm = ite.next();
            ite.remove();
            if (!mo.getMapping().remove(vm)) {
                System.err.println("BUGGY");
            }
            Action removedAction = removeMine(red.getActions(), vm);
            if (consistent(v, red, cstr, in, d)) {
                undo(red, removedAction, vm, p);
            }
        }
        return red;
    }

    private Action removeMine(Collection<Action> actions, VM e) {
        Iterator<Action> ite = actions.iterator();
        while (ite.hasNext()) {
            Action a = ite.next();
            if (a instanceof VMEvent) {
                if (((VMEvent) a).getVM().equals(e)) {
                    ite.remove();
                    return a;
                }
            }
        }
        return null;
    }

    private void undo(ReconfigurationPlan red, Action a, VM e, ReconfigurationPlan full) {
        Mapping fullMap = full.getOrigin().getMapping();
        Mapping redMap = red.getOrigin().getMapping();
        if (a != null) {
            red.add(a);
        }
        if (fullMap.isReady(e)) {
            redMap.addReadyVM(e);
        } else if (fullMap.isSleeping(e)) {
            redMap.addSleepingVM(e, fullMap.getVMLocation(e));
        } else if (fullMap.isRunning(e)) {
            redMap.addRunningVM(e, fullMap.getVMLocation(e));
        }
    }

    private void undo(ReconfigurationPlan red, Action a, Node e, ReconfigurationPlan full) {
        Mapping fullMap = full.getOrigin().getMapping();
        Mapping redMap = red.getOrigin().getMapping();
        if (a != null) {
            red.add(a);
        }
        if (fullMap.isOnline(e)) {
            redMap.addOnlineNode(e);
        } else {
            redMap.addOfflineNode(e);
        }
    }


    private Action removeMine(Collection<Action> actions, Node e) {
        Iterator<Action> ite = actions.iterator();
        while (ite.hasNext()) {
            Action a = ite.next();
            if (a instanceof NodeEvent) {
                if (((NodeEvent) a).getNode().equals(e)) {
                    ite.remove();
                    return a;
                }
            }
        }
        return null;
    }

    public ReconfigurationPlan reduceNodes(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) throws Exception {
        SatConstraint s = Constraint2BtrPlace.build(cstr, in);
        List<Node> l = new ArrayList<>(p.getOrigin().getMapping().getAllNodes());
        l.removeAll(s.getInvolvedNodes());
        Iterator<Node> ite = l.iterator();

        Model mo = p.getOrigin().clone();
        ReconfigurationPlan red = new DefaultReconfigurationPlan(mo);
        for (Action a : p) {
            red.add(a);
        }
        while (ite.hasNext()) {
            Node n = ite.next();
            ite.remove();
            if (mo.getMapping().remove(n)) {
                Action removedAction = removeMine(red.getActions(), n);
                if (consistent(v, red, cstr, in, d)) {
                    undo(red, removedAction, n, p);
                    if (removedAction != null) {
                        red.add(removedAction);
                    }
                }
            }
        }
        return red;
    }
}