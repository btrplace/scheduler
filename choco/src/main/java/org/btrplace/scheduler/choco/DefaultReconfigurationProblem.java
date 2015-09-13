
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

package org.btrplace.scheduler.choco;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.btrplace.model.*;
import org.btrplace.model.view.ModelView;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.*;
import org.btrplace.scheduler.choco.view.*;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.solution.AllSolutionsRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.Occurrence;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.search.strategy.strategy.SetStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Default implementation of {@link ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblem implements ReconfigurationProblem {

    /**
     * The maximum duration of a plan in seconds: One hour.
     */
    public static final int DEFAULT_MAX_TIME = 3600;
    private static final Logger LOGGER = LoggerFactory.getLogger("ChocoRP");
    private boolean useLabels = false;
    private IntVar objective;
    private Model model;

    private Solver solver;

    private Set<VM> ready;
    private Set<VM> running;
    private Set<VM> sleeping;
    private Set<VM> killed;

    private Set<VM> manageable;

    private VM[] vms;
    private TObjectIntHashMap<VM> revVMs;

    private Node[] nodes;
    private TObjectIntHashMap<Node> revNodes;

    private IntVar start;
    private IntVar end;

    private VMTransition[] vmActions;
    private NodeTransition[] nodeActions;

    private DurationEvaluators durEval;

    private IntVar[] vmsCountOnNodes;

    private ObjectiveAlterer alterer = new DefaultObjectiveAlterer();

    private ResolutionPolicy solvingPolicy;

    private TransitionFactory amFactory;

    private SolverViewsManager viewsManager;

    /**
     * Make a new RP where the next state for every VM is indicated.
     * If the state for a VM is omitted, it is considered as unchanged
     *
     * @param m         the initial model
     * @param ps        parameters to customize the problem
     * @param ready     the VMs that must be in the ready state
     * @param running   the VMs that must be in the running state
     * @param sleeping  the VMs that must be in the sleeping state
     * @param killed    the VMs that must be killed
     * @param preRooted the VMs that can be managed by the solver when they are already running and they must keep running
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     * @see DefaultReconfigurationProblemBuilder to ease the instantiation process
     */
    public DefaultReconfigurationProblem(Model m,
                                         Parameters ps,
                                         Set<VM> ready,
                                         Set<VM> running,
                                         Set<VM> sleeping,
                                         Set<VM> killed,
                                         Set<VM> preRooted) throws SchedulerException {
        this.ready = new HashSet<>(ready);
        this.running = new HashSet<>(running);
        this.sleeping = new HashSet<>(sleeping);
        this.killed = new HashSet<>(killed);
        this.manageable = new HashSet<>(preRooted);
        this.useLabels = ps.getVerbosity() > 0;
        this.amFactory = ps.getTransitionFactory();
        model = m;
        durEval = ps.getDurationEvaluators();

        solver = new Solver();
        solver.set(new AllSolutionsRecorder(solver));
        start = VariableFactory.fixed(makeVarLabel("RP.start"), 0, solver);
        end = VariableFactory.bounded(makeVarLabel("RP.end"), 0, DEFAULT_MAX_TIME, solver);

        this.solvingPolicy = ResolutionPolicy.SATISFACTION;
        objective = null;


        fillElements();

        makeCardinalityVariables();

        makeNodeTransitions();
        makeVMTransitions();

        makeViews(ps);
        linkCardinalityWithSlices();

    }

    private void makeViews(Parameters ps) throws SchedulerException {
        List<SolverViewBuilder> viewBuilders = new ArrayList<>(ps.getSolverViews());
        ModelViewMapper vm = ps.getViewMapper();
        for (ModelView v : model.getViews()) {
            ChocoModelViewBuilder modelViewBuilder = vm.getBuilder(v.getClass());
            if (modelViewBuilder != null) {
                SolverViewBuilder sb = modelViewBuilder.build(v);
                viewBuilders.add(sb);
            }
        }
        viewsManager = new SolverViewsManager(this);
        viewsManager.build(viewBuilders);
    }

    @Override
    public ReconfigurationPlan solve(int timeLimit, boolean optimize) throws SchedulerException {

        if (!optimize) {
            solvingPolicy = ResolutionPolicy.SATISFACTION;
        }
        addContinuousResourceCapacities();

        if (!viewsManager.beforeSolve()) {
            return null;
        }

        //Set the timeout
        if (timeLimit > 0) {
            SMF.limitTime(solver, timeLimit * 1000);
        }

        appendNaiveBranchHeuristic();

        getLogger().debug("{} constraints; {} integers", solver.getNbCstrs(), solver.retrieveIntVars().length + solver.retrieveBoolVars().length);


        if (solvingPolicy == ResolutionPolicy.SATISFACTION) {
            solver.findSolution();
        } else {
            solver.getSearchLoop().plugSearchMonitor((IMonitorSolution) () -> {
                int v = objective.getValue();
                String op = solvingPolicy == ResolutionPolicy.MAXIMIZE ? ">=" : "<=";
                solver.post(IntConstraintFactory.arithm(objective, op, alterer.newBound(DefaultReconfigurationProblem.this, v)));
            });
            if (!solver.getObjectiveManager().isOptimization()) {
                solver.set(new ObjectiveManager<IntVar, Integer>(objective, solvingPolicy, true));
            }
            solver.findOptimalSolution(solvingPolicy, objective);
        }
        return makeResultingPlan();
    }

    @Override
    public List<ReconfigurationPlan> getComputedSolutions() throws SchedulerException {
        List<ReconfigurationPlan> plans = new ArrayList<>();
        for (Solution s : solver.getSolutionRecorder().getSolutions()) {
            plans.add(buildReconfigurationPlan(s, model));
        }
        return plans;
    }

    private ReconfigurationPlan makeResultingPlan() throws SchedulerException {

        //Check for the solution
        ESat status = solver.isFeasible();
        if (status == ESat.FALSE) {
            //It is certain the CSP has no solution
            return null;
        } else if (solver.isFeasible() == ESat.UNDEFINED) {
            //We don't know if the CSP has a solution
            throw new SchedulerException(model, "Unable to state about the problem feasibility.");
        }

        Solution s = solver.getSolutionRecorder().getLastSolution();
        return buildReconfigurationPlan(s, model.clone());
    }

    /**
     * Build a plan for a solution.
     * @param s the solution
     * @param src the source model
     * @return the resulting plan
     * @throws SchedulerException if a error occurred
     */
    public ReconfigurationPlan buildReconfigurationPlan(Solution s, Model src) throws SchedulerException {
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(src);
        for (Transition action : nodeActions) {
            action.insertActions(s, plan);
        }

        for (Transition action : vmActions) {
            action.insertActions(s, plan);
        }

        viewsManager.insertActions(s, plan);

        assert plan.isApplyable() : "The following plan cannot be applied:\n" + plan;
        assert checkConsistency(s, plan);
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
                    ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), ArrayUtils.append(solver.retrieveBoolVars(), solver.retrieveIntVars())));

        } else {
            seq = new StrategiesSequencer(
                    solver.getSearchLoop().getStrategy(),
                    ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), ArrayUtils.append(solver.retrieveBoolVars(), solver.retrieveIntVars())));
        }
        RealVar[] rv = solver.retrieveRealVars();
        if (rv != null && rv.length > 0) {
            seq = new StrategiesSequencer(
                    seq,
                    new RealStrategy(rv, new Occurrence<>(), new RealDomainMiddle()));
        }
        SetVar[] sv = solver.retrieveSetVars();
        if (sv != null && sv.length > 0) {
            seq = new StrategiesSequencer(
                    seq,
                    new SetStrategy(sv, new InputOrder<>(), new SetDomainMin(), true));
        }
        solver.set(seq);
    }

    private void addContinuousResourceCapacities() throws SchedulerException {
        TIntArrayList cUse = new TIntArrayList();
        List<IntVar> iUse = new ArrayList<>();
        for (int j = 0; j < getVMs().length; j++) {
            VMTransition a = vmActions[j];
            if (a.getDSlice() != null) {
                iUse.add(VariableFactory.one(solver));
            }
            if (a.getCSlice() != null) {
                cUse.add(1);
            }
        }

        ChocoView v = getView(Cumulatives.VIEW_ID);
        if (v == null) {
            throw new SchedulerException(model, "View '" + Cumulatives.VIEW_ID + "' is required but missing");
        }
        ((Cumulatives) v).addDim(getNbRunningVMs(), cUse.toArray(), iUse.toArray(new IntVar[iUse.size()]));
    }

    /**
     * Create the cardinality variables.
     */
    private void makeCardinalityVariables() {
        vmsCountOnNodes = new IntVar[nodes.length];
        int nbVMs = vms.length;
        for (int i = 0; i < vmsCountOnNodes.length; i++) {
            vmsCountOnNodes[i] = VariableFactory.bounded(makeVarLabel("nbVMsOn('", nodes[i], "')"), 0, nbVMs, solver);
        }
    }

    private void linkCardinalityWithSlices() throws SchedulerException {
        IntVar[] ds = SliceUtils.extractHoster(TransitionUtils.getDSlices(vmActions));
        IntVar[] usages = new IntVar[ds.length];
        for (int i = 0; i < ds.length; i++) {
            usages[i] = VariableFactory.one(solver);
        }
        ChocoView v = getView(Packing.VIEW_ID);
        if (v == null) {
            throw new SchedulerException(model, "View '" + Packing.VIEW_ID + "' is required but missing");
        }
        ((Packing) v).addDim("vmsOnNodes", vmsCountOnNodes, usages, ds);
    }

    @Override
    public final VMState getFutureState(VM v) {
        VMTransition t = getVMAction(v);
        return t == null ? null : t.getFutureState();
    }


    @Override
    public VMState getSourceState(VM v) {
        VMTransition t = getVMAction(v);
        return t == null ? null : t.getSourceState();
    }

    @Override
    public NodeState getSourceState(Node n) {
        NodeTransition t = getNodeAction(n);
        return t == null ? null : t.getSourceState();
    }

    private void fillElements() {
        Set<VM> allVMs = new HashSet<>();
        allVMs.addAll(model.getMapping().getSleepingVMs());
        allVMs.addAll(model.getMapping().getRunningVMs());
        allVMs.addAll(model.getMapping().getReadyVMs());
        //We have to integrate VMs in the ready state: the only VMs that may not appear in the mapping
        allVMs.addAll(ready);

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

    private void makeVMTransitions() throws SchedulerException {
        Mapping map = model.getMapping();
        vmActions = new VMTransition[vms.length];
        for (int i = 0; i < vms.length; i++) {
            VM vmId = vms[i];
            VMState curState = VMState.INIT;
            if (map.isRunning(vmId)) {
                curState = VMState.RUNNING;
            } else if (map.isSleeping(vmId)) {
                curState = VMState.SLEEPING;
            } else if (map.isReady(vmId)) {
                curState = VMState.READY;
            }

            VMState nextState;
            if (running.contains(vmId)) {
                nextState = VMState.RUNNING;
            } else if (sleeping.contains(vmId)) {
                nextState = VMState.SLEEPING;
            } else if (ready.contains(vmId)) {
                nextState = VMState.READY;
            } else if (killed.contains(vmId)) {
                nextState = VMState.KILLED;
            } else {
                nextState = curState; //by default, maintain state
                switch(nextState) {
                    case READY: ready.add(vmId);break;
                    case RUNNING: running.add(vmId); break;
                    case SLEEPING: sleeping.add(vmId); break;
                }
            }

            VMTransitionBuilder am = amFactory.getBuilder(curState, nextState);
            if (am == null) {
                throw new SchedulerException(model, "No model available for VM transition " + curState + " -> " + nextState);
            }
            vmActions[i] = am.build(this, vmId);
            if (vmActions[i].isManaged()) {
                manageable.add(vmId);
            }
        }
    }

    private void makeNodeTransitions() throws SchedulerException {

        Mapping m = model.getMapping();
        nodeActions = new NodeTransition[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            Node nId = nodes[i];
            NodeState state = m.getOfflineNodes().contains(nId) ? NodeState.OFFLINE : NodeState.ONLINE;
            NodeTransitionBuilder b = amFactory.getBuilder(state);
            if (b == null) {
                throw new SchedulerException(model, "No model available for a node transition " + state + " -> (offline|online)");
            }
            nodeActions[i] = b.build(this, nId);
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

    private boolean checkConsistency(Solution s, ReconfigurationPlan p) {
        if (p.getDuration() != s.getIntVal(end)) {
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
    public final ChocoView getView(String id) {
        return viewsManager.get(id);
    }

    @Override
    public Collection<String> getViews() {
        return viewsManager.getKeys();
    }

    @Override
    public boolean addView(ChocoView v) {
        return viewsManager.add(v);
    }

    @Override
    public Solver getSolver() {
        return solver;
    }

    @Override
    public IntVar makeHostVariable(Object... n) {
        return VariableFactory.enumerated(makeVarLabel(n), 0, nodes.length - 1, solver);
    }

    @Override
    public IntVar makeCurrentHost(VM vmId, Object... n) throws SchedulerException {
        int idx = getVM(vmId);
        if (idx < 0) {
            throw new SchedulerException(model, "Unknown VM '" + vmId + "'");
        }
        return makeCurrentNode(model.getMapping().getVMLocation(vmId), useLabels ? n : "");
    }

    @Override
    public IntVar makeCurrentNode(Node nId, Object... n) throws SchedulerException {
        int idx = getNode(nId);
        if (idx < 0) {
            throw new SchedulerException(model, "Unknown node '" + nId + "'");
        }
        if (useLabels) {
            return VariableFactory.fixed(makeVarLabel(n), idx, solver);
        }
        return VariableFactory.fixed("cste -- ", idx, solver);
    }

    @Override
    public IntVar makeUnboundedDuration(Object... n) {
        return VariableFactory.bounded(makeVarLabel(n), 0, end.getUB(), solver);
    }

    @Override
    public IntVar makeDuration(int ub, int lb, Object... n) throws SchedulerException {
        return VariableFactory.bounded(makeVarLabel(n), lb, ub, solver);
    }

    @Override
    public final String makeVarLabel(Object... lbl) {
        if (useLabels) {
            StringBuilder b = new StringBuilder();
            for (Object s : lbl) {
                if (s instanceof Object[]) {
                    for (Object x : (Object[]) s) {
                        b.append(x);
                    }
                } else {
                    b.append(s);
                }
            }
            return b.toString();
        }
        return "";
    }

    @Override
    public Set<VM> getManageableVMs() {
        return Collections.unmodifiableSet(manageable);
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
    public NodeTransition[] getNodeActions() {
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
        return running;
    }

    @Override
    public Set<VM> getFutureReadyVMs() {
        return ready;
    }

    @Override
    public Set<VM> getFutureSleepingVMs() {
        return sleeping;
    }

    @Override
    public Set<VM> getFutureKilledVMs() {
        return killed;
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
    public VMTransition[] getVMActions() {
        return vmActions;
    }

    @Override
    public VMTransition[] getVMActions(Collection<VM> ids) {
        VMTransition[] trans = new VMTransition[ids.size()];
        int i = 0;
        for (VM v : ids) {
            trans[i++] = getVMAction(v);
        }
        return trans;
    }

    @Override
    public VMTransition getVMAction(VM id) {
        int idx = getVM(id);
        return idx < 0 ? null : vmActions[idx];
    }

    @Override
    public NodeTransition getNodeAction(Node id) {
        int idx = getNode(id);
        return idx < 0 ? null : nodeActions[idx];
    }

    @Override
    public VM cloneVM(VM vm) {
        VM newVM = model.newVM();
        if (newVM == null) {
            return null;
        }
        viewsManager.cloneVM(vm, newVM);
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
