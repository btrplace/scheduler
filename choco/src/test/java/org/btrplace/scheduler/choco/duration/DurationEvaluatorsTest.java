/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;
import org.btrplace.scheduler.SchedulerException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ActionDurationEvaluator}.
 *
 * @author Fabien Hermenier
 */
public class DurationEvaluatorsTest {

    static Model mo = new DefaultModel();
    static VM vm1 = mo.newVM();
    static Node n1 = mo.newNode();

    @Test
    public void testInstantiateAndIsRegistered() throws SchedulerException {
        DurationEvaluators d = DurationEvaluators.newBundle();

        //Juste check an evaluator is registered for every possible action.
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
        DurationEvaluators d = DurationEvaluators.newBundle();
        Assert.assertTrue(d.unRegister(org.btrplace.plan.event.MigrateVM.class));
        Assert.assertFalse(d.isRegistered(org.btrplace.plan.event.MigrateVM.class));
        Assert.assertFalse(d.unRegister(org.btrplace.plan.event.MigrateVM.class));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"})
    public void testRegister() {
        DurationEvaluators d = new DurationEvaluators();
        Assert.assertTrue(d.register(org.btrplace.plan.event.MigrateVM.class, new ConstantActionDuration<>(7)));
        Assert.assertFalse(d.register(org.btrplace.plan.event.MigrateVM.class, new ConstantActionDuration<>(3)));
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister", "testRegister"})
    public void testGetEvaluator() {
        DurationEvaluators d = new DurationEvaluators();
        ActionDurationEvaluator<Element> ev = new ConstantActionDuration<>(7);
        d.register(org.btrplace.plan.event.MigrateVM.class, ev);
        Assert.assertEquals(d.getEvaluator(org.btrplace.plan.event.MigrateVM.class), ev);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister", "testRegister"})
    public void testEvaluate() throws SchedulerException {
        DurationEvaluators d = new DurationEvaluators();
        ActionDurationEvaluator<Element> ev = new ConstantActionDuration<>(7);
        d.register(org.btrplace.plan.event.MigrateVM.class, ev);

        Assert.assertEquals(d.evaluate(mo, org.btrplace.plan.event.MigrateVM.class, vm1), 7);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testUnregister"}, expectedExceptions = {SchedulerException.class})
    public void testEvaluateUnregisteredAction() throws SchedulerException {
        DurationEvaluators d = new DurationEvaluators();
        d.evaluate(mo, org.btrplace.plan.event.MigrateVM.class, vm1);
    }

    @Test(dependsOnMethods = {"testInstantiateAndIsRegistered", "testRegister"}, expectedExceptions = {SchedulerException.class})
    public void testEvaluateWithError() throws SchedulerException {
        DurationEvaluators d = new DurationEvaluators();
        d.register(org.btrplace.plan.event.MigrateVM.class, new ConstantActionDuration<>(-5));
        d.evaluate(mo, org.btrplace.plan.event.MigrateVM.class, vm1);
    }
}
