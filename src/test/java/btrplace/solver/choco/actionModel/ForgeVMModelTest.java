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

package btrplace.solver.choco.actionModel;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.action.ForgeVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * Unit tests for {@link ForgeVMModel}.
 *
 * @author Fabien Hermenier
 */
public class ForgeVMModelTest {

    @Test
    public void testBasics() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID vm1 = UUID.randomUUID();
        Model mo = new DefaultModel(m);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ForgeVM.class, new ConstantDuration(7));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setDurationEvaluatators(dev)
                .setNextVMsStates(Collections.singleton(vm1), Collections.<UUID>emptySet(), Collections.<UUID>emptySet(), Collections.<UUID>emptySet())
                .build();
        ForgeVMModel ma = (ForgeVMModel) rp.getVMAction(vm1);
        Assert.assertEquals(vm1, ma.getVM());
        Assert.assertTrue(ma.getDuration().isInstantiatedTo(7));
        Assert.assertFalse(ma.getStart().isInstantiated());
        Assert.assertFalse(ma.getEnd().isInstantiated());
        Assert.assertTrue(ma.getState().isInstantiatedTo(0));
        Assert.assertNull(ma.getCSlice());
        Assert.assertNull(ma.getDSlice());

    }

}
