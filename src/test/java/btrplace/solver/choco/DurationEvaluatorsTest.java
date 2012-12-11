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

import btrplace.solver.SolverException;
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
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.MigrateVM.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.SuspendVM.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.ResumeVM.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.ForgeVM.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.BootVM.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.ShutdownVM.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.BootNode.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.ShutdownNode.class));
        Assert.assertTrue(d.isRegistered(btrplace.plan.event.BootNode.class));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered"})
    public void testUnregister() {
        DurationEvaluators d = new DurationEvaluators();
        Assert.assertTrue(d.unregister(btrplace.plan.event.MigrateVM.class));
        Assert.assertFalse(d.isRegistered(btrplace.plan.event.MigrateVM.class));
        Assert.assertFalse(d.unregister(btrplace.plan.event.MigrateVM.class));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"})
    public void testRegister() {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(btrplace.plan.event.MigrateVM.class);
        Assert.assertTrue(d.register(btrplace.plan.event.MigrateVM.class, new ConstantDuration(7)));
        Assert.assertFalse(d.register(btrplace.plan.event.MigrateVM.class, new ConstantDuration(3)));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister", "testRegister"})
    public void testGetEvaluator() {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(btrplace.plan.event.MigrateVM.class);
        DurationEvaluator ev = new ConstantDuration(7);
        d.register(btrplace.plan.event.MigrateVM.class, ev);
        Assert.assertEquals(d.getEvaluator(btrplace.plan.event.MigrateVM.class), ev);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister", "testRegister"})
    public void testEvaluate() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        DurationEvaluator ev = new ConstantDuration(7);
        d.register(btrplace.plan.event.MigrateVM.class, ev);
        Assert.assertEquals(d.evaluate(btrplace.plan.event.MigrateVM.class, UUID.randomUUID()), 7);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"}, expectedExceptions = {SolverException.class})
    public void testEvaluateUnregisteredAction() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(btrplace.plan.event.MigrateVM.class);
        d.evaluate(btrplace.plan.event.MigrateVM.class, UUID.randomUUID());
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testRegister"}, expectedExceptions = {SolverException.class})
    public void testEvaluateWithError() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        d.register(btrplace.plan.event.MigrateVM.class, new ConstantDuration(-5));
        d.evaluate(btrplace.plan.event.MigrateVM.class, UUID.randomUUID());
    }
}
