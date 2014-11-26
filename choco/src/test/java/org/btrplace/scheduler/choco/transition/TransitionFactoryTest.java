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
import org.testng.Assert;
import org.testng.annotations.Test;

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
        VMTransitionBuilder b = amf.getBuilder(VMState.RUNNING, VMState.SLEEPING).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.SLEEPING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.RUNNING));

        //Sleeping -> Running
        b = amf.getBuilder(VMState.SLEEPING, VMState.RUNNING).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.RUNNING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.SLEEPING));

        //Running -> Running
        b = amf.getBuilder(VMState.RUNNING, VMState.RUNNING).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.RUNNING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.RUNNING));

        //Ready -> Running
        b = amf.getBuilder(VMState.READY, VMState.RUNNING).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.RUNNING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.READY));

        //Running -> Ready
        b = amf.getBuilder(VMState.RUNNING, VMState.READY).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.READY);
        Assert.assertTrue(b.getSourceStates().contains(VMState.RUNNING));

        //Init -> Ready
        b = amf.getBuilder(VMState.INIT, VMState.READY).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.READY);
        Assert.assertTrue(b.getSourceStates().contains(VMState.INIT));

        //Ready -> READY
        b = amf.getBuilder(VMState.READY, VMState.READY).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.READY);
        Assert.assertTrue(b.getSourceStates().contains(VMState.READY));

        //Sleeping -> Sleeping
        b = amf.getBuilder(VMState.SLEEPING, VMState.SLEEPING).get(0);
        Assert.assertNotNull(b);
        Assert.assertEquals(b.getDestinationState(), VMState.SLEEPING);
        Assert.assertTrue(b.getSourceStates().contains(VMState.SLEEPING));

        //The nodes
        NodeTransitionBuilder b2 = amf.getBuilder(NodeState.ONLINE);
        Assert.assertEquals(b2.getSourceState(), NodeState.ONLINE);
        b2 = amf.getBuilder(NodeState.OFFLINE);
        Assert.assertEquals(b2.getSourceState(), NodeState.OFFLINE);
    }

}
