/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.SplitAmong;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of the {@link btrplace.model.constraint.SplitAmong} constraint.
 * <p/>
 * TODO: continuous implementation
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

        Set<Set<UUID>> vGrps = cstr.getGroupsOfVMs();
        Set<Set<UUID>> pGrps = cstr.getGroupsOfNodes();
        CPSolver s = rp.getSolver();

        IntDomainVar[] grpVars = new IntDomainVar[vGrps.size()];
        //VM is assigned on a node <-> group variable associated to the VM
        //is assigned to the group of nodes it belong too.
        int i = 0;
        for (Set<UUID> vms : vGrps) {

            Among a = new Among(vms, pGrps);
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
    public int getGroup(UUID n) {
        int i = 0;
        for (Set<UUID> pGrp : cstr.getGroupsOfNodes()) {
            if (pGrp.contains(n)) {
                break;
            }
            i++;
        }
        return i;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        //contains the set of VMs hosted on a group id.
        Set<UUID>[] usedGrp = new Set[cstr.getGroupsOfNodes().size()];

        Mapping map = m.getMapping();

        Set<UUID> bad = new HashSet<UUID>();
        for (Set<UUID> vms : cstr.getGroupsOfVMs()) {
            int grp = -1;
            for (UUID vm : vms) {
                if (map.getRunningVMs().contains(vm)) {
                    UUID n = map.getVMLocation(vm);
                    int g = getGroup(n);
                    if (g == -1) {
                        bad.add(vm); //The VM is on an disallowed node
                    } else if (grp == -1) {
                        grp = g;
                    } else if (g != grp) {
                        //The VMs spread over multiple group of nodes, the group of VMs is mis-placed
                        bad.addAll(vms);
                    }
                }
            }
            if (grp > 0) {
                if (usedGrp[grp] == null) {
                    usedGrp[grp] = vms;
                } else {
                    //The group of nodes is already used, the VMs on this group plus the current VMs are mis-placed
                    bad.addAll(vms);
                    bad.addAll(usedGrp[grp]);
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
