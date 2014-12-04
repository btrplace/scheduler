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

package org.btrplace.scheduler.choco.view;

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ModelView;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.RoundedUpDivision;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

/**
 * Specify, for a given resource, the physical resource usage associated to each server,
 * and the virtual resource usage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class CShareableResource implements ChocoView {

    private ShareableResource rc;

    private IntVar[] phyRcUsage;

    private IntVar[] virtRcUsage;

    private IntVar[] vmAllocation;

    private RealVar[] ratios;

    private ReconfigurationProblem rp;

    private Solver solver;

    private String id;

    private Model source;

    private Map<VM, VM> references;
    private Map<VM, VM> clones;

    /**
     * The default value of ratio is not logical to detect an unchanged value
     */
    public static final double UNCHECKED_RATIO = Double.MAX_VALUE / 100;

    /**
     * Make a new mapping.
     *
     * @param p the problem to rely on
     * @param r the resource to consider
     */
    public CShareableResource(ReconfigurationProblem p, ShareableResource r) throws SchedulerException {
        this.rc = r;
        this.rp = p;
        this.references = new HashMap<>();
        this.clones = new HashMap<>();
        solver = p.getSolver();
        this.source = p.getSourceModel();
        Node[] nodes = p.getNodes();
        phyRcUsage = new IntVar[nodes.length];
        virtRcUsage = new IntVar[nodes.length];
        this.ratios = new RealVar[nodes.length];
        id = ShareableResource.VIEW_ID_BASE + r.getResourceIdentifier();
        for (int i = 0; i < nodes.length; i++) {
            Node nId = p.getNode(i);
            phyRcUsage[i] = VariableFactory.bounded(p.makeVarLabel("phyRcUsage('", r.getResourceIdentifier(), "', '", nId, "')"), 0, r.getCapacity(nodes[i]), p.getSolver());
            virtRcUsage[i] = VariableFactory.bounded(p.makeVarLabel("virtRcUsage('", r.getResourceIdentifier(), "', '", nId, "')"), 0, Integer.MAX_VALUE / 100, p.getSolver());
            ratios[i] = VariableFactory.real(p.makeVarLabel("overbook('", r.getResourceIdentifier(), "', '", nId, "')"), 1, UNCHECKED_RATIO, 0.01, p.getSolver());
        }


        //Bin packing for the node vmAllocation
        Solver s = p.getSolver();
        List<IntVar> notNullUsage = new ArrayList<>();
        List<IntVar> hosts = new ArrayList<>();

        vmAllocation = new IntVar[p.getVMs().length];
        for (int i = 0; i < vmAllocation.length; i++) {
            VM vmId = p.getVM(i);
            VMTransition a = p.getVMAction(vmId);
            Slice slice = a.getDSlice();
            if (slice == null) {
                //The VMs will not be running, so its consumption is set to 0
                vmAllocation[i] = VariableFactory.fixed(p.makeVarLabel("cste -- " + "vmAllocation('", r.getResourceIdentifier(), "', '", vmId, "'"), 0, s);
            } else {
                //We don't know about the next VM usage for the moment, -1 is used by default to allow to detect an
                //non-updated value.
                vmAllocation[i] = VariableFactory.bounded(p.makeVarLabel("vmAllocation('", r.getResourceIdentifier(), "', '", vmId, "')"), -1, Integer.MAX_VALUE / 1000, s);
                notNullUsage.add(vmAllocation[i]);
                hosts.add(slice.getHoster());
            }

        }
        //We create a BP with only the VMs requiring a not null amount of resources
        ChocoView v = rp.getView(Packing.VIEW_ID);
        if (v == null) {
            throw new SchedulerException(rp.getSourceModel(), "View '" + Cumulatives.VIEW_ID + "' is required but missing");
        }
        ((Packing) v).addDim(r.getResourceIdentifier(),
                virtRcUsage,
                notNullUsage.toArray(new IntVar[notNullUsage.size()]),
                hosts.toArray(new IntVar[hosts.size()]));

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
    public IntVar[] getPhysicalUsage() {
        return phyRcUsage;
    }

    /**
     * Get the physical resource usage of a given node
     *
     * @param nIdx the node identifier
     * @return the variable denoting the resource usage for the node. {@code null} if the node is unknown
     */
    public IntVar getPhysicalUsage(int nIdx) {
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
    public IntVar[] getVirtualUsage() {
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
    public IntVar getVirtualUsage(int nIdx) {
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
    public IntVar[] getVMsAllocation() {
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
    public IntVar getVMsAllocation(int vmIdx) {
        if (vmIdx >= 0 && vmIdx < rp.getVMs().length) {
            return vmAllocation[vmIdx];
        }
        return null;
    }

    /**
     * Get the overbooking ratio for a node.
     * <b>WARNING: it is only allowed to reduce the upper-bound of the ratio using {@code #setSup(x)} methods</b>
     *
     * @param nId the node identifier
     * @return an array of ratios.
     */
    public RealVar getOverbookRatio(int nId) {
        return ratios[nId];
    }

    /**
     * Get the overbooking ratios for every nodes.
     * <b>WARNING: it is only allowed to reduce the upper-bound of the ratio using {@code #setSup(x)} methods</b>
     *
     * @return an array of ratios.
     */
    public RealVar[] getOverbookRatios() {
        return ratios;
    }

    /**
     * Generate and addDim an {@link org.btrplace.plan.event.Allocate} action if the amount of
     * resources allocated to a VM has changed.
     * The action schedule must be known.
     *
     * @param e    the VM identifier
     * @param node the identifier of the node that is currently hosting the VM
     * @param st   the moment that action starts
     * @param ed   the moment the action ends
     * @return {@code true} if the action has been added to the plan,{@code false} otherwise
     */
    public boolean addAllocateAction(ReconfigurationPlan plan, VM e, Node node, int st, int ed) {

        int use = vmAllocation[rp.getVM(e)].getLB();
        if (rc.getConsumption(e) != use) {
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
     * Set the resource usage for each of the VM.
     * If the LB is < 0 , the previous consumption is used to maintain the resource usage.
     * Otherwise, the usage is set to the variable lower bound.
     *
     * @return false if an operation leads to a problem without solution
     */
    @Override
    public boolean beforeSolve(ReconfigurationProblem p) throws SchedulerException {
        for (VM vm : source.getMapping().getAllVMs()) {
            int vmId = p.getVM(vm);
            IntVar v = vmAllocation[vmId];
            if (v.getLB() < 0) {
                int prevUsage = rc.getConsumption(vm);
                try {
                    v.updateLowerBound(prevUsage, Cause.Null);
                } catch (ContradictionException e) {
                    p.getLogger().error("Unable to set the minimal '{}' usage for '{}' to its current usage ({})",
                            rc.getResourceIdentifier(), vm, prevUsage);
                    return false;
                }
            } else {
                try {
                    v.updateLowerBound(v.getLB(), Cause.Null);
                } catch (ContradictionException e) {
                    p.getLogger().error("Unable to set the VM '{}' consumption to '{}'", rc.getResourceIdentifier(), v.getLB());
                    return false;
                }
            }
        }
        return linkVirtualToPhysicalUsage();
    }

    @Override
    public boolean insertActions(ReconfigurationProblem r, ReconfigurationPlan p) {
        Mapping srcMapping = r.getSourceModel().getMapping();

        for (VM vm : r.getFutureRunningVMs()) {
            Slice dSlice = r.getVMAction(vm).getDSlice();
            Node destNode = r.getNode(dSlice.getHoster().getValue());

            if (srcMapping.isRunning(vm) && destNode == srcMapping.getVMLocation(vm)) {
                //Was running and stay on the same node
                //Check if the VM has been cloned
                //TODO: might be too late depending on the symmetry breaking on the actions schedule
                insertAllocateAction(p, vm, destNode, dSlice.getStart().getValue());
            } else {
                //TODO: not constant time operation. Maybe a big failure
                VM dVM = clones.containsKey(vm) ? clones.get(vm) : vm;
                for (Action a : p.getActions()) {
                    if (a instanceof RunningVMPlacement) {
                        RunningVMPlacement tmp = (RunningVMPlacement) a;
                        if (tmp.getVM() == dVM) {
                            if (a instanceof MigrateVM) {
                                //For a migrated VM, we allocate once the migration over
                                insertAllocateEvent(a, Action.Hook.POST, dVM);
                            } else {
                                //Resume or Boot VM
                                //As the VM was not running, we pre-allocate
                                insertAllocateEvent(a, Action.Hook.PRE, dVM);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void insertAllocateEvent(Action a, Action.Hook h, VM vm) {
        int prev = 0;
        VM sVM = references.containsKey(vm) ? references.get(vm) : vm;
        if (rc.consumptionDefined(sVM)) {
            prev = rc.getConsumption(sVM);
        }
        int now = 0;
        IntVar nowI = getVMsAllocation(rp.getVM(sVM));
        if (nowI != null) {
            now = nowI.getLB();
        }
        if (prev != now) {
            AllocateEvent ev = new AllocateEvent(vm, getResourceIdentifier(), now);
            a.addEvent(h, ev);
        }
    }

    private boolean insertAllocateAction(ReconfigurationPlan p, VM vm, Node destNode, int st) {
        String rcId = getResourceIdentifier();
        int prev = rc.getConsumption(vm);
        int now = getVMsAllocation()[rp.getVM(vm)].getValue();
        if (prev != now) {
            Allocate a = new Allocate(vm, destNode, rcId, now, st, st);
            return p.add(a);
        }
        return false;
    }

    private boolean linkVirtualToPhysicalUsage() throws SchedulerException {
        for (int nIdx = 0; nIdx < ratios.length; nIdx++) {
            if (!linkVirtualToPhysicalUsage(nIdx)) {
                return false;
            }
        }

        //The slice scheduling constraint that is necessary
        //TODO: a slice on both the real and the raw resource usage ?
        TIntArrayList cUse = new TIntArrayList();
        List<IntVar> dUse = new ArrayList<>();

        for (VM vmId : rp.getVMs()) {
            VMTransition a = rp.getVMAction(vmId);
            Slice c = a.getCSlice();
            Slice d = a.getDSlice();
            if (c != null) {
                cUse.add(getSourceResource().getConsumption(vmId));
            }
            if (d != null) {
                dUse.add(vmAllocation[rp.getVM(vmId)]);
            }
        }

        IntVar[] capacities = new IntVar[rp.getNodes().length];
        System.arraycopy(virtRcUsage, 0, capacities, 0, rp.getNodes().length);
        ChocoView v = rp.getView(Cumulatives.VIEW_ID);
        if (v == null) {
            throw new SchedulerException(rp.getSourceModel(), "View '" + Cumulatives.VIEW_ID + "' is required but missing");
        }

        ((Cumulatives) v).addDim(capacities, cUse.toArray(), dUse.toArray(new IntVar[dUse.size()]));
        return true;
    }

    private boolean linkVirtualToPhysicalUsage(int nIdx) {
        double r = ratios[nIdx].getUB();
        if (r == UNCHECKED_RATIO) {
            //Default overbooking ratio is 1.
            r = 1;
        }

        try {
            ratios[nIdx].updateBounds(r, r, Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to set '{}' to {}", ratios[nIdx], r);
            return false;
        }


        if (r == 1) {
            solver.post(IntConstraintFactory.arithm(phyRcUsage[nIdx], "=", virtRcUsage[nIdx]));
            try {
                virtRcUsage[nIdx].updateUpperBound(phyRcUsage[nIdx].getUB(), Cause.Null);
            } catch (ContradictionException ex) {
                rp.getLogger().error("Unable to restrict the virtual '{}' capacity of {} to {}: ", rp.getNode(nIdx), phyRcUsage[nIdx].getUB(), ex.getMessage());
                return false;
            }
        } else {
            int maxPhy = getSourceResource().getCapacity(rp.getNode(nIdx));
            int maxVirt = (int) (maxPhy * r);
            if (maxVirt != 0) {
                solver.post(new RoundedUpDivision(phyRcUsage[nIdx], virtRcUsage[nIdx], r));
            } else {
                try {
                    phyRcUsage[nIdx].instantiateTo(0, Cause.Null);
                } catch (ContradictionException ex) {
                    rp.getLogger().error("Unable to restrict the physical '{}' capacity of {} to {}: {}", getResourceIdentifier(), rp.getNode(nIdx), maxPhy, ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        this.references.put(clone, vm);
        this.clones.put(vm, clone);
        return true;
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
        public SolverViewBuilder build(final ModelView v) throws SchedulerException {
            return new DelegatedBuilder(v.getIdentifier(), Arrays.asList(Packing.VIEW_ID, Cumulatives.VIEW_ID)) {
                @Override
                public ChocoView build(ReconfigurationProblem r) throws SchedulerException {
                    return new CShareableResource(r, (ShareableResource) v);
                }
            };
        }
    }
}
