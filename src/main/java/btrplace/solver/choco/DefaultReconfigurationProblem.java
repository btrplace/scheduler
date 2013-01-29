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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.ShareableResource;
import btrplace.plan.Action;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.AllocateEvent;
import btrplace.solver.SolverException;
import btrplace.solver.choco.actionModel.*;
import btrplace.solver.choco.chocoUtil.AliasedCumulatives;
import btrplace.solver.choco.chocoUtil.AliasedCumulativesBuilder;
import btrplace.solver.choco.chocoUtil.BinPacking;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.BranchAndBound;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.objective.IntObjectiveManager;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.cp.solver.search.set.StaticSetVarOrder;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.IObjectiveManager;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;


/**
 * Default implementation of {@link ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblem implements ReconfigurationProblem {

    private final Logger logger = LoggerFactory.getLogger("ChocoRP");

    private boolean useLabels = false;

    /**
     * The maximum duration of a plan in seconds: One hour.
     */
    public static final int DEFAULT_MAX_TIME = 3600;

    private Model model;

    private CPSolver solver;

    private Set<UUID> ready;
    private Set<UUID> runnings;
    private Set<UUID> sleepings;
    private Set<UUID> killed;

    private Set<UUID> manageable;

    private UUID[] vms;
    private TObjectIntHashMap<UUID> revVMs;

    private UUID[] nodes;
    private TObjectIntHashMap<UUID> revNodes;

    private IntDomainVar start;
    private IntDomainVar end;

    private VMActionModel[] vmActions;
    private NodeActionModel[] nodeActions;

    private DurationEvaluators durEval;

    private Map<String, ResourceMapping> resources;

    private IntDomainVar[] vmsCountOnNodes;

    private SliceSchedulerBuilder taskSchedBuilder;

    private AliasedCumulativesBuilder cumulativesBuilder;

    private ObjectiveAlterer objAlterer = null;

    /**
     * Make a new RP where the next state for every VM is indicated.
     * If the state for a VM is omitted, it is considered as unchanged
     * {@link DefaultReconfigurationProblemBuilder} can be used to simplify the instantiation process
     *
     * @param m          the initial model
     * @param dEval      to evaluate the duration of every action
     * @param ready      the VMs that must be in the ready state
     * @param running    the VMs that must be in the running state
     * @param sleeeping  the VMs that must be in the sleeping state
     * @param manageable the VMs that can be managed by the solver
     * @param label      {@code true} to label the variables (for debugging purpose)
     * @param killed     the VMs that must be killed
     * @throws SolverException if an error occurred
     */
    public DefaultReconfigurationProblem(Model m,
                                         DurationEvaluators dEval,
                                         Set<UUID> ready,
                                         Set<UUID> running,
                                         Set<UUID> sleeeping,
                                         Set<UUID> killed,
                                         Set<UUID> manageable,
                                         boolean label
    ) throws SolverException {
        this.ready = new HashSet<UUID>(ready);
        this.runnings = new HashSet<UUID>(running);
        this.sleepings = new HashSet<UUID>(sleeeping);
        this.killed = new HashSet<UUID>(killed);
        this.manageable = new HashSet<UUID>(manageable);
        this.useLabels = label;
        model = m;
        durEval = dEval;

        solver = new CPSolver();
        start = solver.makeConstantIntVar("RP.start", 0);
        end = solver.createBoundIntVar("RP.end", 0, DEFAULT_MAX_TIME);

        solver.post(solver.geq(end, start));

        fillElements();

        makeCardinalities();

        makeNodeActionModels();
        makeVMActionModels();

        makeResources();

        linkCardinatiesWithSlices();

        taskSchedBuilder = new SliceSchedulerBuilder(this);
        cumulativesBuilder = new AliasedCumulativesBuilder(this);
    }

    @Override
    public ReconfigurationPlan solve(int timeLimit, boolean optimize) throws SolverException {

        if (!maintainResourceUsage()) {
            return null;
        }

        addContinuousResourceCapacities();

        solver.post(taskSchedBuilder.build());

        for (AliasedCumulatives cstr : cumulativesBuilder.getConstraints()) {
            solver.post(cstr);
        }

        //Let's rock
        if (timeLimit > 0) {
            solver.setTimeLimit(timeLimit);
        }

        if (objAlterer == null) {
            solver.getConfiguration().putBoolean(choco.kernel.solver.Configuration.STOP_AT_FIRST_SOLUTION, !optimize);
        } else if (optimize) {
            solver.getConfiguration().putBoolean(choco.kernel.solver.Configuration.STOP_AT_FIRST_SOLUTION, true);
        }
        solver.getConfiguration().putInt(Configuration.SOLUTION_POOL_CAPACITY, Integer.MAX_VALUE);
        solver.generateSearchStrategy();

        //Instantiate the VM usage to its LB.
        for (ResourceMapping rcm : getResourceMappings()) {
            for (IntDomainVar v : rcm.getVMsAllocation()) {
                try {
                    v.setVal(v.getInf());
                } catch (ContradictionException e) {
                    getLogger().error("Unable to set the VM '{}' consumption to '{}'", rcm.getIdentifier(), v.getInf());
                    return null;
                }
            }
        }

        //Just in case, a default search heuristic for free variables
        IntDomainVar[] foo = new IntDomainVar[solver.getNbIntVars()];
        SetVar[] bar = new SetVar[solver.getNbSetVars()];

        for (int i = 0; i < foo.length; i++) {
            foo[i] = solver.getIntVarQuick(i);
        }

        for (int i = 0; i < bar.length; i++) {
            bar[i] = solver.getSetVarQuick(i);
        }

        solver.addGoal(new AssignVar(new StaticVarOrder(solver, foo), new MinVal()));
        solver.addGoal(new AssignVar(new StaticSetVarOrder(solver, bar), new MinVal()));

        if (objAlterer == null) {
            solver.launch();
        } else if (optimize) {
            launchWithAlterer();
        }

        if (Boolean.FALSE == solver.isFeasible()) {
            return null;
        } else if (solver.isFeasible() == null) {
            throw new SolverException(model, "Unable to state about the problem feasibility");
        }

        DefaultReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        for (ActionModel action : nodeActions) {
            action.insertActions(plan);
        }
        for (ActionModel action : vmActions) {
            action.insertActions(plan);
        }

        assert plan.isApplyable();
        assert checkConsistency(plan);
        return plan;
    }

    /**
     * Launch the solver with a known ObjectiveAlterer.
     * Each time a solution has been computed, the alterer is called to set a new bound for the objective
     *
     * @return {@code true} iff the injection was successful.
     */
    private void launchWithAlterer() throws SolverException {
        BranchAndBound bb = (BranchAndBound) solver.getSearchStrategy();
        IObjectiveManager obj = bb.getObjectiveManager();
        Field f;
        try {
            f = IntObjectiveManager.class.getDeclaredField("targetBound");
            f.setAccessible(true);
        } catch (Exception e) {
            throw new SolverException(model, "Unable to inject the alterer: " + e.getMessage(), e);
        }

        solver.launch();
        if (solver.isFeasible() == Boolean.TRUE) {
            do {
                int objVal = solver.getObjectiveValue().intValue();
                int newBound = objAlterer.tryNewValue(objVal);
                try {
                    f.set(obj, newBound);
                } catch (Exception e) {
                    throw new SolverException(model, "Unable to set the new target bound " + newBound + " for the objective " + solver.getObjective().getName() + ": " + e.getMessage(), e);
                }
            } while (solver.nextSolution() == Boolean.TRUE);
        }
        //solver.worldPopUntil(solver.getSearchStrategy().baseWorld);
        //solver.restoreSolution(s);
    }

    /**
     * Set the VM resource usage for the result, to its current usage.
     */
    private boolean maintainResourceUsage() {
        for (ResourceMapping rcm : getResourceMappings()) {
            for (UUID vm : getSourceModel().getMapping().getAllVMs()) {
                int vmId = getVM(vm);
                if (rcm.getVMsAllocation()[vmId].getInf() < 0) {
                    int prevUsage = rcm.getSourceResource().get(vm);
                    try {
                        rcm.getVMsAllocation()[vmId].setInf(prevUsage);
                    } catch (ContradictionException e) {
                        getLogger().error("Unable to set the minimal '{}' usage for '{}' to its current usage ({})",
                                rcm.getIdentifier(), vm, prevUsage);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void addContinuousResourceCapacities() {
        TIntArrayList cUse = new TIntArrayList();
        List<IntDomainVar> iUse = new ArrayList<IntDomainVar>();
        for (int j = 0; j < getVMs().length; j++) {
            VMActionModel a = vmActions[j];
            if (a.getDSlice() != null) {
                iUse.add(solver.makeConstantIntVar(1));
            }
            if (a.getCSlice() != null) {
                cUse.add(1);
            }
        }

        taskSchedBuilder.add(getNbRunningVMs(),
                cUse.toArray(),
                iUse.toArray(new IntDomainVar[iUse.size()]));
    }

    /**
     * Create the {@link ResourceMapping} from the {@link ShareableResource}.
     *
     * @throws SolverException if an error occurred
     */
    private void makeResources() throws SolverException {
        resources = new HashMap<String, ResourceMapping>(model.getResources().size());
        for (ShareableResource rc : model.getResources()) {
            ResourceMapping rm = new ResourceMapping(this, rc);
            resources.put(rm.getIdentifier(), rm);
        }
    }

    /**
     * Create the cardinalities variables.
     *
     * @throws SolverException if an error occurred
     */
    private void makeCardinalities() throws SolverException {
        vmsCountOnNodes = new IntDomainVar[nodes.length];
        int nbVMs = vms.length;
        for (int i = 0; i < vmsCountOnNodes.length; i++) {
            vmsCountOnNodes[i] = solver.createBoundIntVar(makeVarLabel("nbVMsOn('" + getNode(i) + "')"), 0, nbVMs);
        }
    }

    private void linkCardinatiesWithSlices() throws SolverException {
        IntDomainVar[] ds = SliceUtils.extractHosters(ActionModelUtils.getDSlices(vmActions));
        IntDomainVar[] usages = new IntDomainVar[ds.length];
        for (int i = 0; i < ds.length; i++) {
            usages[i] = solver.makeConstantIntVar(1);
        }
        solver.post(new BinPacking(solver.getEnvironment(), vmsCountOnNodes, usages, ds));
    }

    private void fillElements() throws SolverException {

        Set<UUID> allVMs = new HashSet<UUID>(model.getMapping().getAllVMs());
        allVMs.addAll(ready); //The only VMs that may not appear in the mapping

        vms = new UUID[allVMs.size()];
        revVMs = new TObjectIntHashMap<UUID>(allVMs.size(), 0.5f, -1); //0.5f is the default load factor

        int i = 0;
        for (UUID vm : allVMs) {
            vms[i] = vm;
            revVMs.put(vm, i++);
        }

        nodes = new UUID[model.getMapping().getAllNodes().size()];
        revNodes = new TObjectIntHashMap<UUID>(nodes.length, 0.5f, -1);
        i = 0;
        for (UUID nId : model.getMapping().getAllNodes()) {
            nodes[i] = nId;
            revNodes.put(nId, i++);
        }
    }

    private void makeVMActionModels() throws SolverException {
        Mapping map = model.getMapping();
        vmActions = new VMActionModel[vms.length];
        for (int i = 0; i < vms.length; i++) {
            UUID vmId = vms[i];
            if (runnings.contains(vmId)) {
                if (map.getSleepingVMs().contains(vmId)) {
                    vmActions[i] = new ResumeVMModel(this, vmId);
                } else if (map.getRunningVMs().contains(vmId)) {
                    if (manageable.contains(vmId)) {
                        vmActions[i] = new RelocatableVMModel(this, vmId);
                    } else {
                        vmActions[i] = new StayRunningVMModel(this, vmId);
                    }
                } else if (map.getReadyVMs().contains(vmId)) {
                    vmActions[i] = new BootVMModel(this, vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' running: not ready");
                }
            }
            if (ready.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (!map.getAllVMs().contains(vmId)) {
                    vmActions[i] = new ForgeVMModel(this, vmId);
                } else if (map.getReadyVMs().contains(vmId)) {
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else if (map.getRunningVMs().contains(vmId)) {
                    vmActions[i] = new ShutdownVMModel(this, vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' ready: not in the 'running' state or already forged");
                }
            }
            if (sleepings.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (map.getRunningVMs().contains(vmId)) {
                    vmActions[i] = new SuspendVMModel(this, vmId);
                } else if (map.getSleepingVMs().contains(vmId)) {
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' sleeping: should be running");
                }
            }
            if (killed.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (map.containsVM(vmId)) {
                    vmActions[i] = new KillVMActionModel(this, vmId);
                } else {
                    throw new SolverException(model, "Unable to kill VM '" + vmId + "': unknown");
                }
            }
            if (vmActions[i] == null) {
                //Next state is undefined, keep the current state
                //Need to update runnings, sleeping and waitings accordingly
                if (map.getRunningVMs().contains(vmId)) {
                    runnings.add(vmId);
                    if (manageable.contains(vmId)) {
                        vmActions[i] = new RelocatableVMModel(this, vmId);
                    } else {
                        vmActions[i] = new StayRunningVMModel(this, vmId);
                    }
                } else if (map.getReadyVMs().contains(vmId)) {
                    ready.add(vmId);
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else if (map.getSleepingVMs().contains(vmId)) {
                    sleepings.add(vmId);
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else {
                    throw new SolverException(model, "Unable to infer the next state of VM '" + vmId + "'");
                }
            }
        }
    }

    private void makeNodeActionModels() throws SolverException {

        Mapping m = model.getMapping();
        nodeActions = new NodeActionModel[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            UUID nId = nodes[i];
            if (m.getOfflineNodes().contains(nId)) {
                nodeActions[i] = new BootableNodeModel(this, nId);
            }
            if (m.getOnlineNodes().contains(nId)) {
                if (nodeActions[i] != null) {
                    throw new SolverException(model, "Next state for node '" + nId + "' is ambiguous");
                }
                nodeActions[i] = new ShutdownableNodeModel(this, nId);
            }
        }
    }

    @Override
    public int getCurrentVMLocation(int vmIdx) {
        UUID id = getVM(vmIdx);
        if (id == null) {
            return -1;
        }
        UUID nodeId = model.getMapping().getVMLocation(id);
        return nodeId == null ? -1 : getNode(nodeId);
    }

    @Override
    public UUID[] getNodes() {
        return nodes;
    }

    @Override
    public UUID[] getVMs() {
        return vms;
    }

    @Override
    public Model getSourceModel() {
        return model;
    }

    @Override
    public Set<UUID> getFutureRunningVMs() {
        return runnings;
    }

    @Override
    public Set<UUID> getFutureReadyVMs() {
        return ready;
    }

    @Override
    public Set<UUID> getFutureSleepingVMs() {
        return sleepings;
    }

    @Override
    public Set<UUID> getFutureKilledVMs() {
        return killed;
    }

    @Override
    public IntDomainVar getStart() {
        return start;
    }

    @Override
    public IntDomainVar getEnd() {
        return end;
    }

    @Override
    public int getVM(UUID vm) {
        return revVMs.get(vm);
    }

    @Override
    public UUID getVM(int idx) {
        return vms[idx];
    }

    @Override
    public int getNode(UUID n) {
        return revNodes.get(n);
    }

    @Override
    public UUID getNode(int idx) {
        return nodes[idx];
    }

    @Override
    public VMActionModel[] getVMActions() {
        return vmActions;
    }

    @Override
    public VMActionModel[] getVMActions(Set<UUID> id) {
        return vmActions;
    }

    @Override
    public VMActionModel getVMAction(UUID id) {
        int idx = getVM(id);
        return idx < 0 ? null : vmActions[idx];
    }

    @Override
    public NodeActionModel getNodeAction(UUID id) {
        int idx = getNode(id);
        return idx < 0 ? null : nodeActions[idx];
    }

    @Override
    public NodeActionModel[] getNodeActions() {
        return nodeActions;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durEval;
    }

    @Override
    public void insertAllocateAction(ReconfigurationPlan plan, UUID vm, UUID node, int st, int ed) {
        for (Map.Entry<String, ResourceMapping> e : resources.entrySet()) {
            ResourceMapping rcm = e.getValue();
            String rcId = e.getKey();
            int prev = rcm.getSourceResource().get(vm);
            int now = rcm.getVMsAllocation()[getVM(vm)].getInf();
            if (prev != now) {
                Allocate a = new Allocate(vm, node, rcId, now, st, ed);
                plan.add(a);
            }
        }
    }

    @Override
    public void insertNotifyAllocations(Action a, UUID vm, Action.Hook k) {
        for (Map.Entry<String, ResourceMapping> e : resources.entrySet()) {
            ResourceMapping rcm = e.getValue();
            String rcId = e.getKey();
            int prev = rcm.getSourceResource().get(vm);
            int now = rcm.getVMsAllocation()[getVM(vm)].getInf();
            if (prev != now) {
                a.addEvent(k, new AllocateEvent(vm, rcId, now));
            }
        }
    }

    private boolean checkConsistency(ReconfigurationPlan p) {
        if (p.getDuration() != end.getVal()) {
            logger.error("The plan effective duration ({}) and the computed duration ({}) mismatch", p.getDuration(), end.getVal());
            return false;
        }
        return true;
    }

    @Override
    public IntDomainVar[] getNbRunningVMs() {
        return vmsCountOnNodes;
    }

    @Override
    public ResourceMapping getResourceMapping(String id) {
        return resources.get(id);
    }

    @Override
    public Collection<ResourceMapping> getResourceMappings() {
        return resources.values();
    }

    @Override
    public CPSolver getSolver() {
        return solver;
    }

    @Override
    public SliceSchedulerBuilder getTaskSchedulerBuilder() {
        return taskSchedBuilder;
    }

    @Override
    public AliasedCumulativesBuilder getAliasedCumulativesBuilder() {
        return cumulativesBuilder;
    }

    @Override
    public IntDomainVar makeHostVariable(String n) {
        return solver.createEnumIntVar(useLabels ? n : "", 0, nodes.length - 1);
    }

    @Override
    public IntDomainVar makeCurrentHost(String n, UUID vmId) throws SolverException {
        int idx = getVM(vmId);
        if (idx < 0) {
            throw new SolverException(model, "Unknown VM '" + vmId + "'");
        }
        return makeCurrentNode(useLabels ? n : "", model.getMapping().getVMLocation(vmId));
    }

    @Override
    public IntDomainVar makeCurrentNode(String n, UUID nId) throws SolverException {
        int idx = getNode(nId);
        if (idx < 0) {
            throw new SolverException(model, "Unknown node '" + nId + "'");
        }
        return solver.makeConstantIntVar(useLabels ? n : "", idx);
    }

    @Override
    public IntDomainVar makeDuration(String n) {
        return solver.createBoundIntVar(useLabels ? n : "", 0, end.getSup());
    }

    @Override
    public IntDomainVar makeDuration(String n, int lb, int ub) throws SolverException {
        if (lb < 0 || ub < lb) {
            throw new SolverException(model, "Unable to create duration '" + n + "': invalid bounds");
        }
        return solver.createBoundIntVar(useLabels ? n : "", lb, ub < end.getSup() ? ub : end.getSup());
    }

    @Override
    public String makeVarLabel(String lbl) {
        return useLabels ? lbl : "";
    }

    @Override
    public boolean isVarLabelling() {
        return useLabels;
    }

    @Override
    public Set<UUID> getManageableVMs() {
        return manageable;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ObjectiveAlterer getObjectiveAlterer() {
        return objAlterer;
    }

    @Override
    public void setObjectiveAlterer(ObjectiveAlterer a) {
        objAlterer = a;
    }
}
