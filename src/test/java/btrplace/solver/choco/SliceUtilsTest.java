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

package btrplace.solver.choco;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link SliceUtils}.
 *
 * @author Fabien Hermenier
 */
public class SliceUtilsTest {

    private List<Slice> makeSlices() {
        CPSolver csp = new CPSolver();
        List<Slice> l = new ArrayList<Slice>();
        for (int i = 0; i < 10; i++) {
            l.add(new Slice(UUID.randomUUID(),
                    csp.createBooleanVar("st " + i),
                    csp.createBooleanVar("ed " + i),
                    csp.createBooleanVar("du " + i),
                    csp.createBooleanVar("ho " + i)
            ));
        }
        return l;
    }

    @Test
    public void testExtractHosters() {
        IntDomainVar[] vs = SliceUtils.extractHosters(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "ho " + i);
        }
    }

    @Test
    public void testExtractStarts() {
        IntDomainVar[] vs = SliceUtils.extractStarts(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "st " + i);
        }
    }

    @Test
    public void testExtractEnds() {
        IntDomainVar[] vs = SliceUtils.extractEnds(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "ed " + i);
        }
    }

    @Test
    public void testExtractDurations() {
        IntDomainVar[] vs = SliceUtils.extractDurations(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "du " + i);
        }
    }


}
