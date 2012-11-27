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

import btrplace.model.StackableResource;
import btrplace.solver.choco.chocoUtil.BinPacking;
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.UUID;

/**
 * Specify, for a given resource, the rawNodeUsage associated to each server,
 * and the vmUsage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class ResourceMapping {

    private StackableResource rc;

    private IntDomainVar[] rawNodeUsage;

    private IntDomainVar[] realNodeUsage;

    private IntDomainVar[] vmUsage;

    /**
     * Make a new mappring.
     *
     * @param rp the problem to rely on
     * @param rc the resource to consider
     */
    public ResourceMapping(ReconfigurationProblem rp, StackableResource rc) {
        this.rc = rc;

        UUID[] nodes = rp.getNodes();
        rawNodeUsage = new IntDomainVar[nodes.length];
        realNodeUsage = new IntDomainVar[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            rawNodeUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("rawNodeUsage('" + rc.getIdentifier() + "', '" + rp.getNode(i) + "')"), 0, rc.get(nodes[i]));
            realNodeUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("realNodeUsage('" + rc.getIdentifier() + "', '" + rp.getNode(i) + "')"), 0, Choco.MAX_UPPER_BOUND);
        }


        //Bin packing for the node vmUsage
        CPSolver s = rp.getSolver();
        List<Slice> dSlices = ActionModelUtil.getDSlices(rp.getVMActions(rp.getFutureRunningVMs()));
        IntDomainVar[] ds = SliceUtils.extractHosters(dSlices);
        vmUsage = new IntDomainVar[ds.length];
        for (int i = 0; i < ds.length; i++) {
            UUID vmId = dSlices.get(i).getSubject();
            vmUsage[i] = s.createBoundIntVar("vmUsage('" + rc.getIdentifier() + "', '" + vmId + "')", rc.get(vmId), Choco.MAX_UPPER_BOUND);
        }
        s.post(new BinPacking(s.getEnvironment(), realNodeUsage, vmUsage, ds));

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
     * @return an {@link StackableResource}
     */
    public StackableResource getSourceResource() {
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


}
