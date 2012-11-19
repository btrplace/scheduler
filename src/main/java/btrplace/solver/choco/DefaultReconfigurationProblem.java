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
import btrplace.plan.Action;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;
import btrplace.solver.choco.actionModel.*;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.*;

/**
 * Default implementation of {@link ReconfigurationProblem}.
 * TODO: resource capacity
 * TODO: actions model
 * TODO: duration evaluator
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblem implements ReconfigurationProblem {

    private Model model;

    private CPSolver solver;

    private Set<UUID> waitings;
    private Set<UUID> runnings;
    private Set<UUID> sleepings;
    private Set<UUID> destroyed;

    private UUID[] vms;
    private TObjectIntHashMap<UUID> revVMs;

    private UUID[] nodes;
    private TObjectIntHashMap<UUID> revNodes;

    private IntDomainVar start;
    private IntDomainVar end;

    private ActionModel[] vmActions;
    private ActionModel[] nodeActions;

    private List<Slice> dSlices;
    private List<Slice> cSlices;

    private DurationEvaluators durEval;

    public DefaultReconfigurationProblem(Model m,
                                         Set<UUID> toWait,
                                         Set<UUID> toRun,
                                         Set<UUID> toSleep,
                                         Set<UUID> toDestroy) throws SolverException {
        this(m, new DurationEvaluators(), toWait, toRun, toSleep, toDestroy);
    }

    public DefaultReconfigurationProblem(Model m,
                                         DurationEvaluators dEval,
                                         Set<UUID> toWait,
                                         Set<UUID> toRun,
                                         Set<UUID> toSleep,
                                         Set<UUID> toDestroy) throws SolverException {
        waitings = toWait;
        runnings = toRun;
        sleepings = toSleep;
        destroyed = toDestroy;
        model = m;
        durEval = dEval;

        cSlices = new ArrayList<Slice>();
        dSlices = new ArrayList<Slice>();

        makeVMActionModels();
        makeNodeActionModels();

        solver = new CPSolver();
        start = solver.makeConstantIntVar(0);
        end = solver.createBoundIntVar("end", 0, ReconfigurationProblem.DEFAULT_MAX_TIME);
        solver.post(solver.geq(end, start));
    }

    private void makeVMActionModels() throws SolverException {
        Set<UUID> allVMs = new HashSet<UUID>(model.getMapping().getAllVMs());
        allVMs.addAll(waitings); //The only VMs that may not appear in the mapping

        vms = new UUID[allVMs.size()];
        revVMs = new TObjectIntHashMap<UUID>(allVMs.size(), 0.5f, -1); //0.5f is the default load factor
        int i = 0;
        Mapping map = model.getMapping();
        vmActions = new ActionModel[allVMs.size()];
        for (UUID vmId : allVMs) {
            if (runnings.contains(vmId)) {
                if (map.getSleepingVMs().contains(vmId)) {
                    vmActions[i] = new ResumeVMModel(vmId);
                } else if (map.getRunningVMs().contains(vmId)) {
                    vmActions[i] = new MigratableVMModel(vmId);
                } else if (map.getWaitingVMs().contains(vmId)) {
                    vmActions[i] = new BootVMModel(vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' running: not instantiated");
                }
            }
            if (waitings.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (!map.getAllVMs().contains(vmId)) {
                    vmActions[i] = new InstantiateVMModel(vmId);
                } else {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' waiting: already instantiated or unknown");
                }
            }
            if (sleepings.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (map.getRunningVMs().contains(vmId)) {
                    vmActions[i] = new SuspendVMModel(vmId);
                } else if (!map.getSleepingVMs().contains(vmId)) {
                    throw new SolverException(model, "Unable to set VM '" + vmId + "' sleeping: should be running");
                }
            }
            if (destroyed.contains(vmId)) {
                if (vmActions[i] != null) {
                    throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
                } else if (map.getRunningVMs().contains(vmId)) {
                    vmActions[i] = new ShutdownVMModel(vmId);
                } else {
                    throw new SolverException(model, "Unable to halt VM '" + vmId + "': should be running");
                }
            } else {
                throw new SolverException(model, "Next state for VM '" + vmId + "' is undefined");
            }
            Slice s = vmActions[i].getCSlice();
            if (s != null) {
                cSlices.add(s);
            }

            s = vmActions[i].getDSlice();
            if (s != null) {
                dSlices.add(s);
            }
            vms[i] = vmId;
            revVMs.put(vmId, i++);
        }
    }

    private void makeNodeActionModels() throws SolverException {
        nodes = new UUID[model.getMapping().getAllNodes().size()];
        revNodes = new TObjectIntHashMap<UUID>(nodes.length, 0.5f, -1);

        Mapping m = model.getMapping();
        int i = 0;
        for (UUID nId : model.getMapping().getAllNodes()) {
            if (m.getOfflineNodes().contains(nId)) {
                nodeActions[i] = new BootableNodeModel(nId);
            }
            if (m.getOfflineNodes().contains(nId)) {
                if (nodeActions[i] != null) {
                    throw new SolverException(model, "Next state for node '" + nId + "' is ambiguous");
                }
                nodeActions[i] = new ShutdownableNodeModel(nId);
            }

            Slice s = nodeActions[i].getCSlice();
            if (s != null) {
                cSlices.add(s);
            }

            s = nodeActions[i].getDSlice();
            if (s != null) {
                dSlices.add(s);
            }

            nodes[i] = nId;
            revNodes.put(nId, i++);
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
    public UUID[] getVirtualMachines() {
        return vms;
    }

    @Override
    public Model getSourceModel() {
        return model;
    }

    @Override
    public Set<UUID> getFutureRunnings() {
        return runnings;
    }

    @Override
    public Set<UUID> getFutureWaitings() {
        return waitings;
    }

    @Override
    public Set<UUID> getFutureSleepings() {
        return sleepings;
    }

    @Override
    public Set<UUID> getFutureDestroyed() {
        return destroyed;
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
    public ActionModel[] getVMActions() {
        return vmActions;
    }

    @Override
    public ActionModel[] getVMActions(Set<UUID> id) {
        return vmActions;
    }

    @Override
    public ActionModel getVMAction(int vmIdx) {
        return vmActions[vmIdx];
    }

    @Override
    public ActionModel[] getNodeActions() {
        return nodeActions;
    }

    @Override
    public ActionModel getNodeAction(int nId) {
        return nodeActions[nId];
    }

    @Override
    public DurationEvaluators getDurationEvaluator() {
        return durEval;
    }

    @Override
    public List<Slice> getDSlices() {
        return dSlices;
    }

    @Override
    public List<Slice> getCSlices() {
        return cSlices;
    }

    @Override
    public ReconfigurationPlan extractSolution() {
        if (!Boolean.TRUE.equals(solver.isFeasible())) {
            return null;
        }

        DefaultReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        for (ActionModel action : nodeActions) {
            for (Action a : action.getResultingActions(this)) {
                plan.add(a);
            }

        }
        for (ActionModel action : vmActions) {
            for (Action a : action.getResultingActions(this)) {
                plan.add(a);
            }

        }
        assert checkConsistency(plan);
        return plan;
    }

    private boolean checkConsistency(ReconfigurationPlan p) {
        for (Action a : p) {
            if (a.getStart() == a.getEnd()) {
                return false;
            }
        }
        return p.getDuration() == end.getVal();
    }

    @Override
    public CPSolver getSolver() {
        return solver;
    }
}
