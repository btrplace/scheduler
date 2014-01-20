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

package btrplace.solver.choco.chocoUtil;


import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ChocoUtils}.
 *
 * @author Fabien Hermenier
 */
public class ChocoUtilsTest {

    @Test
    public void testGetNextContiguous() throws ContradictionException {
        Solver s = new Solver();
        IntVar v = s.createEnumIntVar("foo", 0, 100);

        //1-3 5 9 11-15 17 25-50
        v.remVal(0);
        int[] bounds = ChocoUtils.getNextContiguousValues(v, 0);
        //System.out.println(ChocoUtils.prettyContiguous(v));
        Assert.assertEquals(bounds[0], 1);
        Assert.assertEquals(bounds[1], 100);

        v.remVal(4);
        bounds = ChocoUtils.getNextContiguousValues(v, 1);
        //System.out.println(ChocoUtils.prettyContiguous(v));
        Assert.assertEquals(bounds[0], 1);
        Assert.assertEquals(bounds[1], 3);

        bounds = ChocoUtils.getNextContiguousValues(v, 4);
        //System.out.println(ChocoUtils.prettyContiguous(v));
        Assert.assertEquals(bounds[0], 5);
        Assert.assertEquals(bounds[1], 100);
    }
}
