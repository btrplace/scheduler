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

package btrplace.model;

import gnu.trove.set.hash.TIntHashSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class PartitionsTest {

    @Test
    public void test() {
        Set<Integer> s = new HashSet<Integer>();
        for (int i = 0; i < 10; i++) {
            s.add(i);
        }
        Partitions<Integer> partitions = new Partitions<Integer>(s, 3);
        System.err.println(partitions.toString());
        partitions.putIn(5,1);
        System.err.println(partitions.toString());
        Assert.fail();
    }
}
