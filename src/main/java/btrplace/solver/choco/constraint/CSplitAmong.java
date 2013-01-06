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

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.SplitAmong;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.MyElement;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of the {@link btrplace.model.constraint.SplitAmong} constraint.
 * <p/>
 * TODO: continuous implementation
 * TODO: getMisplaced()
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

        IntDomainVar[] gAssigns = new IntDomainVar[vGrps.size()];
        int nbGroups = pGrps.size();

        //One vGroup variable per group of vms, domain = #groups of servers
        for (int i = 0; i < gAssigns.length; i++) {
            gAssigns[i] = s.createEnumIntVar(rp.makeVarLabel("splitAmong#vG" + i), 0, nbGroups - 1);
        }

        //Associate with each of the node its group
        UUID[] allNodes = rp.getNodes();
        int[] pGroupIdx = new int[allNodes.length];
        for (int i = 0; i < allNodes.length; i++) {
            pGroupIdx[i] = getGroup(allNodes[i]);
        }

        //VM is assigned on a node <-> group variable associated to the VM
        //is assigned to the group of nodes it belong too.
        int i = 0;
        for (Set<UUID> vms : vGrps) {

            //First pass on the group of VMs to check if a VM is already placed
            int gIdx = -1;
            for (UUID vm : vms) {
                if (rp.getFutureRunningVMs().contains(vm)) {
                    IntDomainVar vAssign = rp.getVMAction(vm).getDSlice().getHoster();
                    //If one of the VM is already placed, no need for the constraint
                    if (vAssign.isInstantiated()) {
                        //Get the group of nodes that match the selected node
                        int g = getGroup(rp.getNode(vAssign.getVal()));
                        if (gIdx == -1) {
                            gIdx = g;
                        } else if (gIdx != g) {
                            rp.getLogger().error("The VMs in '{}' spread over multiple group of nodes", vms);
                            return false;
                        }
                    }
                }
            }
            if (gIdx == -1) {
                for (UUID vm : vms) {
                    if (rp.getFutureRunningVMs().contains(vm)) {
                        IntDomainVar vAssign = rp.getVMAction(vm).getDSlice().getHoster();
                        s.post(new MyElement(vAssign, pGroupIdx, gAssigns[i]));
                    }
                }
            }
            i++;
        }

        //forces all the vGroups to use different group of nodes
        s.post(new AllDifferent(gAssigns, s.getEnvironment()));
        return true;
    }

    private int getGroup(UUID n) {
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
