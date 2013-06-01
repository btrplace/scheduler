/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.SplitAmong;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of the {@link btrplace.model.constraint.SplitAmong} constraint.
 *
 * @author Fabien Hermenier
 */
public class CSplitAmong implements ChocoSatConstraint {

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
    public boolean inject(ReconfigurationProblem rp) throws SolverException {

        if (cstr.isContinuous() && !cstr.isSatisfied(rp.getSourceModel())) {
            rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
            return false;
        }

        Collection<Collection<VM>> vGrps = cstr.getGroupsOfVMs();
        Collection<Collection<Node>> pGrps = cstr.getGroupsOfNodes();
        CPSolver s = rp.getSolver();

        IntDomainVar[] grpVars = new IntDomainVar[vGrps.size()];
        //VM is assigned on a node <-> group variable associated to the VM
        //is assigned to the group of nodes it belong too.
        int i = 0;
        for (Collection<VM> vms : vGrps) {

            Among a = new Among(vms, pGrps);
            //If the constraint is continuous, there is no way a group of VMs already binded to a group of
            //nodes can move to another group. It also means the group of VMs will never overlap
            a.setContinuous(cstr.isContinuous());
            CAmong ca = new CAmong(a);
            if (!ca.inject(rp)) {
                return false;
            }

            grpVars[i++] = ca.getGroupVariable();
        }

        //forces all the vGroups to use different group of nodes
        s.post(new AllDifferent(grpVars, s.getEnvironment()));
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
                if (map.getRunningVMs().contains(vm)) {
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
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return SplitAmong.class;
        }

        @Override
        public CSplitAmong build(SatConstraint cstr) {
            return new CSplitAmong((SplitAmong) cstr);
        }
    }
}
