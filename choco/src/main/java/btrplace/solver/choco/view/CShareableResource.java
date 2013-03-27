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

package btrplace.solver.choco.view;

import btrplace.model.Model;
import btrplace.model.ModelView;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.actionModel.KeepRunningVMModel;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Specify, for a given resource, the physical resource usage associated to each server,
 * and the virtual resource usage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class CShareableResource implements ChocoModelView {

    private ShareableResource rc;

    private IntDomainVar[] phyRcUsage;

    private IntDomainVar[] virtRcUsage;

    private IntDomainVar[] vmAllocation;

    private ReconfigurationProblem rp;

    private String id;

    private Model source;

    /**
     * Make a new mapping.
     *
     * @param rp the problem to rely on
     * @param rc the resource to consider
     */
    public CShareableResource(ReconfigurationProblem rp, ShareableResource rc) {
        this.rc = rc;
        this.rp = rp;
        this.source = rp.getSourceModel();
        UUID[] nodes = rp.getNodes();
        phyRcUsage = new IntDomainVar[nodes.length];
        virtRcUsage = new IntDomainVar[nodes.length];

        id = ShareableResource.VIEW_ID_BASE + rc.getResourceIdentifier();
        for (int i = 0; i < nodes.length; i++) {
            phyRcUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("phyRcUsage('" + rc.getResourceIdentifier() + "', '" + rp.getNode(i) + "')"), 0, rc.get(nodes[i]));
            virtRcUsage[i] = rp.getSolver().createBoundIntVar(rp.makeVarLabel("virtRcUsage('" + rc.getResourceIdentifier() + "', '" + rp.getNode(i) + "')"), 0, Choco.MAX_UPPER_BOUND);
        }


        //Bin packing for the node vmAllocation
        CPSolver s = rp.getSolver();
        List<IntDomainVar> notNullUsage = new ArrayList<IntDomainVar>();
        List<IntDomainVar> hosters = new ArrayList<IntDomainVar>();

        vmAllocation = new IntDomainVar[rp.getVMs().length];
        for (int i = 0; i < vmAllocation.length; i++) {
            UUID vmId = rp.getVM(i);
            VMActionModel a = rp.getVMAction(vmId);
            Slice slice = a.getDSlice();
            if (slice == null) { //The VMs will not be running, so its consumption is set to 0
                vmAllocation[i] = s.makeConstantIntVar(rp.makeVarLabel("vmAllocation('" + rc.getResourceIdentifier() + "', '" + vmId + "'"), 0);
            } else {
                //We don't know about the next VM usage for the moment, -1 is used by default to allow to detect an
                //non-updated value.
                vmAllocation[i] = s.createBoundIntVar("vmAllocation('" + rc.getResourceIdentifier() + "', '" + vmId + "')", -1, Choco.MAX_UPPER_BOUND);
                notNullUsage.add(vmAllocation[i]);
                hosters.add(slice.getHoster());
            }

        }
        //We create a BP with only the VMs requiring a not null amount of resources
        //s.post(new LightBinPacking(s.getEnvironment(), virtRcUsage, notNullUsage.toArray(new IntDomainVar[notNullUsage.size()]), hosters.toArray(new IntDomainVar[hosters.size()])));
        //s.post(new BinPacking(s.getEnvironment(), virtRcUsage, notNullUsage.toArray(new IntDomainVar[notNullUsage.size()]), hosters.toArray(new IntDomainVar[hosters.size()])));
        rp.getBinPackingBuilder().add(getResourceIdentifier(), virtRcUsage, notNullUsage.toArray(new IntDomainVar[notNullUsage.size()]), hosters.toArray(new IntDomainVar[hosters.size()]));

    }

    /**
     * Get the resource identifier.
     *
     * @return an identifier
     */
    public String getResourceIdentifier() {
        return rc.getResourceIdentifier();
    }

    /**
     * Get the original resource node physical capacity and VM consumption.
     *
     * @return an {@link ShareableResource}
     */
    public ShareableResource getSourceResource() {
        return rc;
    }

    /**
     * Get the physical resource usage of each node.
     *
     * @return an array of variable denoting the resource usage for each node.
     */
    public IntDomainVar[] getPhysicalUsage() {
        return phyRcUsage;
    }

    /**
     * Get the physical resource usage of a given node
     *
     * @param nIdx the node identifier
     * @return the variable denoting the resource usage for the node. {@code null} if the node is unknown
     */
    public IntDomainVar getPhysicalUsage(int nIdx) {
        if (nIdx >= 0 && nIdx < rp.getNodes().length) {
            return phyRcUsage[nIdx];
        }
        return null;
    }

    /**
     * Get the virtual resource usage  that is made by the VMs on the nodes.
     * <b>Warning: the only possible approach to restrict these value is to increase their
     * upper bound using the associated {@code setSup()} method</b>
     *
     * @return an array of variables denoting each node virtual resource usage.
     */
    public IntDomainVar[] getVirtualUsage() {
        return virtRcUsage;
    }

    /**
     * Get the virtual resource usage of a given node.
     * <b>Warning: the only possible approach to restrict the value is to increase their
     * upper bound using the associated {@code setSup()} method</b>
     *
     * @param nIdx the node identifier
     * @return the variable denoting the resource usage for the node. {@code null} if the node is unknown
     */
    public IntDomainVar getVirtualUsage(int nIdx) {
        if (nIdx >= 0 && nIdx < rp.getNodes().length) {
            return virtRcUsage[nIdx];
        }
        return null;
    }

    /**
     * Get the amount of virtual resource to allocate to each VM.
     * <b>Warning: the only possible approach to restrict these value is to increase their
     * lower bound using the associated {@code setInf()} method</b>
     *
     * @return an array of variables denoting each VM vmAllocation
     */
    public IntDomainVar[] getVMsAllocation() {
        return vmAllocation;
    }

    /**
     * Get the amount of virtual resource to allocate a given VM.
     * <b>Warning: the only possible approach to restrict this value is to increase their
     * lower bound using the associated {@code setInf()} method</b>
     *
     * @param vmIdx the VM identifier
     * @return the variable denoting the virtual resources to allocate to the VM. {@code null} if the VM is unknown
     */
    public IntDomainVar getVMsAllocation(int vmIdx) {
        if (vmIdx >= 0 && vmIdx < rp.getVMs().length) {
            return vmAllocation[vmIdx];
        }
        return null;
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

        int use = vmAllocation[rp.getVM(e)].getInf();
        if (rc.get(e) != use) {
            //The allocation has changed
            Allocate a = new Allocate(e, node, rc.getIdentifier(), use, st, ed);
            return plan.add(a);
        }
        return false;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoModelViewBuilder {
        @Override
        public Class<? extends ModelView> getKey() {
            return ShareableResource.class;
        }

        @Override
        public ChocoModelView build(ReconfigurationProblem rp, ModelView v) throws SolverException {
            ShareableResource rc = (ShareableResource) v;
            return new CShareableResource(rp, rc);
        }
    }

    /**
     * Set the resource usage for each of the VM.
     * If the LB is < 0 , the previous consumption is used to maintain the resource usage.
     * Otherwise, the usage is set to the variable lower bound.
     *
     * @return false if an operation leads to a problem without solution
     */
    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) {
        for (UUID vm : source.getMapping().getAllVMs()) {
            int vmId = rp.getVM(vm);
            IntDomainVar v = vmAllocation[vmId];
            if (v.getInf() < 0) {
                int prevUsage = rc.get(vm);
                try {
                    v.setInf(prevUsage);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to set the minimal '{}' usage for '{}' to its current usage ({})",
                            rc.getResourceIdentifier(), vm, prevUsage);
                    return false;
                }
            } else {
                try {
                    v.setVal(v.getInf());
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to set the VM '{}' consumption to '{}'", rc.getResourceIdentifier(), v.getInf());
                    return false;
                }
            }
        }

        CPSolver s = rp.getSolver();

        //Symetry breaking for VMs that are running and that will stay running
        for (UUID vm : rp.getFutureRunningVMs()) {
            VMActionModel a = rp.getVMAction(vm);
            Slice dSlice = a.getDSlice();
            Slice cSlice = a.getCSlice();
            if (dSlice != null && cSlice != null) {
                IntDomainVar stay = ((KeepRunningVMModel) a).isStaying();

                if (getSourceResource().get(vm) <= getVMsAllocation(rp.getVM(vm)).getInf()) {
                    //If the resource usage will be increasing
                    //Then the duration of the dSlice can be set to 0
                    //(the allocation will be performed at the end of the reconfiguration process)
                    if (stay.isInstantiatedTo(1)) {
                        try {
                            dSlice.getDuration().setVal(0);
                        } catch (ContradictionException ex) {
                            rp.getLogger().info("Unable to set the dSlice duration of {} to 0", dSlice.getSubject());
                            return false;
                        }
                    } else {
                        s.post(new FastImpliesEq(stay, dSlice.getDuration(), 0));
                    }

                } else {
                    //Else, the resource usage is decreasing, so
                    // we set the cSlice duration to 0 to directly reduce the resource allocation
                    if (stay.isInstantiatedTo(1)) {
                        try {
                            cSlice.getDuration().setVal(0);
                        } catch (ContradictionException ex) {
                            rp.getLogger().info("Unable to set the cSlice duration of {} to 0", cSlice.getSubject());
                            return false;
                        }
                    } else {
                        s.post(new FastImpliesEq(stay, cSlice.getDuration(), 0));
                    }
                }
            }
        }


        return true;
    }
}
