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
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.AtMostNValue;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Unit tests for {@link DefaultChocoReconfigurationAlgorithm}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithmTest {

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
        };
        cra.setObjective(obj);
        Assert.assertEquals(cra.getObjective(), obj);
    }

    @Test
    public void testGetStatistics() throws SolverException {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        map.addOnlineNode(n1);
        for (int i = 0; i < 10; i++) {
            UUID n = UUID.randomUUID();
            UUID vm = UUID.randomUUID();
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
        });

        SolvingStatistics st = cra.getSolvingStatistics();
        Assert.assertEquals(st.getNbBacktracks(), 0);
        Assert.assertEquals(st.getNbNodes(), 0);
        Assert.assertEquals(st.getTime(), 0);
        Assert.assertTrue(st.getSolutions().isEmpty());
        Assert.assertFalse(st.isTimeout());

        ReconfigurationPlan p = cra.solve(mo, Collections.EMPTY_LIST);
        Mapping res = p.getResult().getMapping();
        Assert.assertEquals(MappingUtils.usedNodes(res, EnumSet.of(MappingUtils.State.Runnings)).size(), 1);
        st = cra.getSolvingStatistics();
        Assert.assertEquals(st.getSolutions().size(), 10);
    }
}
