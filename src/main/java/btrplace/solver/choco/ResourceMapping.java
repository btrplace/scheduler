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

package btrplace.solver.choco;

import btrplace.model.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.solver.choco.chocoUtil.BinPacking;
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Specify, for a given resource, the rawNodeUsage associated to each server,
 * and the vmUsage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class ResourceMapping {

    private ShareableResource rc;

    private IntDomainVar[] rawNodeUsage;

    private IntDomainVar[] realNodeUsage;

    private IntDomainVar[] vmUsage;

    private ReconfigurationProblem rp;

    /**
     * Make a new mapping.
     *
     * @param rp the problem to rely on
     * @param rc the resource to consider
     */
    public ResourceMapping(ReconfigurationProblem rp, ShareableResource rc) {
        this.rc = rc;
        this.rp = rp;
        UUID[] nodes = rp.getNodes();
        rawNodeUsage = new IntDomainVar[nodes.length];
        realNodeUsage = new IntDomainVar[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            rawNodeUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("rawNodeUsage('" + rc.getIdentifier() + "', '" + rp.getNode(i) + "')"), 0, rc.get(nodes[i]));
            realNodeUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("realNodeUsage('" + rc.getIdentifier() + "', '" + rp.getNode(i) + "')"), 0, Choco.MAX_UPPER_BOUND);
        }


        //Bin packing for the node vmUsage
        CPSolver s = rp.getSolver();
        List<IntDomainVar> notNullUsage = new ArrayList<IntDomainVar>();
        List<IntDomainVar> hosters = new ArrayList<IntDomainVar>();

        vmUsage = new IntDomainVar[rp.getVMs().length];
        for (int i = 0; i < vmUsage.length; i++) {
            UUID vmId = rp.getVM(i);
            VMActionModel a = rp.getVMActions()[i];
            Slice slice = a.getDSlice();
            if (slice == null) { //The VMs will not be running, so its consumption is set to 0
                vmUsage[i] = s.makeConstantIntVar(rp.makeVarLabel("vmUsage('" + rc.getIdentifier() + "', '" + vmId + "'"), 0);
            } else {
                //We don't know about the next VM usage for the moment, -1 is used by default to allow to detect an
                //non-updated value.
                vmUsage[i] = s.createBoundIntVar("vmUsage('" + rc.getIdentifier() + "', '" + vmId + "')", -1, Choco.MAX_UPPER_BOUND);
                notNullUsage.add(vmUsage[i]);
                hosters.add(slice.getHoster());
            }

        }
        //We create a BP with only the VMs requiring a not null amount of resources
        s.post(new BinPacking(s.getEnvironment(), realNodeUsage, notNullUsage.toArray(new IntDomainVar[notNullUsage.size()]), hosters.toArray(new IntDomainVar[hosters.size()])));

    }

    /**
     * Get the resource identifier.
     *
     * @return an identifier
     */
    public String getIdentifier() {
        return rc.getIdentifier();
    }

    /**
     * Get the original resource vmUsage and consumption.
     *
     * @return an {@link ShareableResource}
     */
    public ShareableResource getSourceResource() {
        return rc;
    }

    /**
     * Get the nodes raw vmUsage according to the original resource.
     *
     * @return an array of variable denoting each node raw vmUsage.
     */
    public IntDomainVar[] getRawNodeUsage() {
        return rawNodeUsage;
    }

    /**
     * Get the nodes real vmUsage that is made by the VMs for the resource.
     *
     * @return an array of variables denoting each node real vmUsage.
     */
    public IntDomainVar[] getRealNodeUsage() {
        return realNodeUsage;
    }

    /**
     * Get the VMs consumption of the resource according to the original resource.
     * <b>Warning: the only possible approach to restrict these value is to increase their
     * lower bound using the associated {@code setInf()} method</b>
     *
     * @return an array of variables denoting each VM vmUsage
     */
    public IntDomainVar[] getVMConsumption() {
        return vmUsage;
    }

    /**
     * Generate and add an {@link btrplace.plan.event.Allocate} action if the amount of
     * resources allocated to a VM has changed.
     * The action schedule must be known.
     *
     * @param e    the VM identifier
     * @param node the identifier of the node that is currently hosting the VM
     * @param st   the moment that action starts
     * @param ed   the moment the action ends
     * @return {@code true} if the action has been added to the plan,{@code false} otherwise
     */
    public boolean addAllocateAction(ReconfigurationPlan plan, UUID e, UUID node, int st, int ed) {

        int use = vmUsage[rp.getVM(e)].getInf();
        if (rc.get(e) != use) {
            //The allocation has changed
            Allocate a = new Allocate(e, node, rc.getIdentifier(), use, st, ed);
            return plan.add(a);
        }
        return false;
    }

}
