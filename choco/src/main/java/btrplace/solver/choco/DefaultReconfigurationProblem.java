
/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.view.ModelView;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.actionModel.*;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.extensions.AliasedCumulatives;
import btrplace.solver.choco.view.ChocoModelView;
import btrplace.solver.choco.view.ModelViewMapper;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.loop.monitors.SMF;
import solver.search.solution.AllSolutionsRecorder;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.selectors.values.RealDomainMiddle;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.selectors.variables.Occurrence;
import solver.search.strategy.strategy.AssignmentInterval;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.search.strategy.strategy.set.SetSearchStrategy;
import solver.search.strategy.strategy.set.SetValSelector;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;
import util.ESat;

import java.util.*;


/**
 * Default implementation of {@link ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblem implements ReconfigurationProblem {

    private static final Logger LOGGER = LoggerFactory.getLogger("ChocoRP");

    private boolean useLabels = false;

    private IntVar objective;
    /**
     * The maximum duration of a plan in seconds: One hour.
     */
    public static final int DEFAULT_MAX_TIME = 3600;

    private Model model;

    private Solver solver;

    private Set<VM> readys;
    private Set<VM> runnings;
    private Set<VM> sleepings;
    private Set<VM> killeds;

    private Set<VM> manageable;

    private VM[] vms;
    private TObjectIntHashMap<VM> revVMs;

    private Node[] nodes;
    private TObjectIntHashMap<Node> revNodes;

    private IntVar start;
    private IntVar end;

    private VMActionModel[] vmActions;
    private NodeActionModel[] nodeActions;

    private DurationEvaluators durEval;

    private Map<String, ChocoModelView> views;

    private IntVar[] vmsCountOnNodes;

    private SliceSchedulerBuilder taskSchedBuilder;

    private AliasedCumulativesBuilder cumulativesBuilder;

    private BinPackingBuilder bpBuilder;

    private ObjectiveAlterer alterer = new DefaultObjectiveAlterer();

    private ModelViewMapper viewMapper;

    private ResolutionPolicy solvingPolicy;

    /**
     * Make a new RP where the next state for every VM is indicated.
     * If the state for a VM is omitted, it is considered as unchanged
     *
     * @param m                  the initial model
     * @param dEval              to evaluate the duration of every action
     * @param ready              the VMs that must be in the ready state
     * @param running            the VMs that must be in the running state
     * @param sleeping           the VMs that must be in the sleeping state
     * @param label              {@code true} to label the variables (for debugging purpose)
     * @param killed             the VMs that must be killed
     * @param runningsToConsider the VMs that can be managed by the solver when they are already running and they must keep running
     * @throws SolverException if an error occurred
     * @see DefaultReconfigurationProblemBuilder to ease the instantiation process
     */
    public DefaultReconfigurationProblem(Model m,
                                         DurationEvaluators dEval,
                                         ModelViewMapper vMapper,
                                         Set<VM> ready,
                                         Set<VM> running,
                                         Set<VM> sleeping,
                                         Set<VM> killed,
                                         Set<VM> runningsToConsider,
                                         boolean label
    ) throws SolverException {
        this.readys = new HashSet<>(ready);
        this.runnings = new HashSet<>(running);
        this.sleepings = new HashSet<>(sleeping);
        this.killeds = new HashSet<>(killed);
        this.manageable = new HashSet<>(runningsToConsider);
        this.useLabels = label;
        model = m;
        durEval = dEval;
        this.viewMapper = vMapper;
        solver = new Solver();
        solver.getSearchLoop().plugSearchMonitor(new AllSolutionsRecorder(solver));
        start = VariableFactory.fixed("RP.start", 0, solver);
        end = VariableFactory.bounded("RP.end", 0, DEFAULT_MAX_TIME, solver);

        this.solvingPolicy = ResolutionPolicy.SATISFACTION;
        objective = null;
        this.views = new HashMap<>();

        fillElements();

        makeCardinalyVariables();

        makeNodeActionModels();
        makeVMActionModels();

        bpBuilder = new BinPackingBuilder(this);
        taskSchedBuilder = new SliceSchedulerBuilder(this);
        cumulativesBuilder = new AliasedCumulativesBuilder(this);

        makeViews();

        linkCardinatiesWithSlices();

    }

    @Override
    public ReconfigurationPlan solve(int timeLimit, boolean optimize) throws SolverException {

        if (!optimize) {
            solvingPolicy = ResolutionPolicy.SATISFACTION;
        }

        for (Map.Entry<String, ChocoModelView> cv : views.entrySet()) {
            if (!cv.getValue().beforeSolve(this)) {
                return null;
            }
        }

        try {
            bpBuilder.inject();
        } catch (ContradictionException ex) {
            throw new SolverException(model, ex.getMessage(), ex);
        }

        addContinuousResourceCapacities();


        if (nodeActions.length > 0) {
            solver.post(taskSchedBuilder.build());
        }

        for (AliasedCumulatives cstr : cumulativesBuilder.getConstraints()) {
            solver.post(cstr);
        }

        //Set the timeout
        if (timeLimit > 0) {
            SMF.limitTime(solver, timeLimit * 1000);
        }

        //Resolution policy:
        //mode: sat,min,max
        //allsolutions: false, true
        //timeout: 0, K

        //min, max -> !stop at first
        //sat -> stop || !stop
        appendNaiveBranchHeuristic();

        int nbIntVars = solver.retrieveIntVars().length;
        int nbCstrs = solver.getNbCstrs();


        getLogger().debug("{} constraints; {} integers", nbCstrs, nbIntVars);
        if (solvingPolicy == ResolutionPolicy.SATISFACTION) {
            solver.findSolution();
        } else {
            solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
                @Override
                public void onSolution() {
                    int v = objective.getValue();
                    String op = solvingPolicy == ResolutionPolicy.MAXIMIZE ? ">=" : "<=";
                    solver.postCut(IntConstraintFactory.arithm(objective, op, alterer.newBound(DefaultReconfigurationProblem.this, v)));
                }
            });
            solver.findOptimalSolution(solvingPolicy, objective);
        }
        return makeResultingPlan();
    }

    private ReconfigurationPlan makeResultingPlan() throws SolverException {

        //Check for the solution
        ESat status = solver.isFeasible();
        if (status == ESat.FALSE) {
            //It is certain the CSP has no solution
            return null;
        } else if (solver.isFeasible() == ESat.UNDEFINED) {
            //We don't know if the CSP has a solution
            throw new SolverException(model, "Unable to state about the problem feasibility.");
        }

        DefaultReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        for (ActionModel action : nodeActions) {
            action.insertActions(plan);
        }

        for (ActionModel action : vmActions) {
            action.insertActions(plan);
        }

        for (ChocoModelView view : views.values()) {
            view.insertActions(this, plan);
        }

        assert plan.isApplyable() : "The following plan cannot be applied:\n" + plan;
        assert checkConsistency(plan);
        return plan;
    }

    /**
     * A naive heuristic to be sure every variables will be instantiated.
     * In practice, instantiate each of the variables to its lower-bound
     */
    private void appendNaiveBranchHeuristic() {


        StrategiesSequencer seq;
        if (solver.getSearchLoop().getStrategy() == null) {
            seq = new StrategiesSequencer(
                    IntStrategyFactory.firstFail_InDomainMin(solver.retrieveIntVars()));

        } else {
            seq = new StrategiesSequencer(solver.getSearchLoop().getStrategy(),
                    IntStrategyFactory.firstFail_InDomainMin(solver.retrieveIntVars()));
        }
        RealVar[] rv = solver.retrieveRealVars();
        if (rv != null && rv.length > 0) {
            seq = new StrategiesSequencer(seq, new AssignmentInterval(rv, new Occurrence<>(solver.retrieveRealVars()), new RealDomainMiddle()));
        }
        SetVar[] sv = solver.retrieveSetVars();
        if (sv != null && sv.length > 0) {
            seq = new StrategiesSequencer(seq,
                    new SetSearchStrategy(new InputOrder<>(solver.retrieveSetVars()), new SetValSelector.FirstVal(), true));
        }
        solver.set(seq);
    }

    private void addContinuousResourceCapacities() {
        TIntArrayList cUse = new TIntArrayList();
        List<IntVar> iUse = new ArrayList<>();
        for (int j = 0; j < getVMs().length; j++) {
            VMActionModel a = vmActions[j];
            if (a.getDSlice() != null) {
                iUse.add(VariableFactory.one(solver));
            }
            if (a.getCSlice() != null) {
                cUse.add(1);
            }
        }

        taskSchedBuilder.add(getNbRunningVMs(),
                cUse.toArray(),
                iUse.toArray(new IntVar[iUse.size()]));
    }

    /**
     * Create the {@link ChocoModelView} for each of the {@link ModelView}.
     *
     * @throws SolverException if an error occurred
     */
    private void makeViews() throws SolverException {
        views = new HashMap<>(model.getViews().size());
        for (ModelView rc : model.getViews()) {
            ChocoModelView vv = viewMapper.map(this, rc);
            if (vv != null) {
                ChocoModelView in = views.put(vv.getIdentifier(), vv);
                if (in != null) {
                    throw new SolverException(model, "Cannot use the implementation '" + vv.getIdentifier() +
                            "' implementation for '" + rc.getIdentifier() + "'."
                            + "The '" + in.getIdentifier() + "' implementation is already used");
                }
            } else {
                LOGGER.debug("No implementation available for the view '{}'", rc.getIdentifier());
            }
        }
    }

    /**
     * Create the cardinality variables.
     */
    private void makeCardinalyVariables() {
        vmsCountOnNodes = new IntVar[nodes.length];
        int nbVMs = vms.length;
        for (int i = 0; i < vmsCountOnNodes.length; i++) {
            vmsCountOnNodes[i] = VariableFactory.bounded(makeVarLabel("nbVMsOn('", nodes[i], "')"), 0, nbVMs, solver);
        }
    }

    private void linkCardinatiesWithSlices() {
        IntVar[] ds = SliceUtils.extractHoster(ActionModelUtils.getDSlices(vmActions));
        IntVar[] usages = new IntVar[ds.length];
        for (int i = 0; i < ds.length; i++) {
            usages[i] = VariableFactory.one(solver);
        }
        bpBuilder.add("vmsOnNodes", vmsCountOnNodes, usages, ds);
    }

    private void fillElements() {

        Set<VM> allVMs = new HashSet<>();
        allVMs.addAll(model.getMapping().getSleepingVMs());
        allVMs.addAll(model.getMapping().getRunningVMs());
        allVMs.addAll(model.getMapping().getReadyVMs());
        //We have to integrate VMs in the ready state: the only VMs that may not appear in the mapping
        allVMs.addAll(readys);

        vms = new VM[allVMs.size()];
        //0.5f is a default load factor in trove.
        revVMs = new TObjectIntHashMap<>(allVMs.size(), 0.5f, -1);

        int i = 0;
        for (VM vm : allVMs) {
            vms[i] = vm;
            revVMs.put(vm, i++);
        }

        nodes = new Node[model.getMapping().getOnlineNodes().size() + model.getMapping().getOfflineNodes().size()];
        revNodes = new TObjectIntHashMap<>(nodes.length, 0.5f, -1);
        i = 0;
        for (Node n : model.getMapping().getOnlineNodes()) {
            nodes[i] = n;
            revNodes.put(n, i++);
        }
        for (Node n : model.getMapping().getOfflineNodes()) {
            nodes[i] = n;
            revNodes.put(n, i++);
        }
    }

    private void makeVMActionModels() throws SolverException {
        Mapping map = model.getMapping();
        vmActions = new VMActionModel[vms.length];
        for (int i = 0; i < vms.length; i++) {
            VM vmId = vms[i];
            if (runnings.contains(vmId)) {
                if (map.isSleeping(vmId)) {
                    vmActions[i] = new ResumeVMModel(this, vmId);
                    manageable.add(vmId);
                } else if (map.isRunning(vmId)) {
                    if (manageable.contains(vmId)) {
                        vmActions[i] = new RelocatableVMModel(this, vmId);
                    } else {
                        vmActions[i] = new StayRunningVMModel(this, vmId);
                    }
                } else if (map.isReady(vmId)) {
                    vmActions[i] = new BootVMModel(this, vmId);
                    manageable.add(vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' running: not ready");
                }
            }
            if (readys.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (!map.contains(vmId)) {
                    vmActions[i] = new ForgeVMModel(this, vmId);
                    manageable.add(vmId);
                } else if (map.isReady(vmId)) {
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else if (map.isRunning(vmId)) {
                    vmActions[i] = new ShutdownVMModel(this, vmId);
                    manageable.add(vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' ready: not in the 'running' state or already forged");
                }
            }
            if (sleepings.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (map.isRunning(vmId)) {
                    vmActions[i] = new SuspendVMModel(this, vmId);
                    manageable.add(vmId);
                } else if (map.isSleeping(vmId)) {
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' sleeping: should be running");
                }
            }
            if (killeds.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (map.contains(vmId)) {
                    vmActions[i] = new KillVMActionModel(this, vmId);
                    manageable.add(vmId);
                } else {
                    throw new SolverException(model, "Unable to kill VM '" + vmId + "': unknown");
                }
            }
            if (vmActions[i] == null) {
                //Next state is undefined, keep the current state
                //Need to update runnings, sleeping and waitings accordingly
                if (map.isRunning(vmId)) {
                    runnings.add(vmId);
                    if (manageable.contains(vmId)) {
                        vmActions[i] = new RelocatableVMModel(this, vmId);
                    } else {
                        vmActions[i] = new StayRunningVMModel(this, vmId);
                    }
                } else if (map.isReady(vmId)) {
                    readys.add(vmId);
                    vmActions[i] = new StayAwayVMModel(this, vmId);
                } else if (map.isSleeping(vmId)) {
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
            Node nId = nodes[i];
            if (m.getOfflineNodes().contains(nId)) {
                nodeActions[i] = new BootableNodeModel(this, nId);
            }
            if (m.isOnline(nId)) {
                if (nodeActions[i] != null) {
                    throw new SolverException(model, "Next state for node '" + nId + "' is ambiguous");
                }
                nodeActions[i] = new ShutdownableNodeModel(this, nId);
            }
        }
    }

    @Override
    public int getCurrentVMLocation(int vmIdx) {
        VM id = getVM(vmIdx);
        if (id == null) {
            return -1;
        }
        Node nodeId = model.getMapping().getVMLocation(id);
        return nodeId == null ? -1 : getNode(nodeId);
    }

    private boolean checkConsistency(ReconfigurationPlan p) {
        if (p.getDuration() != end.getValue()) {
            LOGGER.error("The plan effective duration ({}) and the computed duration ({}) mismatch", p.getDuration(), end.getValue());
            return false;
        }
        return true;
    }

    @Override
    public IntVar[] getNbRunningVMs() {
        return vmsCountOnNodes;
    }

    @Override
    public ChocoModelView getView(String id) {
        return views.get(id);
    }

    @Override
    public Collection<ChocoModelView> getViews() {
        return views.values();
    }

    @Override
    public boolean addView(ChocoModelView v) {
        if (views.containsKey(v.getIdentifier())) {
            return false;
        }
        views.put(v.getIdentifier(), v);
        return true;
    }

    @Override
    public Solver getSolver() {
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
    public BinPackingBuilder getBinPackingBuilder() {
        return bpBuilder;
    }

    @Override
    public IntVar makeHostVariable(Object... n) {
        String str = "";
        if (useLabels) {
            StringBuilder b = new StringBuilder();
            for (Object o : n) {
                b.append(o);
            }
            str = b.toString();
        }
        return VariableFactory.enumerated(str, 0, nodes.length - 1, solver);
    }

    @Override
    public IntVar makeCurrentHost(String n, VM vmId) throws SolverException {
        int idx = getVM(vmId);
        if (idx < 0) {
            throw new SolverException(model, "Unknown VM '" + vmId + "'");
        }
        return makeCurrentNode(useLabels ? n : "", model.getMapping().getVMLocation(vmId));
    }

    @Override
    public IntVar makeCurrentNode(String n, Node nId) throws SolverException {
        int idx = getNode(nId);
        if (idx < 0) {
            throw new SolverException(model, "Unknown node '" + nId + "'");
        }
        return VariableFactory.fixed(useLabels ? n : "", idx, solver);
    }

    @Override
    public IntVar makeUnboundedDuration(Object... n) {
        String str = "";
        if (useLabels) {
            StringBuilder b = new StringBuilder();
            for (Object s : n) {
                b.append(s);
            }
            str = b.toString();
        }
        return VariableFactory.bounded(str, 0, end.getUB(), solver);
    }

    @Override
    public IntVar makeDuration(int ub, int lb, Object... n) throws SolverException {
        if (lb < 0 || ub < lb) {
            throw new SolverException(model, "Unable to create duration variable '" + Arrays.toString(n) + "': invalid bounds");
        }
        String s = "";
        if (useLabels) {
            StringBuilder b = new StringBuilder();
            for (Object o : n) {
                b.append(o);
            }
            s = b.toString();
        }
        return VariableFactory.bounded(s, lb, ub < end.getUB() ? ub : end.getUB(), solver);
    }

    @Override
    public String makeVarLabel(Object... lbl) {
        if (useLabels) {
            StringBuilder b = new StringBuilder();
            for (Object s : lbl) {
                b.append(s);
            }
            return b.toString();
        }
        return "";
    }

    @Override
    public boolean isVarLabelling() {
        return useLabels;
    }

    @Override
    public Set<VM> getManageableVMs() {
        return manageable;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public ObjectiveAlterer getObjectiveAlterer() {
        return alterer;
    }

    @Override
    public void setObjectiveAlterer(ObjectiveAlterer a) {
        alterer = a;
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
    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public VM[] getVMs() {
        return vms;
    }

    @Override
    public Model getSourceModel() {
        return model;
    }

    @Override
    public Set<VM> getFutureRunningVMs() {
        return runnings;
    }

    @Override
    public Set<VM> getFutureReadyVMs() {
        return readys;
    }

    @Override
    public Set<VM> getFutureSleepingVMs() {
        return sleepings;
    }

    @Override
    public Set<VM> getFutureKilledVMs() {
        return killeds;
    }

    @Override
    public IntVar getStart() {
        return start;
    }

    @Override
    public IntVar getEnd() {
        return end;
    }

    @Override
    public int getVM(VM vm) {
        return revVMs.get(vm);
    }

    @Override
    public VM getVM(int idx) {
        return vms[idx];
    }

    @Override
    public int getNode(Node n) {
        return revNodes.get(n);
    }

    @Override
    public Node getNode(int idx) {
        return nodes[idx];
    }

    @Override
    public VMActionModel[] getVMActions() {
        return vmActions;
    }

    @Override
    public VMActionModel[] getVMActions(Set<VM> id) {
        return vmActions;
    }

    @Override
    public VMActionModel getVMAction(VM id) {
        int idx = getVM(id);
        return idx < 0 ? null : vmActions[idx];
    }

    @Override
    public NodeActionModel getNodeAction(Node id) {
        int idx = getNode(id);
        return idx < 0 ? null : nodeActions[idx];
    }

    @Override
    public ModelViewMapper getViewMapper() {
        return viewMapper;
    }

    @Override
    public VM cloneVM(VM vm) {
        VM newVM = model.newVM();
        if (newVM == null) {
            return newVM;
        }
        for (ChocoModelView v : views.values()) {
            v.cloneVM(vm, newVM);
        }
        return newVM;
    }

    @Override
    public IntVar getObjective() {
        return objective;
    }

    @Override
    public void setObjective(boolean b, IntVar v) {
        this.objective = v;
        this.solvingPolicy = b ? ResolutionPolicy.MINIMIZE : ResolutionPolicy.MAXIMIZE;
    }

    @Override
    public ResolutionPolicy getResolutionPolicy() {
        return this.solvingPolicy;
    }
}
