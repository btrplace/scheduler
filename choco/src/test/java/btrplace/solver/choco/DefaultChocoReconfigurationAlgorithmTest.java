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

import btrplace.model.*;
import btrplace.model.constraint.Fence;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.AtMostNValue;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link DefaultChocoReconfigurationAlgorithm}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithmTest implements PremadeElements {

    @Test
    public void testGetsAndSets() {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        cra.setTimeLimit(10);
        Assert.assertEquals(cra.getTimeLimit(), 10);

        cra.setMaxEnd(-5);
        Assert.assertEquals(cra.getMaxEnd(), -5);

        cra.doOptimize(false);
        Assert.assertEquals(cra.doOptimize(), false);

        cra.repair(true);
        Assert.assertEquals(cra.repair(), true);

        cra.labelVariables(true);
        Assert.assertEquals(cra.areVariablesLabelled(), true);

        Assert.assertNotNull(cra.getViewMapper());
        ModelViewMapper m = new ModelViewMapper();
        cra.setViewMapper(m);
        Assert.assertEquals(cra.getViewMapper(), m);

        ReconfigurationObjective obj = new ReconfigurationObjective() {
            @Override
            public void inject(ReconfigurationProblem rp) throws SolverException {

            }

            @Override
            public Set<UUID> getMisPlacedVMs(Model m) {
                return Collections.emptySet();
            }
        };
        cra.setObjective(obj);
        Assert.assertEquals(cra.getObjective(), obj);
    }

    @Test
    public void testGetStatistics() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            UUID n = new UUID(2, i);
            UUID vm = new UUID(3, i);
            map.addOnlineNode(n);
            map.addRunningVM(vm, n);
        }
        Model mo = new DefaultModel(map);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.doOptimize(true);
        cra.setTimeLimit(0);
        cra.setObjective(new ReconfigurationObjective() {
            @Override
            public void inject(ReconfigurationProblem rp) throws SolverException {
                Mapping map = rp.getSourceModel().getMapping();
                CPSolver s = rp.getSolver();
                IntDomainVar nbNodes = s.createBoundIntVar("nbNodes", 1, map.getOnlineNodes().size());
                IntDomainVar[] hosters = SliceUtils.extractHosters(ActionModelUtils.getDSlices(rp.getVMActions()));
                s.post(new AtMostNValue(hosters, nbNodes));
                s.setObjective(nbNodes);
                s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
            }

            @Override
            public Set<UUID> getMisPlacedVMs(Model m) {
                return Collections.emptySet();
            }
        });

        SolvingStatistics st = cra.getSolvingStatistics();
        Assert.assertEquals(st.getNbBacktracks(), 0);
        Assert.assertEquals(st.getNbSearchNodes(), 0);
        Assert.assertEquals(st.getSolvingDuration(), 0);
        Assert.assertTrue(st.getSolutions().isEmpty());
        Assert.assertFalse(st.isTimeout());

        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>emptyList());
        Mapping res = p.getResult().getMapping();
        Assert.assertEquals(MappingUtils.usedNodes(res, EnumSet.of(MappingUtils.State.Runnings)).size(), 1);
        st = cra.getSolvingStatistics();
        Assert.assertEquals(st.getSolutions().size(), 10);
    }

    @Test
    public void testSolvableRepair() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3).run(n1, vm1, vm4).run(n2, vm2).run(n3, vm3, vm5).build();

        //A satisfied constraint
        Fence c1 = new Fence(new HashSet<UUID>(Arrays.asList(vm1, vm2)), new HashSet<UUID>(Arrays.asList(n1, n2)));

        //A constraint that is not satisfied. vm2 is misplaced
        Fence c2 = new Fence(new HashSet<UUID>(Arrays.asList(vm1, vm2)), new HashSet<UUID>(Arrays.asList(n1, n3)));

        ReconfigurationObjective o = new ReconfigurationObjective() {

            @Override
            public void inject(ReconfigurationProblem rp) throws SolverException {
                //Do noting.
            }

            @Override
            public Set<UUID> getMisPlacedVMs(Model m) {
                return new HashSet<UUID>(Arrays.asList(vm2, vm3));
            }
        };

        Set<SatConstraint> cstrs = new HashSet<SatConstraint>(Arrays.asList(c1, c2));
        Model mo = new DefaultModel(map);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.repair(true);
        cra.doOptimize(false);
        cra.setObjective(o);

        //Solve a problem with the repair mode
        Assert.assertNotNull(cra.solve(mo, cstrs));
        SolvingStatistics st = cra.getSolvingStatistics();
        Assert.assertEquals(st.getNbManagedVMs(), 2); //vm2, vm3.
    }
}
