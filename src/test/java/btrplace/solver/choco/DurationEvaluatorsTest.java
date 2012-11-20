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

import btrplace.plan.SolverException;
import btrplace.plan.action.*;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link DurationEvaluator}.
 *
 * @author Fabien Hermenier
 */
public class DurationEvaluatorsTest {

    @Test
    public void testInstantiateAndIsRegistered() {
        DurationEvaluators d = new DurationEvaluators();

        //Juste check an evaluator is registered for every possible action.
        Assert.assertTrue(d.isRegistered(MigrateVM.class));
        Assert.assertTrue(d.isRegistered(SuspendVM.class));
        Assert.assertTrue(d.isRegistered(ResumeVM.class));
        Assert.assertTrue(d.isRegistered(InstantiateVM.class));
        Assert.assertTrue(d.isRegistered(BootVM.class));
        Assert.assertTrue(d.isRegistered(ShutdownVM.class));
        Assert.assertTrue(d.isRegistered(BootNode.class));
        Assert.assertTrue(d.isRegistered(ShutdownNode.class));
        Assert.assertTrue(d.isRegistered(BootNode.class));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered"})
    public void testUnregister() {
        DurationEvaluators d = new DurationEvaluators();
        Assert.assertTrue(d.unregister(MigrateVM.class));
        Assert.assertFalse(d.isRegistered(MigrateVM.class));
        Assert.assertFalse(d.unregister(MigrateVM.class));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"})
    public void testRegister() {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(MigrateVM.class);
        Assert.assertTrue(d.register(MigrateVM.class, new ConstantDuration(7)));
        Assert.assertFalse(d.register(MigrateVM.class, new ConstantDuration(3)));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister", "testRegister"})
    public void testGetEvaluator() {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(MigrateVM.class);
        DurationEvaluator ev = new ConstantDuration(7);
        d.register(MigrateVM.class, ev);
        Assert.assertEquals(d.getEvaluator(MigrateVM.class), ev);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister", "testRegister"})
    public void testEvaluate() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        DurationEvaluator ev = new ConstantDuration(7);
        d.register(MigrateVM.class, ev);
        Assert.assertEquals(d.evaluate(MigrateVM.class, UUID.randomUUID()), 7);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"}, expectedExceptions = {SolverException.class})
    public void testBadEvaluate() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(MigrateVM.class);
        Assert.assertEquals(d.evaluate(MigrateVM.class, UUID.randomUUID()), -1);
    }
}
