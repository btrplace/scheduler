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

package org.btrplace.plan;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Action;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple unit tests for {@link Dependency}.
 *
 * @author Fabien Hermenier
 */
public class DependencyTest {

    private static Model mo = new DefaultModel();
    VM vm = mo.newVM();

    @Test
    public void testInstantiation() {
        Action a = new MockAction(vm, 1, 4);
        Set<Action> d = new HashSet<>();
        d.add(new MockAction(vm, 2, 5));
        d.add(new MockAction(vm, 3, 7));
        Dependency dep = new Dependency(a, d);
        Assert.assertEquals(dep.getAction(), a);
        Assert.assertEquals(dep.getDependencies(), d);
        Assert.assertFalse(dep.toString().contains("null"));
    }
}
