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

import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.constraint.ChocoConstraintBuilder;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.transition.TransitionFactory;
import org.btrplace.scheduler.choco.transition.TransitionUtils;
import org.btrplace.scheduler.choco.transition.VMTransitionBuilder;
import org.btrplace.scheduler.choco.view.ModelViewMapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultChocoScheduler}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoSchedulerTest {

    @Test
    public void testGetsAndSets() {
        ChocoScheduler cra = new DefaultChocoScheduler();

        cra.setTimeLimit(10);
        Assert.assertEquals(cra.getTimeLimit(), 10);

        cra.setMaxEnd(-5);
        Assert.assertEquals(cra.getMaxEnd(), -5);

        cra.doOptimize(false);
        Assert.assertEquals(cra.doOptimize(), false);

        cra.doRepair(true);
        Assert.assertEquals(cra.doRepair(), true);

        cra.setVerbosity(3);
        Assert.assertEquals(cra.getVerbosity(), 3);

        Assert.assertNotNull(cra.getViewMapper());
        ModelViewMapper m = new ModelViewMapper();
        cra.setViewMapper(m);
        Assert.assertEquals(cra.getViewMapper(), m);
    }

    @Test
    public void testGetStatistics() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < 10; i++) {
            Node n = mo.newNode();
            map.addOnlineNode(n);
            map.addRunningVM(mo.newVM(), n);
        }
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.doOptimize(true);
        cra.setTimeLimit(0);

        Assert.assertNull(cra.getStatistics());

        class Foo extends OptConstraint {
            @Override
            public String id() {
                return "foo";
            }
        }

        cra.getConstraintMapper().register(new ChocoConstraintBuilder() {
            @Override
            public Class<? extends Constraint> getKey() {
                return Foo.class;
            }

            @Override
            public ChocoConstraint build(Constraint cstr) {
                return new ChocoConstraint() {
                    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
                        Mapping map = rp.getSourceModel().getMapping();
                        Solver s = rp.getSolver();
                        IntVar nbNodes = VF.bounded("nbNodes", 1, map.getOnlineNodes().size(), s);
                        IntVar[] hosters = SliceUtils.extractHoster(TransitionUtils.getDSlices(rp.getVMActions()));
                        s.post(IntConstraintFactory.atmost_nvalues(hosters, nbNodes, true));
                        rp.setObjective(true, nbNodes);
                        return true;
                    }

                    public Set<VM> getMisPlacedVMs(Model m) {
                        return Collections.emptySet();
                    }
                };
            }
        });

        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>emptyList(), new Foo());
        Mapping res = p.getResult().getMapping();
        int nbRunning = 0;
        for (Node n : res.getOnlineNodes()) {
            if (!res.getRunningVMs(n).isEmpty()) {
                nbRunning++;
            }
        }
        Assert.assertEquals(nbRunning, 1);
        SolvingStatistics st = cra.getStatistics();
        System.out.println(st);
        Assert.assertEquals(st.getSolutions().get(0).getOptValue(), 1);
        Assert.assertEquals(st.getSolutions().size(), 1);
    }

    @Test
    public void testSolvableRepair() throws SchedulerException {
        Model mo = new DefaultModel();
        final VM vm1 = mo.newVM();
        final VM vm2 = mo.newVM();
        final VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();

        new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1, vm4).run(n2, vm2).run(n3, vm3, vm5).get();

        //A satisfied constraint
        Fence c1 = new Fence(vm1, new HashSet<>(Arrays.asList(n1, n2)));

        //A constraint that is not satisfied. vm2 is misplaced
        Fence c2 = new Fence(vm2, new HashSet<>(Arrays.asList(n1, n3)));

        Set<SatConstraint> cstrs = new HashSet<SatConstraint>(Arrays.asList(c1, c2));
        mo = new DefaultModel();
        new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1, vm4).run(n2, vm2).run(n3, vm3, vm5).get();
        ChocoScheduler cra = new DefaultChocoScheduler();
        class Foo extends OptConstraint {
            @Override
            public String id() {
                return "foo";
            }
        }

        cra.getConstraintMapper().register(new ChocoConstraintBuilder() {
            @Override
            public Class<? extends Constraint> getKey() {
                return Foo.class;
            }

            @Override
            public ChocoConstraint build(Constraint cstr) {
                return new ChocoConstraint() {
                    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
                        return true;
                    }

                    public Set<VM> getMisPlacedVMs(Model m) {
                        return new HashSet<>(Arrays.asList(vm2, vm3));
                    }
                };
            }
        });
        cra.doRepair(true);
        cra.doOptimize(false);

        //Solve a problem with the repair mode
        Assert.assertNotNull(cra.solve(mo, cstrs, new Foo()));
        SolvingStatistics st = cra.getStatistics();
        System.out.println(st);
        Assert.assertEquals(st.getNbManagedVMs(), 2); //vm2, vm3.
    }

    @Test(expectedExceptions = {SchedulerException.class})
    public void testWithUnknownVMs() throws SchedulerException {
        Model mo = new DefaultModel();
        final VM vm1 = mo.newVM();
        final VM vm2 = mo.newVM();
        final VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        VM vm7 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        new MappingFiller(mo.getMapping()).on(n1, n2, n3).run(n1, vm1, vm4).run(n2, vm2).run(n3, vm3, vm5);
        SatConstraint cstr = mock(SatConstraint.class);
        when(cstr.getInvolvedVMs()).thenReturn(Arrays.asList(vm1, vm2, vm6));
        when(cstr.getInvolvedNodes()).thenReturn(Arrays.asList(n1, n4));
        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.solve(mo, Collections.singleton(cstr));
    }

    /**
     * Issue #14
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testNonHomogeneousIncrease() throws SchedulerException {
        ShareableResource cpu = new ShareableResource("cpu");
        ShareableResource mem = new ShareableResource("mem");
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();


        cpu.setCapacity(n1, 10);
        mem.setCapacity(n1, 10);
        cpu.setCapacity(n2, 10);
        mem.setCapacity(n2, 10);

        cpu.setConsumption(vm1, 5);
        mem.setConsumption(vm1, 4);

        cpu.setConsumption(vm2, 3);
        mem.setConsumption(vm2, 8);

        cpu.setConsumption(vm3, 5);
        cpu.setConsumption(vm3, 4);

        cpu.setConsumption(vm4, 4);
        cpu.setConsumption(vm4, 5);

        //vm1 requires more cpu resources, but fewer mem resources
        Preserve pCPU = new Preserve(vm1, "cpu", 7);
        Preserve pMem = new Preserve(vm1, "mem", 2);


        Mapping map = new MappingFiller(mo.getMapping()).on(n1, n2)
                .run(n1, vm1)
                .run(n2, vm3, vm4)
                .ready(vm2).get();

        mo.attach(cpu);
        mo.attach(mem);

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setMaxEnd(5);
        ReconfigurationPlan p = cra.solve(mo, Arrays.<SatConstraint>asList(pCPU, pMem,
                new Online(n1),
                new Running(vm2),
                new Ready(vm3)));
        Assert.assertNotNull(p);
    }

    /**
     * Remove the ready->running transition so the solving process will fail
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test(expectedExceptions = {SchedulerException.class})
    public void testTransitionFactoryCustomisation() throws SchedulerException {
        ChocoScheduler cra = new DefaultChocoScheduler();
        TransitionFactory tf = cra.getTransitionFactory();
        List<VMTransitionBuilder> b = tf.getBuilder(VMState.READY, VMState.RUNNING);
        Assert.assertTrue(tf.remove(b.get(0)));
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        mo.getMapping().addReadyVM(v);
        cra.solve(mo, Collections.<SatConstraint>singletonList(new Running(v)));
    }
}
