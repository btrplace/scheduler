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

/**
 * Unit tests for {@link org.btrplace.model.constraint.Sleeping}.
 *
 * @author Fabien Hermenier
 */
public class SleepingTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();

        Sleeping s = new Sleeping(v);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(Collections.singletonList(v), s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        Assert.assertTrue(s.setContinuous(true));
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Sleeping s = new Sleeping(v);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Sleeping(v).equals(s));
        Assert.assertEquals(new Sleeping(v).hashCode(), s.hashCode());
        Assert.assertFalse(new Sleeping(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        VM v = i.newVM();
        Node n = i.newNode();

        Mapping c = i.getMapping();

        c.addOnlineNode(n);
        c.addSleepingVM(v, n);
        Sleeping d = new Sleeping(v);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(v);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addRunningVM(v, n);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.remove(v);
        Assert.assertEquals(d.isSatisfied(i), false);
    }
}
