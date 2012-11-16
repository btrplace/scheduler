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
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.*;

/**
 * Default implementation of {@link ReconfigurationProblem}.
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

    private UUID [] vms;
    private TObjectIntHashMap<UUID> revVMs;

    private UUID [] nodes;
    private TObjectIntHashMap<UUID> revNodes;

    private IntDomainVar start;
    private IntDomainVar end;

    private ActionModel [] vmActions;
    private ActionModel [] nodeActions;

    private SolvingStatistics stats;

    public DefaultReconfigurationProblem(Model m, Set<UUID> toWait,
                                                  Set<UUID> toRun,
                                                  Set<UUID> toSleep,
                                                  Set<UUID> toDestroy) throws SolverException {
        waitings = toWait;
        runnings = toRun;
        sleepings = toSleep;
        destroyed = toDestroy;
        model = m;

        checkDisjointSet();


        Set<UUID> allVMs = new HashSet<UUID>(m.getMapping().getAllVMs());
        allVMs.addAll(toWait); //The only VMs that may not appear in the mapping
        vms = new UUID[allVMs.size()];
        revVMs = new TObjectIntHashMap<UUID>(allVMs.size(), 0.5f, -1); //0.5f is the default load factor
        int i = 0;

        for (UUID vmId : allVMs) {
            vms[i] = vmId;
            revVMs.put(vmId, i++);
        }


        i = 0;
        Set<UUID> s = model.getMapping().getAllNodes();
        nodes = new UUID[s.size()];
        revNodes = new TObjectIntHashMap<UUID>(s.size(), 0.5f, -1);
        for (UUID nId : s) {
            nodes[i] = nId;
            revNodes.put(nId, i++);
        }

        solver = new CPSolver();
        start = solver.makeConstantIntVar(0);
        end = solver.createBoundIntVar("end", 0, ReconfigurationProblem.DEFAULT_MAX_TIME);
        solver.post(solver.geq(end, start));
    }

    private void checkDisjointSet() throws SolverException {
        Mapping map = model.getMapping();
        for (UUID vmId : map.getAllVMs()) {
            int nbIn = runnings.contains(vmId) ? 1 : 0;
            if (waitings.contains(vmId)) {nbIn++;}
            if (sleepings.contains(vmId)) {nbIn++;}
            if (destroyed.contains(vmId)) {nbIn++;}
            if (nbIn == 0) {
                throw new SolverException(model, "Next state for VM '" + vmId + "' is unknown.");
            } else if (nbIn > 1) {
                throw new SolverException(model, "Next state for VM '" + vmId + "' is ambiguous");
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
    public ActionModel [] getVMActions() {
        return vmActions;
    }

    @Override
    public ActionModel getVMAction(int vmIdx) {
        return vmActions[vmIdx];
    }

    @Override
    public ActionModel [] getNodeActions() {
        return nodeActions;
    }

    @Override
    public ActionModel getNodeAction(int nId) {
        return nodeActions[nId];
    }

    @Override
    public DurationEvaluator getDurationEvaluator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Slice> getDSlices() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Slice> getCSlices() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ActionModel [] getVMActions(Set<UUID> id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IntDomainVar getTimeVMReady(UUID vm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ReconfigurationPlan extractSolution() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SolvingStatistics getSolvingStatistics() {
        return stats;
    }

    @Override
    public CPSolver getSolver() {
        return solver;
    }
}
