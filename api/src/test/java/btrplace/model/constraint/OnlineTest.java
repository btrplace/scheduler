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

package btrplace.model.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OnlineTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));
        Online o = new Online(s);
        Assert.assertNotNull(o.getChecker());
        Assert.assertEquals(o.getInvolvedNodes(), s);
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        System.out.println(o);
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        Mapping c = i.getMapping();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));
        Online o = new Online(s);

        Assert.assertEquals(o.isSatisfied(i), true);
        c.addOfflineNode(n2);
        Assert.assertEquals(o.isSatisfied(i), false);
    }

    @Test
    public void testEquals() {
        Set<Integer> x = new HashSet<>(Arrays.asList(n1, n2));
        Online s = new Online(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Online(x).equals(s));
        Assert.assertEquals(new Online(x).hashCode(), s.hashCode());
        x = new HashSet<>(Arrays.asList(n3));
        Assert.assertFalse(new Online(x).equals(s));
    }
}
