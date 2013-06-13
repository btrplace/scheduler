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

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link Splitters}.
 *
 * @author Fabien Hermenier
 */
public class SplittersTest {

    @Test
    public void testExtractIn() {
        Set<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> in = new HashSet<>(Arrays.asList(3, 5, 8, 10));
        Set<Integer> removed = Splitters.extractInside(s, in);
        Assert.assertEquals(removed, new HashSet<>(Arrays.asList(3, 5)));
        Assert.assertEquals(s.size(), 3);
        Assert.assertFalse(s.contains(3));
        Assert.assertFalse(s.contains(5));

        removed = Splitters.extractInside(s, in);
        Assert.assertEquals(removed.size(), 0);
    }
}
