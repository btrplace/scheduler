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

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.NodeState;
import org.btrplace.model.VMState;

import java.util.*;

/**
 * A customisable factory that provide the right transition model according
 * to the given element states.
 *
 * @author Fabien Hermenier
 */
public class TransitionFactory {

    private Map<VMState, List<VMTransitionBuilder>> vmAMB2;

    private Map<NodeState, NodeTransitionBuilder> nodeAMB;

    /**
     * Make a new factory.
     */
    public TransitionFactory() {
        vmAMB2 = new HashMap<>();
        nodeAMB = new HashMap<>();
    }

    /**
     * Add a builder for a VM
     *
     * @param b the builder to add
     */
    public void add(VMTransitionBuilder b) {
        List<VMTransitionBuilder> l = vmAMB2.get(b.getDestinationState());
        if (l == null) {
            l = new ArrayList<>();
            vmAMB2.put(b.getDestinationState(), l);
        }
        l.add(b);
    }

    /**
     * Remove a builder for an action on a VM.
     *
     * @param b the builder to remove
     * @return {@code true} if it has been removed
     */
    public boolean remove(VMTransitionBuilder b) {
        VMState dst = b.getDestinationState();
        return vmAMB2.get(dst).remove(b);
    }

    /**
     * Remove a builder for an action on a node.
     *
     * @param b the builder to remove
     * @return {@code true} if it has been removed
     */
    public boolean remove(NodeTransitionBuilder b) {
        return nodeAMB.remove(b.getSourceState()) != null;
    }

    /**
     * Add a builder for a VM
     *
     * @param b the builder to add
     */
    public void add(NodeTransitionBuilder b) {
        nodeAMB.put(b.getSourceState(), b);
    }

    /**
     * Get the model builder for a given transition
     *
     * @param srcState the current VM state
     * @param dstState the current VM state
     * @return the list of possible transitions. May be empty
     */
    public List<VMTransitionBuilder> getBuilder(VMState srcState, VMState dstState) {
        List<VMTransitionBuilder> dstCompliant = vmAMB2.get(dstState);
        List<VMTransitionBuilder> possibles = new ArrayList<>();
        for (VMTransitionBuilder vmb : dstCompliant) {
            if (vmb.getSourceStates().contains(srcState)) {
                possibles.add(vmb);
            }
        }
        return possibles;
    }

    /**
     * Get the model builder for a given transition
     *
     * @param srcState the current VM state
     * @return the {@link NodeTransition} associated to the state transition
     */
    public NodeTransitionBuilder getBuilder(NodeState srcState) {
        return nodeAMB.get(srcState);
    }

    /**
     * a new factory that embeds the default builders.
     *
     * @return a viable factory
     */
    public static TransitionFactory newBundle() {
        TransitionFactory f = new TransitionFactory();
        f.add(new BootVM.Builder());
        f.add(new ShutdownVM.Builder());
        f.add(new SuspendVM.Builder());
        f.add(new ResumeVM.Builder());
        f.add(new KillVM.Builder());
        f.add(new RelocatableVM.Builder());
        f.add(new ForgeVM.Builder());
        f.add(new StayAwayVM.BuilderReady());
        f.add(new StayAwayVM.BuilderSleeping());
        f.add(new BootableNode.Builder());
        f.add(new ShutdownableNode.Builder());
        return f;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Object nb : nodeAMB.values()) {
            b.append("node ").append(nb).append('\n');
        }
        Set<VMTransitionBuilder> vmb = new HashSet<>();
        for (Map.Entry<VMState, List<VMTransitionBuilder>> entry : vmAMB2.entrySet()) {
            for (VMTransitionBuilder a : entry.getValue()) {
                if (vmb.add(a)) {
                    b.append("vm ").append(a).append('\n');
                }
            }
        }
        return b.toString();
    }
}
