package btrplace.solver.api.cstrSpec.reducer;

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
import java.util.List;
import java.util.ListIterator;

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


    private boolean consistent(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) throws Exception {
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

        //System.err.println(tc.pretty(true));
        ReconfigurationPlan r1 = reduceVMs(v, p, cstr, in, tc.isDiscrete());
        ReconfigurationPlan res = reduceNodes(v, r1, cstr, in, tc.isDiscrete());
        TestCase r = new TestCase(v, cstr, res, in, tc.isDiscrete());
        if (r.succeed()) {
            System.err.println("BUG");
            System.err.println(r.pretty(true));
            System.exit(1);
        }
        return r;
    }

    public ReconfigurationPlan reduceVMs(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) throws Exception {
        if (consistent(v, p, cstr, in, d)) {
            return p;
        }
        SatConstraint s = Constraint2BtrPlace.build(cstr, in);
        List<VM> l = new ArrayList<>(p.getOrigin().getMapping().getAllVMs());
        l.removeAll(s.getInvolvedVMs());
        ListIterator<VM> ite = l.listIterator();

        Model mo = p.getOrigin().clone();
        ReconfigurationPlan red = p;
        ReconfigurationPlan prev = red;
        while (ite.hasNext()) {
            red = new DefaultReconfigurationPlan(mo);
            VM vm = ite.next();
            ite.remove();
            int state;
            Node h = null;
            if (mo.getMapping().isReady(vm)) {
                state = 0;
            } else if (mo.getMapping().isRunning(vm)) {
                state = 1;
            } else {
                state = 2;
            }
            if (state != 0) {
                h = mo.getMapping().getVMLocation(vm);
            }

            if (!mo.getMapping().remove(vm)) {
                System.err.println("BUGGY");
            }
            Action removedAction = null;
            for (Action a : prev) {
                if (a instanceof VMEvent) {
                    if (mo.getMapping().contains(((VMEvent) a).getVM())) {
                        red.add(a);
                    } else {
                        removedAction = a;
                    }
                } else {
                    red.add(a);
                }
            }
            if (consistent(v, red, cstr, in, d)) {
                //System.err.println("Removing " + vm + " make the problem consistent;");
                //The VM must be present, so put it back
                if (state == 0) {
                    mo.getMapping().addReadyVM(vm);
                } else if (state == 1) {
                    mo.getMapping().addRunningVM(vm, h);
                } else {
                    mo.getMapping().addSleepingVM(vm, h);
                }
                ite.add(vm);
                //Add back the removed action
                if (removedAction != null) {
                    red.add(removedAction);
                }
            }
            //System.err.println("After pass:\n" + new TestCase(v, cstr, red, in , d).pretty(true));
            prev = red;
        }
        //System.err.println("\n\n");
        return red;
    }

    public ReconfigurationPlan reduceNodes(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) throws Exception {

        if (consistent(v, p, cstr, in, d)) {
            return p;
        }
        SatConstraint s = Constraint2BtrPlace.build(cstr, in);
        List<Node> l = new ArrayList<>(p.getOrigin().getMapping().getAllNodes());
        l.removeAll(s.getInvolvedNodes());
        ListIterator<Node> ite = l.listIterator();

        Model mo = p.getOrigin().clone();
        ReconfigurationPlan red = p;
        ReconfigurationPlan prev = red;
        //System.err.println("---");
        //System.err.println(new TestCase(v, cstr, red, in, d).pretty(true));
        while (ite.hasNext()) {
            red = new DefaultReconfigurationPlan(mo);
            Node n = ite.next();
            ite.remove();
            //System.err.println("Remove " + n);
            boolean on = mo.getMapping().isOnline(n);
            Action removedAction = null;
            if (mo.getMapping().remove(n)) {
                //System.err.println("Out of the model");
                for (Action a : prev) {
                    if (a instanceof NodeEvent) {
                        if (mo.getMapping().contains(((NodeEvent) a).getNode())) {
                            red.add(a);
                        } else {
                            //System.err.println("Out of the plan");
                            removedAction = a;
                        }
                    } else {
                        red.add(a);
                    }
                }

                if (consistent(v, red, cstr, in, d)) {
                    //System.err.println("Fail, make the tc consistent");
                        //The node must be present, put it back
                        if (on) {
                            mo.getMapping().addOnlineNode(n);
                        } else {
                            mo.getMapping().addOfflineNode(n);
                        }
                        ite.add(n);
                    if (removedAction != null) {
                        red.add(removedAction);
                    }
                }
            } else {
                //System.err.println("no way");
                ite.add(n);
                for (Action a : prev) {
                    //System.err.println("Re-insert " + a);
                    red.add(a);
                }
            }
            //System.err.println("After pass:\n" + new TestCase(v, cstr, red, in , d).pretty(true));
            prev = red;
        }
        return red;
    }
}