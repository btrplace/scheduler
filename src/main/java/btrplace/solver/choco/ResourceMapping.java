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

import btrplace.model.IntResource;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Specify, for a given resource, the capacity associated to each server,
 * and the usage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class ResourceMapping {

    private IntResource rc;

    private IntDomainVar[] capacity;

    private int[] usage;

    /**
     * Make a new mappring.
     *
     * @param rp the problem to rely on
     * @param rc the resource to consider
     */
    public ResourceMapping(ReconfigurationProblem rp, IntResource rc) {
        this.rc = rc;

        UUID[] nodes = rp.getNodes();
        UUID[] vms = rp.getVMs();
        capacity = new IntDomainVar[nodes.length];
        usage = new int[vms.length];
        for (int i = 0; i < nodes.length; i++) {
            capacity[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("capacity('" + rc.identifier() + "', '" + rp.getNode(i) + "')"), 0, rc.get(nodes[i]));
        }

        for (int i = 0; i < vms.length; i++) {
            usage[i] = rc.get(vms[i]);
        }
    }

    /**
     * Get the resource identifier.
     *
     * @return an identifier
     */
    public String getIdentifier() {
        return rc.identifier();
    }

    /**
     * Get the original resource usage and consumption.
     *
     * @return an {@link IntResource}
     */
    public IntResource getSourceResource() {
        return rc;
    }

    /**
     * Get the nodes capacity according to the original resource.
     *
     * @return an array of variable denoting each node capacity
     */
    public IntDomainVar[] getCapacities() {
        return capacity;
    }

    /**
     * Get the VMs usage of the resource according to the original resource.
     *
     * @return an array of integer denoting each VM usage
     */
    public int[] getUsage() {
        return usage;
    }


}
