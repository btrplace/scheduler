/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.NodeState;
import org.btrplace.model.VMState;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A customisable factory that provides the right transition model according
 * to the given element states.
 *
 * @author Fabien Hermenier
 */
public class TransitionFactory {

  /**
   * Map Destination -> <Source, builder>.
   */
  private final Map<VMState, Map<VMState, VMTransitionBuilder>> vmAMB2;

    private final Map<NodeState, NodeTransitionBuilder> nodeAMB;

  /**
     * Make a new factory.
     */
    public TransitionFactory() {
        vmAMB2 = new EnumMap<>(VMState.class);
        for (VMState src : VMState.values()) {
            vmAMB2.put(src, new EnumMap<>(VMState.class));
        }
        nodeAMB = new EnumMap<>(NodeState.class);
    }

    /**
     * Add a builder for a VM.
     * Every builder that supports the same transition will be replaced.
     *
     * @param b the builder to add
     */
    public void add(VMTransitionBuilder b) {
        Map<VMState, VMTransitionBuilder> m = vmAMB2.get(b.getDestinationState());
        for (VMState src : b.getSourceStates()) {
            m.put(src, b);
        }
    }

    /**
     * Remove a builder for an action on a VM.
     *
     * @param b the builder to remove
     * @return {@code true} if it has been removed
     */
    public boolean remove(VMTransitionBuilder b) {
        Map<VMState, VMTransitionBuilder> m = vmAMB2.get(b.getDestinationState());
        for (VMState src : b.getSourceStates()) {
            m.remove(src);
        }
        return true;
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
     * @return the list of possible transitions. {@code null} if no transition is available
     */
    public VMTransitionBuilder getBuilder(VMState srcState, VMState dstState) {
        Map<VMState, VMTransitionBuilder> dstCompliant = vmAMB2.get(dstState);
        if (dstCompliant == null) {
            return null;
        }
        return dstCompliant.get(srcState);
    }

    /**
     * Get the model builder for a given transition
     *
     * @param srcState the current node state
     * @return the {@link NodeTransition} associated to the state transition. {@code null} if no transition is available
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
        f.add(new StayAwayVM.BuilderInit());
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
        for (Map.Entry<VMState, Map<VMState, VMTransitionBuilder>> entry : vmAMB2.entrySet()) {
            for (VMTransitionBuilder a : entry.getValue().values()) {
                if (vmb.add(a)) {
                    b.append("vm ").append(a).append('\n');
                }
            }
        }
        return b.toString();
    }
}
