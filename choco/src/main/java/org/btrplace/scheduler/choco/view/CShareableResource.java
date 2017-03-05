/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.ResourceCapacity;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ResourceRelated;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.RunningVMPlacement;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.SchedulerModelingException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.RoundedUpDivision;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specify, for a given resource, the physical resource usage associated to each server,
 * and the virtual resource usage consumed by each of the VMs they host.
 *
 * @author Fabien Hermenier
 */
public class CShareableResource implements ChocoView {

    private ShareableResource rc;

    private List<IntVar> phyRcUsage;

    private List<IntVar> virtRcUsage;

    private TIntList vmAllocation;

    private TDoubleList ratios;

    private ReconfigurationProblem rp;

    private org.chocosolver.solver.Model csp;

    private String id;

    private Model source;

    private Map<VM, VM> references;
    private Map<VM, VM> clones;

    private TObjectDoubleMap<Node> wantedRatios;
    private TObjectIntMap<VM> wantedAmount;
    private TObjectIntMap<Node> wantedCapacity;

    /**
     * The default value of ratio is not logical to detect an unchanged value
     */
    public static final double UNCHECKED_RATIO = Double.MAX_VALUE / 100;

    /**
     * Make a new mapping.
     *
     * @param r the resource to consider
     */
    public CShareableResource(ShareableResource r) throws SchedulerException {
        this.rc = r;
        this.id = r.getIdentifier();
        wantedCapacity = new TObjectIntHashMap<>();
        wantedAmount = new TObjectIntHashMap<>();
        wantedRatios = new TObjectDoubleHashMap<>();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem p) throws SchedulerException {
        this.rp = p;
        this.references = new HashMap<>();
        this.clones = new HashMap<>();
        csp = p.getModel();
        this.source = p.getSourceModel();
        List<Node> nodes = p.getNodes();
        phyRcUsage = new ArrayList<>(nodes.size());
        virtRcUsage = new ArrayList<>(nodes.size());
        this.ratios = new TDoubleArrayList(nodes.size());
        id = ShareableResource.VIEW_ID_BASE + rc.getResourceIdentifier();
        for (Node nId : p.getNodes()) {
            phyRcUsage.add(csp.intVar(p.makeVarLabel("phyRcUsage('", rc.getResourceIdentifier(), "', '", nId, "')"), 0, rc.getCapacity(nId), true));
            virtRcUsage.add(csp.intVar(p.makeVarLabel("virtRcUsage('", rc.getResourceIdentifier(), "', '", nId, "')"), 0, Integer.MAX_VALUE / 100, true));
            ratios.add(UNCHECKED_RATIO);
        }
        phyRcUsage = Collections.unmodifiableList(phyRcUsage);
        virtRcUsage = Collections.unmodifiableList(virtRcUsage);

        //Bin packing for the node vmAllocation

        vmAllocation = new TIntArrayList();
        for (VM vmId : p.getVMs()) {
            VMTransition a = p.getVMAction(vmId);
            Slice slice = a.getDSlice();
            if (slice == null) {
                //The VMs will not be running, so its consumption is set to 0
                vmAllocation.add(0);
            } else {
                //We don't know about the next VM usage for the moment, -1 is used by default to allow to detect an
                //non-updated value.
                vmAllocation.add(-1);
            }
        }
        return true;

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
    public List<IntVar> getPhysicalUsage() {
        return phyRcUsage;
    }

    /**
     * Get the physical resource usage of a given node
     *
     * @param nIdx the node identifier
     * @return the variable denoting the resource usage for the node.
     */
    public IntVar getPhysicalUsage(int nIdx) {
        return phyRcUsage.get(nIdx);
    }

    /**
     * Get the virtual resource usage  that is made by the VMs on the nodes.
     * <b>Warning: the only possible approach to restrict these value is to increase their
     * upper bound using the associated {@code setSup()} method</b>
     *
     * @return an immutable list of variables denoting each node virtual resource usage.
     */
    public List<IntVar> getVirtualUsage() {
        return virtRcUsage;
    }
    /**
     * Get the minimum amount of resource to allocate to a VM.
     *
     * @param vmIdx the VM identifier
     * @return the variable denoting the virtual resources to allocate to the VM
     */
    public int getVMAllocation(int vmIdx) {
        return vmAllocation.get(vmIdx);
    }

    /**
     * Change the VM resource allocation.
     *
     * @param vmIdx the VM identifier
     * @param v     the amount to ask.
     * @return the retained value. May be bigger than {@code v} if a previous call asks for more
     */
    public int minVMAllocation(int vmIdx, int v) {
        int vv = Math.max(v, vmAllocation.get(vmIdx));
        vmAllocation.set(vmIdx, vv);
        return vv;
    }

    /**
     * Get the overbooking ratio for a node.
     *
     * @param nId the node identifier
     * @return a ratio >= 1
     */
    public double getOverbookRatio(int nId) {
        return ratios.get(nId);
    }

    /**
     * Cap the overbooking ratio for a given node.
     * @param nIdx the node
     * @param d the new ratio. {@code >= 1}
     * @return the resulting ratio. Will be lower than {@code d} if a previous cap stated a lower value
     */
    public double capOverbookRatio(int nIdx, double d) {
        if (d < 1) {
            return ratios.get(nIdx);
        }
        double v = Math.min(ratios.get(nIdx), d);
        ratios.set(nIdx, v);
        return v;
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
            int v = getVMAllocation(vmId);
            if (v < 0) {
                int prevUsage = rc.getConsumption(vm);
                minVMAllocation(vmId, prevUsage);
            }

        }

        ChocoView v = rp.getView(Packing.VIEW_ID);
        if (v == null) {
            throw SchedulerModelingException.missingView(rp.getSourceModel(), Packing.VIEW_ID);
        }

        IntVar[] host = new IntVar[p.getFutureRunningVMs().size()];
        int[] demand = new int[host.length];
        int i = 0;
        for (VM vm : p.getFutureRunningVMs()) {
            host[i] = rp.getVMAction(vm).getDSlice().getHoster();
            demand[i] = getVMAllocation(p.getVM(vm));
            i++;
        }
        ((Packing) v).addDim(rc.getResourceIdentifier(),
                virtRcUsage,
                demand,
                host);

        return linkVirtualToPhysicalUsage();
    }

    private VM destVM(VM vm) {
        return clones.containsKey(vm) ? clones.get(vm) : vm;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem r, Solution s, ReconfigurationPlan p) {
        Mapping srcMapping = r.getSourceModel().getMapping();

        // Encache the VM -> Action to ease the event injection.
        Map<VM, Action> actions = new HashMap<>();
        p.getActions().stream().filter(RunningVMPlacement.class::isInstance)
                .map(a -> (RunningVMPlacement) a).forEach(
                a -> actions.put(destVM(a.getVM()), (Action) a));
        for (VM vm : r.getFutureRunningVMs()) {
            Slice dSlice = r.getVMAction(vm).getDSlice();
            Node destNode = r.getNode(s.getIntVal(dSlice.getHoster()));

            if (srcMapping.isRunning(vm) && destNode.equals(srcMapping.getVMLocation(vm))) {
                //Was running and stay on the same node
                //Check if the VM has been cloned
                //TODO: might be too late depending on the symmetry breaking on the actions schedule
                insertAllocateAction(p, vm, destNode, s.getIntVal(dSlice.getStart()));
            } else {
                VM dVM = destVM(vm);
                Action a = actions.get(dVM);
                if (a instanceof MigrateVM) {
                    //For a migrated VM, we allocate once the migration over
                    insertAllocateEvent(a, Action.Hook.POST, dVM);
                } else {
                    //Resume or Boot VM
                    //As the VM was not running, we pre-allocate
                    insertAllocateEvent(a, Action.Hook.PRE, dVM);
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
        int now = getVMAllocation(rp.getVM(sVM));
        if (prev != now) {
            AllocateEvent ev = new AllocateEvent(vm, getResourceIdentifier(), now);
            a.addEvent(h, ev);
        }
    }

    private boolean insertAllocateAction(ReconfigurationPlan p, VM vm, Node destNode, int st) {
        String rcId = getResourceIdentifier();
        int prev = rc.getConsumption(vm);
        int now = getVMAllocation(rp.getVM(vm));
        if (prev != now) {
            Allocate a = new Allocate(vm, destNode, rcId, now, st, st);
            return p.add(a);
        }
        return false;
    }

    /**
     * Reduce the cardinality wrt. the worst case scenario.
     *
     * @param nIdx the node index
     * @param min  the min (but > 0 ) consumption for a VM
     * @param nbZeroes the number of VMs consuming 0
     * @return {@code false} if the problem no longer has a solution
     */
    private boolean capHosting(int nIdx, int min, int nbZeroes) {
        Node n = rp.getNode(nIdx);
        double capa = getSourceResource().getCapacity(n) * getOverbookRatio(nIdx)/*.getLB()*/;
        int card = (int) (capa / min + 1) + nbZeroes;
        try {
            //Restrict the hosting capacity.
            rp.getNbRunningVMs().get(nIdx).updateUpperBound(card, Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to cap the hosting capacity of '" + n + " ' to " + card, ex);
            return false;
        }
        return true;
    }

    private boolean linkVirtualToPhysicalUsage() throws SchedulerException {
        int min = Integer.MAX_VALUE;

        //Number of VMs with a 0 usage
        int nbZeroes = 0;

        for (int vId = 0; vId < vmAllocation.size(); vId++) {
            int alloc = vmAllocation.get(vId);
            if (alloc > 0) {
                min = Math.min(alloc, min);
            } else {
                nbZeroes++;
            }
        }
        for (int nIdx = 0; nIdx < ratios.size(); nIdx++) {
            if (!linkVirtualToPhysicalUsage(nIdx)) {
                return false;
            }
            if (!capHosting(nIdx, min, nbZeroes)) {
                return false;
            }
        }

        //The slice scheduling constraint that is necessary
        TIntArrayList cUse = new TIntArrayList();
        List<IntVar> dUse = new ArrayList<>();

        for (VMTransition a : rp.getVMActions()) {
            VM vm = a.getVM();
            Slice c = a.getCSlice();
            Slice d = a.getDSlice();
            if (c != null) {
                cUse.add(getSourceResource().getConsumption(vm));
            }
            if (d != null) {
                int m = getVMAllocation(rp.getVM(vm));
                dUse.add(rp.fixed(m, "vmAllocation('", getResourceIdentifier(), "', '", vm, "'"));

            }
        }

        Cumulatives v = (Cumulatives) rp.getView(Cumulatives.VIEW_ID);
        v.addDim(virtRcUsage, cUse.toArray(), dUse.toArray(new IntVar[dUse.size()]));

        checkInitialSatisfaction();
        return true;
    }

    /**
     * Check if the initial capacity > sum current consumption
     * The ratio is instantiated now so the computation is correct
     */
    private void checkInitialSatisfaction() {
        //Seems to me we don't support ratio change
        for (Node n : rp.getSourceModel().getMapping().getOnlineNodes()) {
            int nIdx = rp.getNode(n);
            double ratio = getOverbookRatio(nIdx)/*.getLB()*/;
            double capa = getSourceResource().getCapacity(n) * ratio;
            int usage = 0;
            for (VM vm : rp.getSourceModel().getMapping().getRunningVMs(n)) {
                usage += getSourceResource().getConsumption(vm);
                if (usage > capa) {
                    //Here, the problem is not feasible but we consider an exception
                    //because such a situation does not physically makes sense (one cannot run at 110%)
                    throw new SchedulerModelingException(rp.getSourceModel(), "Usage of virtual resource " + getResourceIdentifier() + " on node " + n + " (" + usage + ") exceeds its capacity (" + capa + ")");
                }
            }
        }
    }


    private boolean linkVirtualToPhysicalUsage(int nIdx) {
        double r = ratios.get(nIdx);
        if (r == UNCHECKED_RATIO) {
            //Default overbooking ratio is 1.
            r = 1;
            capOverbookRatio(nIdx, r);
            return noOverbook(nIdx);
        }
        return overbook(nIdx, r);
    }

    private boolean overbook(int nIdx, double r) {
        Node n = rp.getNode(nIdx);
        int maxPhy = getSourceResource().getCapacity(n);
        int maxVirt = (int) (maxPhy * r);
        if (maxVirt != 0) {
            csp.post(new RoundedUpDivision(phyRcUsage.get(nIdx), virtRcUsage.get(nIdx), r));
            return true;
        }

        try {
            phyRcUsage.get(nIdx).instantiateTo(0, Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to restrict the physical '" + getResourceIdentifier() + "' capacity of " + n + " to " + maxPhy, ex);
            return false;
        }
        return true;
    }

    private boolean noOverbook(int nIdx) {
        csp.post(csp.arithm(phyRcUsage.get(nIdx), "=", virtRcUsage.get(nIdx)));
        try {
            virtRcUsage.get(nIdx).updateUpperBound(phyRcUsage.get(nIdx).getUB(), Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to restrict the virtual '" + getResourceIdentifier() + "' capacity of " + rp.getNode(nIdx) + " to " + phyRcUsage.get(nIdx).getUB(), ex);
            return false;
        }
        return true;
    }

    private double ratio(Node n, double d) {
        if (wantedRatios.containsKey(n)) {
            return Math.min(d, wantedRatios.get(n));
        }
        return d;
    }

    private int consumption(VM v, int a) {
        if (wantedAmount.containsKey(v)) {
            return Math.max(a, wantedAmount.get(v));
        }
        return a;
    }

    private int capacity(Node n, int a) {
        if (wantedCapacity.containsKey(n)) {
            return Math.max(a, wantedCapacity.get(n));
        }
        return a;
    }

    /**
     * {@inheritDoc}
     *
     * @param i the model to use to inspect the VMs.
     * @return the set of VMs that cannot have their associated {@link Preserve} constraint satisfy with regards
     * to a possible {@link Overbook} and single-node {@link ResourceCapacity} constraint.
     */
    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        for (SatConstraint c : i.getSatConstraints()) {
            if (!(c instanceof ResourceRelated && ((ResourceRelated) c).getResource().equals(rc.getResourceIdentifier()))) {
                continue;
            }
            if (c instanceof Preserve) {
                VM v = c.getInvolvedVMs().iterator().next();
                wantedAmount.put(v, consumption(v, ((Preserve) c).getAmount()));
            } else if (c instanceof Overbook) {
                Node n = c.getInvolvedNodes().iterator().next();
                wantedRatios.put(n, ratio(n, ((Overbook) c).getRatio()));
            } else if (c instanceof ResourceCapacity && c.getInvolvedNodes().size() == 1) {
                Node n = c.getInvolvedNodes().iterator().next();
                wantedCapacity.put(n, capacity(n, ((ResourceCapacity) c).getAmount()));
            }
        }
        Mapping m = i.getModel().getMapping();
        Set<VM> candidates = new HashSet<>();
        for (Node n : m.getOnlineNodes()) {
            if (overloaded(m, n)) {
                candidates.addAll(m.getRunningVMs(n));
            }
        }
        return candidates;
    }

    private boolean overloaded(Mapping m, Node n) {
        int free = rc.getCapacity(n);
        if (wantedCapacity.containsKey(n)) {
            free -= wantedCapacity.get(n);
        }
        if (wantedRatios.containsKey(n)) {
            free *= wantedRatios.get(n);
        }
        for (VM vm : m.getRunningVMs(n)) {
            if (wantedAmount.containsKey(vm)) {
                free -= wantedAmount.get(vm);
            } else {
                free -= rc.getConsumption(vm);
            }
        }
        return free < 0;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        this.references.put(clone, vm);
        this.clones.put(vm, clone);
        return true;
    }

    @Override
    public List<String> getDependencies() {
        return Arrays.asList(Packing.VIEW_ID, Cumulatives.VIEW_ID);
    }

    /**
     * Estimate the weight of each VMs with regards to multiple dimensions.
     * In practice, it sums the normalised size of each VM against the total capacity
     *
     * @param rp  the problem to solve
     * @param rcs the resources to consider
     * @return a weight per VM
     */
    public static Map<VM, Integer> getWeights(ReconfigurationProblem rp, List<CShareableResource> rcs) {
        Model mo = rp.getSourceModel();

        int[] capa = new int[rcs.size()];
        int[] cons = new int[rcs.size()];
        Map<VM, Integer> cost = new HashMap<>();
        for (Node n : mo.getMapping().getAllNodes()) {
            for (int i = 0; i < rcs.size(); i++) {
                capa[i] += rcs.get(i).virtRcUsage.get(rp.getNode(n)).getUB() * rcs.get(i).ratios.get(rp.getNode(n))/*.getLB()*/;
            }
        }

        for (VM v : mo.getMapping().getAllVMs()) {
            for (int i = 0; i < rcs.size(); i++) {
                cons[i] += rcs.get(i).getVMAllocation(rp.getVM(v));
            }
        }

        for (VM v : mo.getMapping().getAllVMs()) {
            double sum = 0;
            for (int i = 0; i < rcs.size(); i++) {
                double ratio = 0;
                if (cons[i] > 0) {
                    ratio = 1.0 * rcs.get(i).getVMAllocation(rp.getVM(v)) / capa[i];
                }
                sum += ratio;
            }
            cost.put(v, (int) (sum * 10000));
        }
        return cost;
    }
}
