/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.plan.event.*;
import btrplace.solver.SolverException;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.solver.choco.durationEvaluator.DurationEvaluator}.
 *
 * @author Fabien Hermenier
 */
public class DurationEvaluatorsTest implements PremadeElements {

    @Test
    public void testInstantiateAndIsRegistered() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();

        //Juste check an evaluator is registered for every possible action.
        Model mo = new DefaultModel(new DefaultMapping());
        mo.getAttributes().put(vm1, "boot", 2);
        mo.getAttributes().put(vm1, "shutdown", 3);
        mo.getAttributes().put(vm1, "migrate", 4);
        mo.getAttributes().put(vm1, "suspend", 5);
        mo.getAttributes().put(vm1, "resume", 6);
        mo.getAttributes().put(vm1, "kill", 7);
        mo.getAttributes().put(vm1, "allocate", 8);
        mo.getAttributes().put(vm1, "forge", 9);
        mo.getAttributes().put(n1, "boot", 10);
        mo.getAttributes().put(n1, "shutdown", 11);

        Assert.assertEquals(d.evaluate(mo, BootVM.class, vm1), 2);
        Assert.assertEquals(d.evaluate(mo, ShutdownVM.class, vm1), 3);
        Assert.assertEquals(d.evaluate(mo, MigrateVM.class, vm1), 4);
        Assert.assertEquals(d.evaluate(mo, SuspendVM.class, vm1), 5);
        Assert.assertEquals(d.evaluate(mo, ResumeVM.class, vm1), 6);
        Assert.assertEquals(d.evaluate(mo, KillVM.class, vm1), 7);
        Assert.assertEquals(d.evaluate(mo, Allocate.class, vm1), 8);
        Assert.assertEquals(d.evaluate(mo, ForgeVM.class, vm1), 9);
        Assert.assertEquals(d.evaluate(mo, BootNode.class, n1), 10);
        Assert.assertEquals(d.evaluate(mo, ShutdownNode.class, n1), 11);
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
        Assert.assertEquals(d.evaluate(new DefaultModel(new DefaultMapping()), btrplace.plan.event.MigrateVM.class, vm1), 7);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"}, expectedExceptions = {SolverException.class})
    public void testEvaluateUnregisteredAction() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        d.unregister(btrplace.plan.event.MigrateVM.class);
        d.evaluate(new DefaultModel(new DefaultMapping()), btrplace.plan.event.MigrateVM.class, vm1);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testRegister"}, expectedExceptions = {SolverException.class})
    public void testEvaluateWithError() throws SolverException {
        DurationEvaluators d = new DurationEvaluators();
        d.register(btrplace.plan.event.MigrateVM.class, new ConstantDuration(-5));
        d.evaluate(new DefaultModel(new DefaultMapping()), btrplace.plan.event.MigrateVM.class, vm1);
    }
}
