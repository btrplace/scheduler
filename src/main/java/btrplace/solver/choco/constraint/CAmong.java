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
import btrplace.model.constraint.Among;
import btrplace.model.constraint.Fence;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.MyElement;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Choco implementation of the {@link btrplace.model.constraint.Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class CAmong implements ChocoSatConstraint {

    private Among cstr;

    /**
     * Make a new constraint.
     *
     * @param a the constraint to rely on
     */
    public CAmong(Among a) {
        cstr = a;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {

        List<Set<UUID>> groups = new ArrayList<Set<UUID>>();
        groups.addAll(cstr.getGroupsOfNodes());
        Collection<UUID> vms = cstr.getInvolvedVMs();
        if (groups.size() == 1 && !groups.iterator().next().equals(rp.getSourceModel().getMapping().getAllNodes())) {
            //Only 1 group of nodes, it's just a fence constraint
            new CFence(new Fence(new HashSet<UUID>(vms), groups.get(0))).inject(rp);
        } else {
            //Get only the future running VMs
            Set<UUID> runnings = new HashSet<UUID>();
            for (UUID vm : vms) {
                if (rp.getFutureRunningVMs().contains(vm)) {
                    runnings.add(vm);
                }
            }

            //Now, we create a variable to indicate on which group of nodes the VMs will be
            IntDomainVar vmGrpId = rp.getSolver().createEnumIntVar(rp.makeVarLabel("among#pGrp"), 0, groups.size() - 1);
            int gIdx = -1;
            //First pass on the group of VMs to check if a VM is already placed
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
                            rp.getLogger().error("The VMs in '{}' cannot spread over multiple group of nodes", vms);
                            return false;
                        }
                    }
                }
            }
            if (gIdx == -1) {
                //grp: A table to indicate the group each node belong to, -1 for no group
                int[] grps = new int[rp.getNodes().length];
                Set<UUID> possibleNodes = new HashSet<UUID>();
                for (int i = 0; i < grps.length; i++) {
                    UUID n = rp.getNodes()[i];
                    int idx = getGroup(n);
                    if (idx >= 0) {
                        grps[i] = idx;
                        possibleNodes.add(n);
                    }
                }
                //In any case, the VMs cannot go to nodes that are in no groups
                new CFence(new Fence(runnings, new HashSet<UUID>(possibleNodes))).inject(rp);
                //We link the VM placement variable with the group variable
                for (UUID vm : runnings) {
                    IntDomainVar assign = rp.getVMAction(vm).getDSlice().getHoster();
                    SConstraint c = new MyElement(assign, grps, vmGrpId, 0, MyElement.Sort.detect);
                    rp.getSolver().post(c);
                }
            } else {
                //As the group is already known, it's now just a fence constraint
                new CFence(new Fence(runnings, groups.get(vmGrpId.getVal()))).inject(rp);
            }
        }
        return true;
    }

    /**
     * Get the group the node belong to.
     *
     * @param n the node
     * @return the group identifier, {@code -1} if the node does not belong to a group
     */
    private int getGroup(UUID n) {
        int i = 0;
        for (Set<UUID> pGrp : cstr.getGroupsOfNodes()) {
            if (pGrp.contains(n)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        if (!cstr.isSatisfied(m).equals(SatConstraint.Sat.SATISFIED)) {
            return new HashSet<UUID>(cstr.getInvolvedVMs());
        }
        return Collections.emptySet();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Among.class;
        }

        @Override
        public CAmong build(SatConstraint cstr) {
            return new CAmong((Among) cstr);
        }
    }
}
