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

package org.btrplace.safeplace.util;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllPackingsGeneratorTest {

    @Test
    public void test() {
        AllPackingsGenerator<Character> pg = new AllPackingsGenerator<>(Character.class, Arrays.asList(new Character[]{'a', 'b', 'c'}));
        Set<Set<Set<Character>>> packings = new HashSet<>();
        while (pg.hasNext()) {
            packings.add(pg.next());
        }
        for (Set<Set<Character>> s : packings) {
            System.out.println(s);
        }
        Assert.assertEquals(packings.size(), 15);
    }
}
