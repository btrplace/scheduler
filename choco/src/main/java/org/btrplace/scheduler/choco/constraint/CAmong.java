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
import org.btrplace.model.constraint.Fence;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class CAmong implements ChocoConstraint {

    private Among cstr;

    private IntVar vmGrpId;

    /**
     * The label of the variable denoting the group of VMs.
     */
    public static final String GROUP_LABEL = "among#grp";

    /**
     * Make a new constraint.
     *
     * @param a the constraint to rely on
     */
    public CAmong(Among a) {
        cstr = a;
    }

    /**
     * Get the group variable that indicate on which group the VMs are running.
     *
     * @return a variable that may be instantiated but {@code null} until
     * {@link #inject(org.btrplace.scheduler.choco.ReconfigurationProblem)} has been called
     */
    public IntVar getGroupVariable() {
        return vmGrpId;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {

        int nextGrp = -1;
        int curGrp = -1;

        List<Collection<Node>> groups = new ArrayList<>();
        groups.addAll(cstr.getGroupsOfNodes());

        Set<VM> running = new HashSet<>();
        Mapping src = rp.getSourceModel().getMapping();
        for (VM vm : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                //The VM will be running
                running.add(vm);
                IntVar vAssign = rp.getVMAction(vm).getDSlice().getHoster();
                //If one of the VM is already placed, no need for the constraint, the group will be known
                if (vAssign.isInstantiated()) {
                    //Get the group of nodes that match the selected node
                    int g = getGroup(rp.getNode(vAssign.getValue()), groups);
                    if (errorReported(rp, vm, nextGrp, g)) {
                        return false;
                    }
                    nextGrp = g;
                }
            }

            if (cstr.isContinuous() && src.isRunning(vm)) {
                //The VM is already running, so we get its current group
                Node curNode = src.getVMLocation(vm);
                int g = getGroup(curNode, groups);
                if (errorReported(rp, vm, curGrp, g)) {
                    return false;
                }
                curGrp = g;
            }
        }
        if (cstr.isContinuous() && curGrp != -1) {
            return restrictGroup(rp, running, groups, curGrp);
        } else if (groups.size() == 1) {
            return restrictGroup(rp, running, groups, 0);
        }
        return restrictGroup(rp, running, groups, nextGrp);
    }

    private boolean errorReported(ReconfigurationProblem rp, VM vm, int futureGroup, int g) {
        if (futureGroup == -1) {
            //It is not possible to state
            return false;
        }
        if (g == -1) {
            rp.getLogger().error("The VM in '{}' will be placed out of any of the allowed group", vm);
            return true;
        } else if (futureGroup != g) {
            rp.getLogger().error("The VMs in '{}' cannot be spread over multiple group of nodes", cstr.getInvolvedVMs());
            return true;
        }
        return false;
    }

    private boolean restrictGroup(ReconfigurationProblem rp, Set<VM> running, List<Collection<Node>> groups, int selected) {
        if (selected == -1) {
            //Now, we create a variable to indicate on which group of nodes the VMs will be
            vmGrpId = VariableFactory.enumerated(rp.makeVarLabel(GROUP_LABEL), 0, groups.size() - 1, rp.getSolver());
            //grp: A table to indicate the group each node belong to, -1 for no group
            int[] grp = new int[rp.getNodes().length];
            for (int i = 0; i < grp.length; i++) {
                Node n = rp.getNode(i);
                int idx = getGroup(n, groups);
                if (idx >= 0) {
                    grp[i] = idx;
                } else {
                    grp[i] = -1;
                }
            }
            //We link the VM placement variable with the group variable
            for (VM vm : running) {
                IntVar assign = rp.getVMAction(vm).getDSlice().getHoster();
                Constraint c = IntConstraintFactory.element(vmGrpId, grp, assign, 0, "detect");
                rp.getSolver().post(c);
            }
        } else {
            //As the group is already known, it's now just a fence constraint
            vmGrpId = VariableFactory.fixed(rp.makeVarLabel(GROUP_LABEL), selected, rp.getSolver());
            if (!fence(rp, running, groups.get(selected))) {
                return false;
            }
        }
        return true;
    }

    private boolean fence(ReconfigurationProblem rp, Collection<VM> vms, Collection<Node> group) {
        for (VM v : vms) {
            if (!new CFence(new Fence(v, group)).inject(rp)) {
                return false;
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
    public int getGroup(Node n, List<Collection<Node>> grps) {
        int i = 0;
        for (Collection<Node> pGrp : grps) {
            if (pGrp.contains(n)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        if (!cstr.isSatisfied(m)) {
            return new HashSet<>(cstr.getInvolvedVMs());
        }
        return Collections.emptySet();
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
        public Class<? extends org.btrplace.model.constraint.Constraint> getKey() {
            return Among.class;
        }

        @Override
        public CAmong build(org.btrplace.model.constraint.Constraint c) {
            return new CAmong((Among) c);
        }
    }
}
