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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.solver.SolverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link DefaultReconfigurationProblemBuilder}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithmBuilderTest {

    private static UUID nOn1 = UUID.randomUUID();
    private static UUID nOn2 = UUID.randomUUID();
    private static UUID nOff = UUID.randomUUID();

    private static UUID vm1 = UUID.randomUUID();
    private static UUID vm2 = UUID.randomUUID();
    private static UUID vm3 = UUID.randomUUID();
    private static UUID vm4 = UUID.randomUUID();
    private static UUID vm5 = UUID.randomUUID();
    private static UUID vm6 = UUID.randomUUID();
    private static UUID vm7 = UUID.randomUUID();


    private static Model defaultModel() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(nOn1);
        map.addOnlineNode(nOn2);
        map.addOfflineNode(nOff);

        map.addRunningVM(vm1, nOn1);
        map.addRunningVM(vm2, nOn1);
        map.addRunningVM(vm3, nOn2);
        map.addSleepingVM(vm4, nOn2);
        map.addReadyVM(vm5);
        map.addReadyVM(vm6);
        return new DefaultModel(map);
    }

    @Test(groups = {"DefaultRPBuilder"})
    public void testBasic() throws SolverException {
        Model m = defaultModel();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m).build();
        Assert.assertNotNull(rp.getDurationEvaluators());
        Assert.assertTrue(rp.getFutureKilledVMs().isEmpty());
        Assert.assertEquals(m.getMapping().getRunningVMs(), rp.getFutureRunningVMs());
        Assert.assertEquals(m.getMapping().getSleepingVMs(), rp.getFutureSleepingVMs());
        Assert.assertEquals(m.getMapping().getReadyVMs(), rp.getFutureReadyVMs());
        Assert.assertEquals(m.getMapping().getAllVMs(), rp.getManageableVMs());
    }

    @Test(groups = {"DefaultRPBuilder"}, dependsOnMethods = {"testBasic"})
    public void testWithDurations() throws SolverException {
        Model m = defaultModel();
        DurationEvaluators d = new DurationEvaluators();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m).setDurationEvaluatators(d).build();
        Assert.assertEquals(d, rp.getDurationEvaluators());
        Assert.assertTrue(rp.getFutureKilledVMs().isEmpty());
        Assert.assertEquals(m.getMapping().getRunningVMs(), rp.getFutureRunningVMs());
        Assert.assertEquals(m.getMapping().getSleepingVMs(), rp.getFutureSleepingVMs());
        Assert.assertEquals(m.getMapping().getReadyVMs(), rp.getFutureReadyVMs());
        Assert.assertEquals(m.getMapping().getAllVMs(), rp.getManageableVMs());
    }

    @Test(groups = {"DefaultRPBuilder"}, dependsOnMethods = {"testBasic"})
    public void testManageableVMs() throws SolverException {
        Model m = defaultModel();
        Set<UUID> man = m.getMapping().getRunningVMs();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m).setManageableVMs(man).build();
        Assert.assertTrue(rp.getFutureKilledVMs().isEmpty());
        Assert.assertEquals(m.getMapping().getRunningVMs(), rp.getFutureRunningVMs());
        Assert.assertEquals(m.getMapping().getSleepingVMs(), rp.getFutureSleepingVMs());
        Assert.assertEquals(m.getMapping().getReadyVMs(), rp.getFutureReadyVMs());
        Assert.assertEquals(man, rp.getManageableVMs());
    }

    @Test(groups = {"DefaultRPBuilder"}, dependsOnMethods = {"testBasic"})
    public void testLabelling() throws SolverException {
        Model m = defaultModel();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m).labelVariables().build();
        Assert.assertEquals(true, rp.isVarLabelling());
    }

    @Test(groups = {"DefaultRPBuilder"}, dependsOnMethods = {"testBasic"})
    public void testWithNextState() throws SolverException {
        Model m = defaultModel();

        Set<UUID> toRun = new HashSet<UUID>();
        Set<UUID> toWait = new HashSet<UUID>();
        toWait.add(vm6);
        toWait.add(vm7);
        toRun.add(vm5);
        toRun.add(vm4);
        toRun.add(vm1);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(m)
                .setNextVMsStates(toWait, toRun, Collections.singleton(vm3), Collections.singleton(vm2))
                .build();
        Assert.assertEquals(rp.getFutureReadyVMs(), toWait);
        Assert.assertEquals(rp.getFutureRunningVMs(), toRun);
        Assert.assertEquals(rp.getFutureSleepingVMs(), Collections.singleton(vm3));
        Assert.assertEquals(rp.getFutureKilledVMs(), Collections.singleton(vm2));
    }
}
