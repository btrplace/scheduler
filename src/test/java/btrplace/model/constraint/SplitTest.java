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

package btrplace.model.constraint;

import btrplace.model.*;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link Split}.
 *
 * @author Fabien Hermenier
 */
public class SplitTest {

    @Test
    public void testInstantiation() {
        Set<UUID> s1 = new HashSet<UUID>();
        Set<UUID> s2 = new HashSet<UUID>();
        s1.add(UUID.randomUUID());
        s2.add(UUID.randomUUID());
        List<Set<UUID>> args = new ArrayList<Set<UUID>>();
        args.add(s1);
        args.add(s2);
        Split sp = new Split(args);
        Assert.assertEquals(args, sp.getSets());
        Assert.assertEquals(2, sp.getInvolvedVMs().size());
        Assert.assertTrue(sp.getInvolvedNodes().isEmpty());
        Assert.assertFalse(sp.toString().contains("null"));
    }

    @Test
    public void testEquals() {
        Set<UUID> s1 = new HashSet<UUID>();
        Set<UUID> s2 = new HashSet<UUID>();
        s1.add(UUID.randomUUID());
        s2.add(UUID.randomUUID());
        List<Set<UUID>> args = new ArrayList<Set<UUID>>();
        args.add(s1);
        args.add(s2);
        Split sp = new Split(args);
        Assert.assertTrue(sp.equals(sp));
        Assert.assertTrue(new Split(args).equals(sp));
        Assert.assertEquals(new Split(args).hashCode(), sp.hashCode());
        List<Set<UUID>> args2 = new ArrayList<Set<UUID>>(args);
        args2.add(Collections.singleton(UUID.randomUUID()));
        Assert.assertFalse(new Split(args2).equals(sp));
    }

    @Test
    public void testIsSatisfied() {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        UUID vm5 = UUID.randomUUID();

        Set<Set<UUID>> args = new HashSet<Set<UUID>>();
        Set<UUID> s1 = new HashSet<UUID>();
        s1.add(vm1);
        s1.add(vm2);
        Set<UUID> s2 = new HashSet<UUID>();
        s2.add(vm3);
        s2.add(vm4);
        Set<UUID> s3 = new HashSet<UUID>();
        s3.add(UUID.randomUUID());
        args.add(s1);
        args.add(s2);
        args.add(s3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);

        Split sp = new Split(args);
        Model mo = new DefaultModel(map);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, sp.isSatisfied(mo));
        map.addRunningVM(vm3, n3);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, sp.isSatisfied(mo));
        map.addRunningVM(vm3, n1);
        Assert.assertEquals(SatConstraint.Sat.UNSATISFIED, sp.isSatisfied(mo));
    }

}
