/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Lonely;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.Disjoint;
import org.btrplace.scheduler.choco.extensions.Precedences;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Lonely}.
 *
 * @author Fabien Hermenier
 */
public class CLonely implements ChocoConstraint {

  private final Lonely cstr;

    /**
     * Make a new constraint.
     *
     * @param c the lonely constraint to rely on
     */
    public CLonely(Lonely c) {
        this.cstr = c;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) {
        //Remove non future-running VMs
        List<IntVar> myHosts = new ArrayList<>();
        List<IntVar> otherHosts = new ArrayList<>();
        Collection<VM> vms = new HashSet<>();
        Set<VM> otherVMs = new HashSet<>();
        for (VM vm : rp.getFutureRunningVMs()) {
            IntVar host = rp.getVMAction(vm).getDSlice().getHoster();
            if (cstr.getInvolvedVMs().contains(vm)) {
                myHosts.add(host);
                vms.add(vm);
            } else {
                otherHosts.add(host);
                otherVMs.add(vm);
            }
        }
        if (myHosts.isEmpty() || otherHosts.isEmpty()) {
            // There is no VMs to isolate.
            return true;
        }

        //Link the assignment variables with the set
        Model s = rp.getModel();
        s.post(new Disjoint(
                new IntVar[][]{myHosts.toArray(new IntVar[0]), otherHosts.toArray(new IntVar[0])},
                rp.getNodes().size()));

        if (cstr.isContinuous()) {
            continuousRestriction(rp, vms, otherVMs);
        }
        return true;
    }

    private static void continuousRestriction(ReconfigurationProblem rp, Collection<VM> vms, Set<VM> otherVMs) {
        //Get the position of all the others c-slices and their associated end moment
        TIntArrayList otherPos = new TIntArrayList();
        TIntArrayList minePos = new TIntArrayList();
        List<IntVar> otherEnds = new ArrayList<>();
        List<IntVar> mineEnds = new ArrayList<>();
        Mapping map = rp.getSourceModel().getMapping();
        for (Node n : map.getOnlineNodes()) {
            for (VM vm : map.getRunningVMs(n)) {
                if (!vms.contains(vm)) {
                    otherPos.add(rp.getNode(map.getVMLocation(vm)));
                    VMTransition a = rp.getVMAction(vm);
                    otherEnds.add(a.getCSlice().getEnd());
                } else {
                    minePos.add(rp.getNode(map.getVMLocation(vm)));
                    VMTransition a = rp.getVMAction(vm);
                    mineEnds.add(a.getCSlice().getEnd());
                }
            }
        }
        for (VM vm : vms) {
            VMTransition a = rp.getVMAction(vm);
            Precedences p = new Precedences(a.getDSlice().getHoster(),
                    a.getDSlice().getStart(),
                    otherPos.toArray(),
                    otherEnds.toArray(new IntVar[otherEnds.size()]));
            rp.getModel().post(p);
        }

        //TODO: The following reveals a model problem. Too many constraints!!
        for (VM vm : otherVMs) {
            VMTransition a = rp.getVMAction(vm);
            Precedences p = new Precedences(a.getDSlice().getHoster(),
                    a.getDSlice().getStart(),
                    minePos.toArray(),
                    mineEnds.toArray(new IntVar[mineEnds.size()]));
            rp.getModel().post(p);
        }
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Set<VM> bad = new HashSet<>();
        Set<Node> hosts = new HashSet<>();
        Collection<VM> vms = cstr.getInvolvedVMs();
        Mapping map = i.getModel().getMapping();
        for (VM vm : vms) {
            if (map.isRunning(vm)) {
                hosts.add(map.getVMLocation(vm));
            }
        }
        for (Node n : hosts) {
            //Every used node that host a VMs that is not a part of the constraint
            //is a bad node. All the hosted VMs are candidate for relocation. Not optimal, but safe
            for (VM vm : map.getRunningVMs(n)) {
                if (!vms.contains(vm)) {
                    bad.addAll(map.getRunningVMs(n));
                    break;
                }
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
