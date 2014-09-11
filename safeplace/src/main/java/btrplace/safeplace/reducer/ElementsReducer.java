/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.safeplace.reducer;

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
import btrplace.safeplace.CTestCase;
import btrplace.safeplace.CTestCaseResult;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.verification.Verifier;
import btrplace.safeplace.verification.btrplace.Constraint2BtrPlace;
import btrplace.safeplace.verification.spec.SpecVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Remove supposed useless VMs or nodes.
 * <p>
 * This try to remove one by one VMs or nodes that are not involved
 * in the constraint. The actions that manipulate these elements are removed too.
 * An element is maintained in the plan if its removal changes the error
 *
 * @author Fabien Hermenier
 */
public class ElementsReducer extends Reducer {

    @Override
    public CTestCase reduce(CTestCase tc, SpecVerifier v1, Verifier v2, CTestCaseResult.Result errType) throws Exception {
        //System.err.println("Reduce: \n" + tc.getPlan().getOrigin().getMapping() + "\n" + tc.getPlan());
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getParameters();

        ReconfigurationPlan r1 = reduceVMs(v1, v2, p, cstr, in, tc.continuous(), errType);
        ReconfigurationPlan res = reduceNodes(v1, v2, r1, cstr, in, tc.continuous(), errType);
        //TestCase r = new TestCase(v, cstr, res, in, tc.isDiscrete());
        if (consistent(v1, v2, derive(tc, in, res), errType)) {
            System.err.println("BUG while reducing element(s):");
            System.err.println(tc);
            System.err.println("Now: " + derive(tc, in, res));
            System.err.println(tc.getPlan().equals(res));
            System.exit(1);
        }
        //System.out.println("Reduced from " + p.getOrigin().getMapping().getNbVMs() + " VMs x " + p.getOrigin().getMapping().getNbNodes() + " nodes  to " + res.getOrigin().getMapping().getNbVMs() + " x " + p.getOrigin().getMapping().getNbNodes());
        return derive(tc, in, res);
    }

    public ReconfigurationPlan reduceVMs(SpecVerifier v1, Verifier v2, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean c, CTestCaseResult.Result errType) throws Exception {
        List<VM> l;
        if (cstr.isCore()) {
            l = new ArrayList<>(p.getOrigin().getMapping().getAllVMs());
        } else {
            SatConstraint s = Constraint2BtrPlace.build(cstr, in);
            l = new ArrayList<>(p.getOrigin().getMapping().getAllVMs());
            l.removeAll(s.getInvolvedVMs());
        }
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
            if (consistent(v1, v2, cstr, in, red, c, errType)) {
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

    public ReconfigurationPlan reduceNodes(SpecVerifier v1, Verifier v2, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean c, CTestCaseResult.Result errType) throws Exception {
        List<Node> l;
        if (cstr.isCore()) {
            l = new ArrayList<>(p.getOrigin().getMapping().getAllNodes());
        } else {
            SatConstraint s = Constraint2BtrPlace.build(cstr, in);
            l = new ArrayList<>(p.getOrigin().getMapping().getAllNodes());
            l.removeAll(s.getInvolvedNodes());
        }
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
                if (consistent(v1, v2, cstr, in, red, c, errType)) {
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