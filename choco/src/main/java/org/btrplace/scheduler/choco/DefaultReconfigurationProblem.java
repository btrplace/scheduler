/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.btrplace.model.*;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.InconsistentSolutionException;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.SchedulerModelingException;
import org.btrplace.scheduler.UnstatableProblemException;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.*;
import org.btrplace.scheduler.choco.view.AliasedCumulatives;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.btrplace.scheduler.choco.view.Cumulatives;
import org.btrplace.scheduler.choco.view.Packing;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.Occurrence;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.search.strategy.strategy.SetStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Default implementation of {@link ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblem implements ReconfigurationProblem {

    private static final Logger LOGGER = LoggerFactory.getLogger("ChocoRP");
    private boolean useLabels = false;
    private IntVar objective;
    private final Model model;

    private final Solver solver;
    private final org.chocosolver.solver.Model csp;

    private final Set<VM> ready;
    private final Set<VM> running;
    private final Set<VM> sleeping;
    private final Set<VM> killed;

    private Set<VM> manageable;

    private List<VM> vms;
    private TObjectIntHashMap<VM> revVMs;

    private List<Node> nodes;
    private TObjectIntHashMap<Node> revNodes;

    private final IntVar start;
    private final IntVar end;

    private List<VMTransition> vmActions;
    private List<NodeTransition> nodeActions;

    private final DurationEvaluators durEval;

    private List<IntVar> vmsCountOnNodes;


    private ResolutionPolicy solvingPolicy;

    private final TransitionFactory amFactory;

    private final Map<String, ChocoView> coreViews;

    private final List<Solution> solutions;

    private final StopButton stopButton;

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
    DefaultReconfigurationProblem(Model m,
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

        IEnvironment env = ps.getEnvironmentFactory().build(m);
        csp = new org.chocosolver.solver.Model(env, "", ps.chocoSettings());
        solver = csp.getSolver();
        start = fixed(0, "RP.start");
        end = csp.intVar(makeVarLabel("RP.end"), 0, ps.getMaxEnd(), true);

        this.solvingPolicy = ResolutionPolicy.SATISFACTION;
        objective = null;

        this.solutions = new ArrayList<>();

        fillElements();

        makeCardinalityVariables();

        makeNodeTransitions();
        makeVMTransitions();
        manageable = Collections.unmodifiableSet(manageable);
        coreViews = new HashMap<>();
        for (Class<? extends ChocoView> c : ps.getChocoViews()) {
            try {
                ChocoView v = c.newInstance();
                v.inject(ps, this);
                addView(v);
            } catch (Exception e) {
                throw new SchedulerModelingException(model, "Unable to instantiate solver-only view '" + c.getSimpleName() + "'", e);
            }
        }

        stopButton = new StopButton();
        solver.addStopCriterion(stopButton);
    }

    @Override
    public ReconfigurationPlan solve(int timeLimit, boolean optimize) throws SchedulerException {

        //Check for multiple destination state
        if (!distinctVMStates()) {
            return null;
        }

        if (!optimize) {
            solvingPolicy = ResolutionPolicy.SATISFACTION;
        }
        linkCardinalityWithSlices();
        addContinuousResourceCapacities();
        getRequiredView(Packing.VIEW_ID).beforeSolve(this);
        getRequiredView(Cumulatives.VIEW_ID).beforeSolve(this);
        getRequiredView(AliasedCumulatives.VIEW_ID).beforeSolve(this);

        //Set the timeout
        if (timeLimit > 0) {
            solver.limitTime(timeLimit * 1000L);
        }

        if (solver.getSearch() == null) {
            defaultHeuristic();
        }

        solver.plugMonitor((IMonitorSolution) () -> {
            Solution s = new Solution(csp);
            s.record();
            solutions.add(s);
        });

        if (solvingPolicy == ResolutionPolicy.SATISFACTION) {
            solver.findSolution();
        } else {
            solver.findOptimalSolution(objective, solvingPolicy.equals(ResolutionPolicy.MAXIMIZE));
        }

        if (solver.isFeasible() == ESat.UNDEFINED) {
            //We don't know if the CSP has a solution
            throw new UnstatableProblemException(model, timeLimit);
        }
        return makeResultingPlan();
    }

    /**
     * Check if every VM has a single destination state
     *
     * @return {@code true} if states are distinct
     */
    private boolean distinctVMStates() {

        boolean ok = vms.size() == running.size() + sleeping.size() + ready.size() + killed.size();


        //It is sure there is no solution as a VM cannot have multiple destination state
        Map<VM, VMState> states = new HashMap<>();
        for (VM v : running) {
            states.put(v, VMState.RUNNING);
        }
        for (VM v : ready) {
            VMState prev = states.put(v, VMState.READY);
            if (prev != null) {
                getLogger().debug("multiple destination state for {}: {} and {}", v, prev, VMState.READY);
            }
        }
        for (VM v : sleeping) {
            VMState prev = states.put(v, VMState.SLEEPING);
            if (prev != null) {
                getLogger().debug("multiple destination state for {}: {} and {}", v, prev, VMState.SLEEPING);
            }
        }
        for (VM v : killed) {
            VMState prev = states.put(v, VMState.KILLED);
            if (prev != null) {
                getLogger().debug("multiple destination state for {}: {} and {}", v, prev, VMState.KILLED);
            }
        }
        return ok;
    }

    @Override
    public List<ReconfigurationPlan> getComputedSolutions() throws SchedulerException {
        return solutions.stream()
                .map(s -> buildReconfigurationPlan(s, model))
                .collect(Collectors.toList());
    }

    private ReconfigurationPlan makeResultingPlan() {

        //Check for the solution
        ESat status = solver.isFeasible();
        if (status == ESat.FALSE) {
            //It is certain the CSP has no solution
            return null;
        }
        Solution s = null;
        if (!solutions.isEmpty()) {
            s = solutions.get(solutions.size() - 1);
        }
        return buildReconfigurationPlan(s, model.copy());
    }

    /**
     * Build a plan for a solution.
     * @param s the solution
     * @param src the source model
     * @return the resulting plan
     * @throws SchedulerException if a error occurred
     */
    @Override
    @SuppressWarnings("squid:S3346")
    public ReconfigurationPlan buildReconfigurationPlan(Solution s, Model src) throws SchedulerException {
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(src);
        for (NodeTransition action : nodeActions) {
            action.insertActions(s, plan);
        }

        for (VMTransition action : vmActions) {
            action.insertActions(s, plan);
        }

        assert plan.isApplyable() : "The following plan cannot be applied:\n" + plan;
        assert checkConsistency(s, plan);
        return plan;
    }

    /**
     * A naive heuristic to be sure every variables will be instantiated.
     */
    private void defaultHeuristic() {
        IntStrategy intStrat = Search.intVarSearch(new FirstFail(csp), new IntDomainMin(), csp.retrieveIntVars(true));
        SetStrategy setStrat = new SetStrategy(csp.retrieveSetVars(), new InputOrder<>(csp), new SetDomainMin(), true);
      RealStrategy realStrat = new RealStrategy(csp.retrieveRealVars(), new Occurrence<>(), new RealDomainMiddle(), 1e-2, true);
        solver.setSearch(new StrategiesSequencer(intStrat, realStrat, setStrat));
    }

    private void addContinuousResourceCapacities() {
        TIntArrayList cUse = new TIntArrayList();
        List<IntVar> iUse = new ArrayList<>();
        for (int j = 0; j < getVMs().size(); j++) {
            VMTransition a = vmActions.get(j);
            if (a.getDSlice() != null) {
                iUse.add(csp.intVar(1));
            }
            if (a.getCSlice() != null) {
                cUse.add(1);
            }
        }

        ChocoView v = getView(Cumulatives.VIEW_ID);
        if (v == null) {
            throw SchedulerModelingException.missingView(model, Cumulatives.VIEW_ID);
        }

        ((Cumulatives) v).addDim(getNbRunningVMs(), cUse.toArray(), iUse.toArray(new IntVar[iUse.size()]));
    }

    private void linkCardinalityWithSlices() {
        Stream<Slice> s = vmActions.stream().map(VMTransition::getDSlice).filter(Objects::nonNull);
        IntVar[] ds = s.map(Slice::getHoster).toArray(IntVar[]::new);
        int[] usages = new int[ds.length];
        Arrays.fill(usages, 1);
        ChocoView v = getView(Packing.VIEW_ID);
        if (v == null) {
            throw SchedulerModelingException.missingView(model, Packing.VIEW_ID);
        }
        ((Packing) v).addDim("vmsOnNodes", vmsCountOnNodes, usages, ds);
    }

    /**
     * Create the cardinality variables.
     */
    private void makeCardinalityVariables() {
        vmsCountOnNodes = new ArrayList<>(nodes.size());
        int nbVMs = vms.size();
        for (Node n : nodes) {
            final IntVar card;
            if (useLabels) {
                card = csp.intVar(makeVarLabel("nbVMsOn('", n, "')"), 0, nbVMs, true);
            } else {
                card = csp.intVar("",0, nbVMs, true);
            }
            vmsCountOnNodes.add(card);
        }
        vmsCountOnNodes = Collections.unmodifiableList(vmsCountOnNodes);
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

        Set<VM> allVMs = new HashSet<>(model.getMapping().getNbVMs());
        allVMs.addAll(model.getMapping().getReadyVMs());
        allVMs.addAll(ready);
        for (final Node no : model.getMapping().getOnlineNodes()) {
            allVMs.addAll(model.getMapping().getRunningVMs(no));
            allVMs.addAll(model.getMapping().getSleepingVMs(no));
        }

        vms = new ArrayList<>(allVMs.size());

        //0.5f is a default load factor in trove.
        revVMs = new TObjectIntHashMap<>(allVMs.size(), 0.5f, -1);

        int i = 0;
        for (VM vm : allVMs) {
            vms.add(vm);
            revVMs.put(vm, i++);
        }
        vms = Collections.unmodifiableList(vms);
        nodes = new ArrayList<>();
        revNodes = new TObjectIntHashMap<>(nodes.size(), 0.5f, -1);
        i = 0;
        for (Node n : model.getMapping().getOnlineNodes()) {
            nodes.add(n);
            revNodes.put(n, i++);
        }
        for (Node n : model.getMapping().getOfflineNodes()) {
            nodes.add(n);
            revNodes.put(n, i++);
        }
        nodes = Collections.unmodifiableList(nodes);
    }

    private void makeVMTransitions() {
        Mapping map = model.getMapping();
        vmActions = new ArrayList<>(vms.size());
        for (VM vmId : vms) {
            VMState curState = map.getState(vmId);
            if (curState == null) {
                curState = VMState.INIT;
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
                    case READY:
                        ready.add(vmId);
                        break;
                    case RUNNING:
                        running.add(vmId);
                        break;
                    case SLEEPING:
                        sleeping.add(vmId);
                        break;
                    default:
                        throw new LifeCycleViolationException(model, vmId, curState, nextState);
                }
            }

            VMTransitionBuilder am = amFactory.getBuilder(curState, nextState);
            if (am == null) {
                throw new LifeCycleViolationException(model, vmId, curState, nextState);
            }
            VMTransition t = am.build(this, vmId);
            vmActions.add(t);
            if (t.isManaged()) {
                manageable.add(vmId);
            }
        }
    }

    private void makeNodeTransitions() {

        Mapping m = model.getMapping();
        nodeActions = new ArrayList<>(nodes.size());
        for (Node nId : nodes) {
            NodeState state = m.getState(nId);
            NodeTransitionBuilder b = amFactory.getBuilder(state);
            if (b == null) {
                throw new LifeCycleViolationException(model, nId, state, EnumSet.of(NodeState.OFFLINE, NodeState.ONLINE));
            }
            nodeActions.add(b.build(this, nId));
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
            String msg = String.format("The plan effective duration (%s) and the computed duration (%s) mismatch",
                    p.getDuration(), s.getIntVal(end));
            throw new InconsistentSolutionException(p, msg);
        }
        return true;
    }

    @Override
    public List<IntVar> getNbRunningVMs() {
        return vmsCountOnNodes;
    }

    @Override
    public final ChocoView getView(String id) {
        return coreViews.get(id);
    }

    @Override
    public Collection<String> getViews() {
        return coreViews.keySet();
    }

    @Override
    public boolean addView(ChocoView v) {
        if (coreViews.containsKey(v.getIdentifier())) {
            return false;
        }
        coreViews.put(v.getIdentifier(), v);
        return true;
    }

    @Override
    public Solver getSolver() {
        return solver;
    }

    @Override
    public org.chocosolver.solver.Model getModel() {
        return csp;
    }

    @Override
    public IntVar makeHostVariable(Object... n) {
        return csp.intVar(makeVarLabel(n), 0, nodes.size() - 1, false);
    }

    @Override
    public IntVar makeHostVariable() {
        return csp.intVar("", 0, nodes.size() - 1, false);
    }

    @Override
    public IntVar makeHostVariable(List<Node> candidates, Object... n) {
        if (candidates.size() == 1) {
            return makeCurrentNode(candidates.get(0), n);
        }
        final int[] ids = new int[candidates.size()];
        int idx = 0;
        for (final Node no : candidates) {
            ids[idx++] = getNode(no);
        }
        // We don't rely on csp.makeInt() as this may lead to unnecessary clone().
        final int[] sorted = ArrayUtils.mergeAndSortIfNot(ids);
        return new BitsetIntVarImpl(makeVarLabel(n), sorted, csp.ref());
    }

    @Override
    public IntVar makeCurrentHost(VM vmId, Object... n) throws SchedulerException {
        return makeCurrentNode(model.getMapping().getVMLocation(vmId), useLabels ? n : "");
    }

    @Override
    public IntVar makeCurrentHost(VM vmId) throws SchedulerException {
        return makeCurrentNode(model.getMapping().getVMLocation(vmId));
    }

    @Override
    public IntVar makeCurrentNode(Node nId, Object... n) throws SchedulerException {
        int idx = getNode(nId);
        if (idx < 0) {
            throw new SchedulerModelingException(model, "Unknown node '" + nId + "'");
        }
        if (useLabels) {
            return fixed(idx, makeVarLabel(n));
        }
        return csp.intVar(idx);
    }

    @Override
    public IntVar makeCurrentNode(Node nId) throws SchedulerException {
        int idx = getNode(nId);
        if (idx < 0) {
            throw new SchedulerModelingException(model, "Unknown node '" + nId + "'");
        }
        return csp.intVar(idx);
    }

    @Override
    public IntVar makeUnboundedDuration(Object... n) {
        return csp.intVar(makeVarLabel(n), 0, end.getUB(), true);
    }

    @Override
    public IntVar makeUnboundedDuration() {
        return csp.intVar("", 0, end.getUB(), true);
    }

    @Override
    public IntVar makeDuration(int ub, int lb, Object... n) throws SchedulerException {
        return csp.intVar(makeVarLabel(n), lb, ub, true);
    }

    @Override
    public IntVar makeDuration(int ub, int lb) throws SchedulerException {
        return csp.intVar("", lb, ub, true);
    }

    @Override
    public final String makeVarLabel(Object... lbl) {
        if (!useLabels) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (Object s : lbl) {
            if (s instanceof Object[]) {
                for (Object o : (Object[]) s) {
                    b.append(o);
                }
            } else {
                b.append(s);
            }
        }
        return b.toString();
    }

    @Override
    public final String makeVarLabel(Object lbl) {
        if (!useLabels) {
            return "";
        }
        return lbl.toString();
    }

    public boolean labelVariables() {
        return useLabels;
    }

    @Override
    public IntVar fixed(int v, Object... lbl) {
        if (useLabels) {
            return csp.intVar(makeVarLabel(lbl), v);
        }
        return csp.intVar(v);
    }

    @Override
    public IntVar fixed(int v, Object lbl) {
        if (useLabels) {
            return csp.intVar(lbl.toString(), v);
        }
        return csp.intVar(v);
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
    public List<NodeTransition> getNodeActions() {
        return nodeActions;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durEval;
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public List<VM> getVMs() {
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
        return vms.get(idx);
    }

    @Override
    public int getNode(Node n) {
        return revNodes.get(n);
    }

    @Override
    public Node getNode(int idx) {
        return nodes.get(idx);
    }

    @Override
    public List<VMTransition> getVMActions() {
        return vmActions;
    }

    @Override
    public List<VMTransition> getVMActions(Collection<VM> ids) {
        List<VMTransition> trans = new ArrayList<>(ids.size());
        for (VM v : ids) {
            trans.add(getVMAction(v));
        }
        return trans;
    }

    @Override
    public VMTransition getVMAction(VM id) {
        int idx = getVM(id);
        return idx < 0 ? null : vmActions.get(idx);
    }


    @Override
    public NodeTransition getNodeAction(Node id) {
        int idx = getNode(id);
        return idx < 0 ? null : nodeActions.get(idx);
    }

    @Override
    public VM cloneVM(VM vm) {
        VM newVM = model.newVM();
        if (newVM == null) {
            return null;
        }
        for (Map.Entry<String, ChocoView> e : coreViews.entrySet()) {
            e.getValue().cloneVM(vm, newVM);
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

    @Override
    public void stop() {
        stopButton.stopNow();
    }
}
