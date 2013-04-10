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

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.actionModel.ActionModelVisitor;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link ActionModelUtils}.
 *
 * @author Fabien Hermenier
 */
public class ActionModelUtilsTest {

    private VMActionModel[] makeActions() {
        VMActionModel[] as = new VMActionModel[10];
        CPSolver s = new CPSolver();
        for (int i = 0; i < as.length; i++) {
            as[i] = new MockActionModel(s, i);

        }
        return as;
    }

    @Test
    public void testGetDSlices() {
        List<Slice> cs = ActionModelUtils.getDSlices(makeActions());
        Assert.assertEquals(5, cs.size());
        for (int i = 0; i < cs.size() - 1; i++) {
            Slice s = cs.get(i);
            Slice ns = cs.get(i + 1);
            Assert.assertTrue(s.getHoster().getName().startsWith("dS"));
            Assert.assertTrue(s.getHoster().getVal() < ns.getHoster().getVal());
        }
    }

    @Test
    public void testGetCSlices() {
        List<Slice> cs = ActionModelUtils.getCSlices(makeActions());
        Assert.assertEquals(5, cs.size());
        for (int i = 0; i < cs.size() - 1; i++) {
            Slice s = cs.get(i);
            Slice ns = cs.get(i + 1);
            Assert.assertTrue(s.getHoster().getName().startsWith("cS"));
            Assert.assertTrue(s.getHoster().getVal() < ns.getHoster().getVal());
        }
    }

    @Test
    public void testGetStarts() {
        IntDomainVar[] sts = ActionModelUtils.getStarts(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntDomainVar s = sts[i];
            IntDomainVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("start"));
            Assert.assertTrue(s.getVal() < ns.getVal());
        }
    }

    @Test
    public void testGetEnds() {
        IntDomainVar[] sts = ActionModelUtils.getEnds(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntDomainVar s = sts[i];
            IntDomainVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("end"));
            Assert.assertTrue(s.getVal() < ns.getVal());
        }
    }

    @Test
    public void testGetDurations() {
        IntDomainVar[] sts = ActionModelUtils.getDurations(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntDomainVar s = sts[i];
            IntDomainVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("duration"));
            Assert.assertTrue(s.getVal() < ns.getVal());
        }
    }

    public static class MockActionModel implements VMActionModel {

        private IntDomainVar st, ed, d, h, c, state;

        private Slice cSlice, dSlice;

        public MockActionModel(CPSolver s, int nb) {
            st = s.createBoundIntVar("start" + nb, nb, nb + 1);
            ed = s.createBoundIntVar("end" + nb, nb, nb + 1);
            d = s.createBoundIntVar("duration" + nb, nb, nb + 1);
            h = s.createBoundIntVar("hoster" + nb, nb, nb + 1);
            c = s.createBoundIntVar("cost" + nb, nb, nb + 1);
            state = s.createBoundIntVar("state" + nb, nb, nb + 1);
            if (nb % 2 == 0) {
                cSlice = new Slice(new UUID(10, nb),
                        s.createBoundIntVar("cS" + nb + "-st", nb, nb + 1),
                        s.createBoundIntVar("cS" + nb + "-ed", nb, nb + 1),
                        s.createBoundIntVar("cS" + nb + "-d", nb, nb + 1),
                        s.createBoundIntVar("cS" + nb + "-h", nb, nb + 1));
            } else {
                dSlice = new Slice(new UUID(15, nb),
                        s.createBoundIntVar("dS" + nb + "-st", nb, nb + 1),
                        s.createBoundIntVar("dS" + nb + "-ed", nb, nb + 1),
                        s.createBoundIntVar("dS" + nb + "-d", nb, nb + 1),
                        s.createBoundIntVar("dS" + nb + "-h", nb, nb + 1));
            }
        }

        @Override
        public IntDomainVar getStart() {
            return st;
        }

        @Override
        public UUID getVM() {
            return null;
        }

        @Override
        public IntDomainVar getEnd() {
            return ed;
        }

        @Override
        public IntDomainVar getDuration() {
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
        public IntDomainVar getState() {
            return state;
        }

        @Override
        public void visit(ActionModelVisitor v) {
            throw new UnsupportedOperationException();
        }
    }
}
