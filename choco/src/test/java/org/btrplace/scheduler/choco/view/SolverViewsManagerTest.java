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

package org.btrplace.scheduler.choco.view;

import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SolverViewsManagerTest {

    private static class MockViewBuilder extends SolverViewBuilder {
        private String key;
        private List<String> deps;
        public static List<String> order = new ArrayList<>();

        public MockViewBuilder(String k, List<String> deps) {
            this.key = k;
            this.deps = deps;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public ChocoView build(ReconfigurationProblem rp) throws SchedulerException {
            order.add(key);
            return null;
        }

        @Override
        public List<String> getDependencies() {
            return deps;
        }
    }
     /*
    @Test
    public void test() throws SchedulerException {
        List<SolverViewBuilder> b = new ArrayList<>();
        b.add(new MockViewBuilder("a", Collections.<String>emptyList()));
        b.add(new MockViewBuilder("b", Collections.<String>emptyList()));
        b.add(new MockViewBuilder("c", Arrays.asList("a")));
        b.add(new MockViewBuilder("d", Arrays.asList("a", "c")));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel()).setParams(new DefaultParameters()).build();
        SolverViewsManager l = new SolverViewsManager(rp, b);
        Assert.assertEquals(MockViewBuilder.order, Arrays.asList("a", "b", "c", "d"));
    }

    @Test(expectedExceptions = {SchedulerException.class})
    public void testCyclicDependency() throws SchedulerException {
        List<SolverViewBuilder> b = new ArrayList<>();
        b.add(new MockViewBuilder("a", Arrays.asList("c")));
        b.add(new MockViewBuilder("b", Arrays.asList("a")));
        b.add(new MockViewBuilder("c", Arrays.asList("b")));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(new DefaultModel()).setParams(new DefaultParameters()).build();
        SolverViewsManager l = new SolverViewsManager(rp, b);
    }        */
}
