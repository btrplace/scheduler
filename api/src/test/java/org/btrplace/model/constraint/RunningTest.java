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

package org.btrplace.model.constraint;

import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Running}.
 *
 * @author Fabien Hermenier
 */
public class RunningTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM vm = mo.newVM();
        Running s = new Running(vm);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(Collections.singletonList(vm), s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        Assert.assertTrue(s.setContinuous(true));
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM vm = mo.newVM();
        Running s = new Running(vm);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Running(vm).equals(s));
        Assert.assertEquals(new Running(vm).hashCode(), s.hashCode());
        Assert.assertFalse(new Running(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        VM vm = i.newVM();
        List<Node> ns = Util.newNodes(i, 2);
        Mapping c = i.getMapping();
        c.addOnlineNode(ns.get(0));
        c.addRunningVM(vm, ns.get(0));
        Running d = new Running(vm);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(vm);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addSleepingVM(vm, ns.get(0));
        Assert.assertEquals(d.isSatisfied(i), false);
        c.remove(vm);
        Assert.assertEquals(d.isSatisfied(i), false);
    }
}
