/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Node;
import org.btrplace.model.NodeState;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.EnumSet;

/**
 * Unit tests for {@link TransitionFactory}.
 *
 * @author Fabien Hermenier
 */
public class TransitionFactoryTest {

    @Test
    public void testDefault() {
        TransitionFactory amf = TransitionFactory.newBundle();

        System.out.println(amf);
        //Running -> Sleeping
        VMTransitionBuilder b = amf.getBuilder(VMState.RUNNING, VMState.SLEEPING);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.SLEEPING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.RUNNING));

        //Sleeping -> Running
        b = amf.getBuilder(VMState.SLEEPING, VMState.RUNNING);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.RUNNING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.SLEEPING));

        //Running -> Running
        b = amf.getBuilder(VMState.RUNNING, VMState.RUNNING);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.RUNNING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.RUNNING));

        //Ready -> Running
        b = amf.getBuilder(VMState.READY, VMState.RUNNING);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.RUNNING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.READY));

        //Running -> Ready
        b = amf.getBuilder(VMState.RUNNING, VMState.READY);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.READY);
        Assert.assertTrue(b.getSourceStates().contains(VMState.RUNNING));

        //Init -> Ready
        b = amf.getBuilder(VMState.INIT, VMState.READY);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.READY);
        Assert.assertTrue(b.getSourceStates().contains(VMState.INIT));

        //Ready -> READY
        b = amf.getBuilder(VMState.READY, VMState.READY);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.READY);
        Assert.assertTrue(b.getSourceStates().contains(VMState.READY));

        //Sleeping -> Sleeping
        b = amf.getBuilder(VMState.SLEEPING, VMState.SLEEPING);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.SLEEPING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.SLEEPING));

        //The nodes
        NodeTransitionBuilder b2 = amf.getBuilder(NodeState.ONLINE);
        Assert.assertEquals(b2.getSourceState(), NodeState.ONLINE);
        b2 = amf.getBuilder(NodeState.OFFLINE);
        Assert.assertEquals(b2.getSourceState(), NodeState.OFFLINE);
    }

    @Test
    public void testRemove() {
        TransitionFactory amf = TransitionFactory.newBundle();
        MockVMBuilder vmb = new MockVMBuilder();
        amf.remove(vmb);
        for (VMState src : vmb.getSourceStates()) {
            Assert.assertNull(amf.getBuilder(src, vmb.getDestinationState()));
        }

        MockNodeBuilder nb = new MockNodeBuilder();
        amf.remove(nb);
        Assert.assertNull(amf.getBuilder(nb.getSourceState()));
    }

    @Test
    public void testAdd() {
        TransitionFactory amf = TransitionFactory.newBundle();
        MockVMBuilder vmb = new MockVMBuilder();
        amf.add(vmb);
        for (VMState src : vmb.getSourceStates()) {
            Assert.assertEquals(amf.getBuilder(src, vmb.getDestinationState()), vmb);
        }

        MockNodeBuilder nb = new MockNodeBuilder();
        amf.add(nb);
        Assert.assertEquals(amf.getBuilder(nb.getSourceState()), nb);
    }

    public static class MockVMBuilder extends VMTransitionBuilder {
        public MockVMBuilder() {
            super("foo", EnumSet.of(VMState.READY, VMState.SLEEPING), VMState.RUNNING);
        }

        @Override
        public VMTransition build(ReconfigurationProblem rp, VM v) throws SchedulerException {
            return null;
        }
    }

    public static class MockNodeBuilder extends NodeTransitionBuilder {
        public MockNodeBuilder() {
            super("foo", NodeState.OFFLINE);
        }

        @Override
        public NodeTransition build(ReconfigurationProblem rp, Node n) throws SchedulerException {
            return null;
        }
    }
}
