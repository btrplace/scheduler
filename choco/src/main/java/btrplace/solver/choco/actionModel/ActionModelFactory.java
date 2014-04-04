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

package btrplace.solver.choco.actionModel;

import btrplace.model.NodeState;
import btrplace.model.VMState;
import btrplace.solver.SolverException;

import java.util.*;

/**
 * A customisable factory to allow to plug your own {@link ActionModel} for a given transition.
 *
 * @author Fabien Hermenier
 */
public class ActionModelFactory {

    private Map<VMState, List<VMActionModelBuilder>> vmAMB2;

    private Map<NodeState, NodeActionModelBuilder> nodeAMB;

    /**
     * Make a new factory.
     */
    public ActionModelFactory() {
        vmAMB2 = new HashMap<>();
        nodeAMB = new HashMap<>();
    }

    public void add(VMActionModelBuilder b) {
        List<VMActionModelBuilder> l = vmAMB2.get(b.getDestinationState());
        if (l == null) {
            l = new ArrayList<>();
            vmAMB2.put(b.getDestinationState(), l);
        }
        l.add(b);
    }

    public void add(NodeActionModelBuilder b) {
        nodeAMB.put(b.getSourceState(), b);
    }

    /**
     * Get the model builder for a given transition
     *
     * @param srcState the current VM state
     * @param dstState the current VM state
     * @return the {@link btrplace.solver.choco.actionModel.VMActionModel} associated to the state transition
     * @throws btrplace.solver.SolverException no transition or multiple transitions are possible
     */
    public VMActionModelBuilder getBuilder(VMState srcState, VMState dstState) throws SolverException {
        List<VMActionModelBuilder> possibleTransitions = vmAMB2.get(srcState);
        for (VMActionModelBuilder vmb : possibleTransitions) {
            if (vmb.getDestinationState() == dstState) {
                return vmb;
            }
        }
        return null;
    }

    /**
     * Get the model builder for a given transition
     *
     * @param srcState the current VM state
     * @return the {@link btrplace.solver.choco.actionModel.NodeActionModel} associated to the state transition
     */
    public NodeActionModelBuilder getBuilder(NodeState srcState) throws SolverException {
        return nodeAMB.get(srcState);
    }

    public static void fillWithDefaults(ActionModelFactory f) {
        f.add(new BootVMModel.Builder());
        f.add(new ShutdownVMModel.Builder());
        f.add(new SuspendVMModel.Builder());
        f.add(new ResumeVMModel.Builder());
        f.add(new KillVMModel.Builder());

        f.add(new BootableNodeModel.Builder());
        f.add(new ShutdownableNodeModel.Builder());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Object nb : nodeAMB.values()) {
            b.append("node ").append(nb).append('\n');
        }
        Set<VMActionModelBuilder> vmb = new HashSet<>();
        for (Map.Entry<VMState, List<VMActionModelBuilder>> entry : vmAMB2.entrySet()) {
            for (VMActionModelBuilder a : entry.getValue()) {
                if (vmb.add(a)) {
                    b.append("vm ").append(a).append('\n');
                }
            }
        }
        return b.toString();
    }
}
