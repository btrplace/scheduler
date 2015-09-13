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

package org.btrplace.scheduler.choco;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.ArrayList;
import java.util.List;


/**
 * Unit tests for {@link SliceUtils}.
 *
 * @author Fabien Hermenier
 */
public class SliceUtilsTest {

    private List<Slice> makeSlices() {
        Model mo = new DefaultModel();
        Solver csp = new Solver();
        List<Slice> l = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            l.add(new Slice(mo.newVM(),
                    VF.bounded("st " + i, 0, 1, csp),
                    VF.bounded("ed " + i, 0, 1, csp),
                    VF.bounded("du " + i, 0, 1, csp),
                    VF.bounded("ho " + i, 0, 1, csp)
            ));
        }
        return l;
    }

    @Test
    public void testExtractHosters() {
        IntVar[] vs = SliceUtils.extractHoster(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "ho " + i);
        }
    }

    @Test
    public void testExtractStarts() {
        IntVar[] vs = SliceUtils.extractStarts(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "st " + i);
        }
    }

    @Test
    public void testExtractEnds() {
        IntVar[] vs = SliceUtils.extractEnds(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "ed " + i);
        }
    }

    @Test
    public void testExtractDurations() {
        IntVar[] vs = SliceUtils.extractDurations(makeSlices());
        for (int i = 0; i < vs.length; i++) {
            Assert.assertEquals(vs[i].getName(), "du " + i);
        }
    }


}
