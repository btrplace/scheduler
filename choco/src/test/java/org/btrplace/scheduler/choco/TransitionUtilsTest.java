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
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.transition.TransitionUtils;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.List;


/**
 * Unit tests for {@link org.btrplace.scheduler.choco.transition.TransitionUtils}.
 *
 * @author Fabien Hermenier
 */
public class TransitionUtilsTest {

    private VMTransition[] makeActions() {
        VMTransition[] as = new VMTransition[10];
        Solver s = new Solver();
        for (int i = 0; i < as.length; i++) {
            as[i] = new MockTransition(s, i);

        }
        return as;
    }

    @Test
    public void testGetDSlices() {
        List<Slice> cs = TransitionUtils.getDSlices(makeActions());
        Assert.assertEquals(5, cs.size());
        for (int i = 0; i < cs.size() - 1; i++) {
            Slice s = cs.get(i);
            Slice ns = cs.get(i + 1);
            Assert.assertTrue(s.getHoster().getName().startsWith("dS"));
            Assert.assertTrue(s.getHoster().getValue() < ns.getHoster().getValue());
        }
    }

    @Test
    public void testGetCSlices() {
        List<Slice> cs = TransitionUtils.getCSlices(makeActions());
        Assert.assertEquals(5, cs.size());
        for (int i = 0; i < cs.size() - 1; i++) {
            Slice s = cs.get(i);
            Slice ns = cs.get(i + 1);
            Assert.assertTrue(s.getHoster().getName().startsWith("cS"));
            Assert.assertTrue(s.getHoster().getValue() < ns.getHoster().getValue());
        }
    }

    @Test
    public void testGetStarts() {
        IntVar[] sts = TransitionUtils.getStarts(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntVar s = sts[i];
            IntVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("start"));
            Assert.assertTrue(s.getValue() < ns.getValue());
        }
    }

    @Test
    public void testGetEnds() {
        IntVar[] sts = TransitionUtils.getEnds(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntVar s = sts[i];
            IntVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("end"));
            Assert.assertTrue(s.getValue() < ns.getValue());
        }
    }

    @Test
    public void testGetDurations() {
        IntVar[] sts = TransitionUtils.getDurations(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntVar s = sts[i];
            IntVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("duration"));
            Assert.assertTrue(s.getValue() < ns.getValue());
        }
    }

    public static class MockTransition implements VMTransition {

        private IntVar st, ed, d, h, c;
        private BoolVar state;

        private Slice cSlice, dSlice;

        public MockTransition(Solver s, int nb) {
            Model mo = new DefaultModel();
            st = VF.fixed("start" + nb, nb, s);
            ed = VF.fixed("end" + nb, nb, s);
            d = VF.fixed("duration" + nb, nb, s);
            h = VF.fixed("hoster" + nb, nb, s);
            c = VF.fixed("cost" + nb, nb, s);
            state = VF.bool("state" + nb, s);
            if (nb % 2 == 0) {
                cSlice = new Slice(mo.newVM(),
                        VF.fixed("cS" + nb + "-st", nb, s),
                        VF.fixed("cS" + nb + "-ed", nb, s),
                        VF.fixed("cS" + nb + "-d", nb, s),
                        VF.fixed("cS" + nb + "-h", nb, s));
            } else {
                dSlice = new Slice(mo.newVM(),
                        VF.fixed("dS" + nb + "-st", nb, s),
                        VF.fixed("dS" + nb + "-ed", nb, s),
                        VF.fixed("dS" + nb + "-d", nb, s),
                        VF.fixed("dS" + nb + "-h", nb, s));
            }
        }

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public IntVar getStart() {
            return st;
        }

        @Override
        public VM getVM() {
            return null;
        }

        @Override
        public IntVar getEnd() {
            return ed;
        }

        @Override
        public IntVar getDuration() {
            return d;
        }

        @Override
        public Slice getCSlice() {
            return cSlice;
        }

        @Override
        public Slice getDSlice() {
            return dSlice;
        }

        @Override
        public boolean insertActions(ReconfigurationPlan plan) {
            return true;
        }

        @Override
        public BoolVar getState() {
            return state;
        }

    }
}
