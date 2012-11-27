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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Specify, for a given resource, the rawUsage associated to each server,
 * and the usage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class ResourceMapping {

    private StackableResource rc;

    private IntDomainVar[] rawUsage;

    private IntDomainVar[] realUsage;

    private IntDomainVar[] usage;

    /**
     * Make a new mappring.
     *
     * @param rp the problem to rely on
     * @param rc the resource to consider
     */
    public ResourceMapping(ReconfigurationProblem rp, StackableResource rc) {
        this.rc = rc;

        UUID[] nodes = rp.getNodes();
        UUID[] vms = rp.getVMs();
        rawUsage = new IntDomainVar[nodes.length];
        realUsage = new IntDomainVar[nodes.length];
        usage = new IntDomainVar[vms.length];

        for (int i = 0; i < nodes.length; i++) {
            rawUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("rawUsage('" + rc.getIdentifier() + "', '" + rp.getNode(i) + "')"), 0, rc.get(nodes[i]));
            realUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("realUsage('" + rc.getIdentifier() + "', '" + rp.getNode(i) + "')"), 0, Choco.MAX_UPPER_BOUND);
        }


        //Bin packing for the node usage
        CPSolver s = rp.getSolver();
        SliceRcComparator cmp = new SliceRcComparator(rc, false);
        List<Slice> dSlices = ActionModelUtil.getDSlices(rp.getVMActions(rp.getFutureRunningVMs()));
        Collections.sort(dSlices, cmp);
        IntDomainVar[] ds = SliceUtils.extractHosters(dSlices);
        IntDomainVar[] usages = new IntDomainVar[ds.length];
        for (int i = 0; i < ds.length; i++) {
            UUID vmId = dSlices.get(i).getSubject();
            usages[i] = s.createBoundIntVar("usage('" + rc.getIdentifier() + "', '" + vmId + "')", rc.get(vmId), Choco.MAX_UPPER_BOUND);
        }
        s.post(new BinPacking(s.getEnvironment(), realUsage, usages, ds));

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
     * Get the original resource usage and consumption.
     *
     * @return an {@link StackableResource}
     */
    public StackableResource getSourceResource() {
        return rc;
    }

    /**
     * Get the nodes raw usage according to the original resource.
     *
     * @return an array of variable denoting each node raw usage.
     */
    public IntDomainVar[] getRawUsage() {
        return rawUsage;
    }

    /**
     * Get the nodes real usage that is made by the VMs for the resource.
     *
     * @return an array of variables denoting each node real usage.
     */
    public IntDomainVar[] getRealUsage() {
        return realUsage;
    }

    /**
     * Get the VMs consumption of the resource according to the original resource.
     * <b>Warning: the only possible approach to restrict these value is to increase their
     * lower bound using the associated {@code setInf()} method</b>
     *
     * @return an array of variables denoting each VM usage
     */
    public IntDomainVar[] getConsumption() {
        return usage;
    }


}
