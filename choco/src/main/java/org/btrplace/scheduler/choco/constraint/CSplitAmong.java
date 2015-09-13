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

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.SplitAmong;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of the {@link org.btrplace.model.constraint.SplitAmong} constraint.
 *
 * @author Fabien Hermenier
 */
public class CSplitAmong implements ChocoConstraint {

    private SplitAmong cstr;

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely on
     */
    public CSplitAmong(SplitAmong s) {
        this.cstr = s;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {

        if (cstr.isContinuous() && !cstr.isSatisfied(rp.getSourceModel())) {
            rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
            return false;
        }

        Collection<Collection<VM>> vGroups = cstr.getGroupsOfVMs();
        Collection<Collection<Node>> pGroups = cstr.getGroupsOfNodes();
        Solver s = rp.getSolver();

        IntVar[] grpVars = new IntVar[vGroups.size()];
        //VM is assigned on a node <-> group variable associated to the VM
        //is assigned to the group of nodes it belong too.
        int i = 0;
        for (Collection<VM> vms : vGroups) {

            Among a = new Among(vms, pGroups);
            //If the constraint is continuous, there is no way a group of VMs already bound to a group of
            //nodes can move to another group. It also means the group of VMs will never overlap
            a.setContinuous(cstr.isContinuous());
            CAmong ca = new CAmong(a);
            if (!ca.inject(rp)) {
                return false;
            }

            grpVars[i++] = ca.getGroupVariable();
        }

        //forces all the vGroups to use different group of nodes
        s.post(IntConstraintFactory.alldifferent(grpVars, "DEFAULT"));
        return true;
    }

    /**
     * Get the group the node belong to.
     *
     * @param n the node
     * @return the group identifier, {@code -1} if the node does not belong to a group
     */
    public int getPGroup(Node n) {
        int i = 0;
        for (Collection<Node> pGrp : cstr.getGroupsOfNodes()) {
            if (pGrp.contains(n)) {
                break;
            }
            i++;
        }
        return i;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        //contains the set of VMs hosted on a group id.
        Collection<VM>[] usedGrp = new Set[cstr.getGroupsOfNodes().size()];

        Mapping map = m.getMapping();

        Set<VM> bad = new HashSet<>();
        for (Collection<VM> vms : cstr.getGroupsOfVMs()) {
            int grp = -1;
            for (VM vm : vms) {
                if (map.isRunning(vm)) {
                    Node n = map.getVMLocation(vm);
                    int g = getPGroup(n);
                    if (g == -1) {
                        //The VM is on a node that belong to none of the given groups
                        bad.add(vm);
                    } else if (grp == -1) {
                        grp = g;
                        usedGrp[g] = vms;
                    } else if (g != grp) {
                        //The VMs spread over multiple group of nodes, the group of VMs is mis-placed
                        bad.addAll(vms);
                        if (usedGrp[g] != null) {
                            bad.addAll(usedGrp[g]);
                        }
                        bad.addAll(usedGrp[grp]);
                    }
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
            return SplitAmong.class;
        }

        @Override
        public CSplitAmong build(Constraint c) {
            return new CSplitAmong((SplitAmong) c);
        }
    }
}
