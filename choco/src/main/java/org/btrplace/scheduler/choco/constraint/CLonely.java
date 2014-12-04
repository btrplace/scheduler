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

package org.btrplace.scheduler.choco.constraint;

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Lonely;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.Disjoint;
import org.btrplace.scheduler.choco.extensions.Precedences;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Lonely}.
 *
 * @author Fabien Hermenier
 */
public class CLonely implements ChocoConstraint {

    private Lonely cstr;

    /**
     * Make a new constraint.
     *
     * @param c the lonely constraint to rely on
     */
    public CLonely(Lonely c) {
        this.cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {
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
        //Link the assignment variables with the set
        Solver s = rp.getSolver();
        s.post(new Disjoint(myHosts.toArray(new IntVar[myHosts.size()]),
                otherHosts.toArray(new IntVar[otherHosts.size()]),
                rp.getNodes().length));

        if (cstr.isContinuous()) {
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
                s.post(p);
            }

            //TODO: The following reveals a model problem. Too many constraints!!
            for (VM vm : otherVMs) {
                VMTransition a = rp.getVMAction(vm);
                Precedences p = new Precedences(a.getDSlice().getHoster(),
                        a.getDSlice().getStart(),
                        minePos.toArray(),
                        mineEnds.toArray(new IntVar[mineEnds.size()]));
                s.post(p);
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Set<VM> bad = new HashSet<>();
        Set<Node> hosts = new HashSet<>();
        Collection<VM> vms = cstr.getInvolvedVMs();
        Mapping map = m.getMapping();
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

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Lonely.class;
        }

        @Override
        public CLonely build(Constraint c) {
            return new CLonely((Lonely) c);
        }
    }
}
