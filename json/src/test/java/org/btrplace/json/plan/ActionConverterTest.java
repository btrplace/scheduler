/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.plan;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ActionConverter}.
 *
 * @author Fabien Hermenier
 */
public class ActionConverterTest {

    private static final Model mo = new DefaultModel();
    private static final VM vm1 = mo.newVM();
    private static final VM vm2 = mo.newVM();
    private static final VM vm3 = mo.newVM();
    private static final Node n1 = mo.newNode();
    private static final Node n2 = mo.newNode();

    private static final ActionConverter ac = new ActionConverter(mo);

    @Test
    public void testMigrate() throws JSONConverterException {
        MigrateVM a = new MigrateVM(vm1, n1, n2, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
        a.setBandwidth(200);
        o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testBootVM() throws JSONConverterException {
        BootVM a = new BootVM(vm1, n1, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testKillVM() throws JSONConverterException {
        KillVM a = new KillVM(vm1, n1, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testAllocate() throws JSONConverterException {
        Allocate a = new Allocate(vm1, n1, "foo", 4, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testSuspendVM() throws JSONConverterException {
        SuspendVM a = new SuspendVM(vm1, n1, n2, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testResumeVM() throws JSONConverterException {
        ResumeVM a = new ResumeVM(vm1, n1, n2, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testForgeVM() throws JSONConverterException {
        ForgeVM a = new ForgeVM(vm1, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testShutdownVM() throws JSONConverterException {
        ShutdownVM a = new ShutdownVM(vm1, n1, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testShutdownNode() throws JSONConverterException {
        ShutdownNode a = new ShutdownNode(n1, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testBootNode() throws JSONConverterException {
        BootNode a = new BootNode(n1, 3, 5);
        JSONObject o = ac.toJSON(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test(dependsOnMethods = "testBootNode")
    public void testEvents() throws JSONConverterException {
        BootNode a = new BootNode(n1, 3, 5);
        a.addEvent(Action.Hook.PRE, new AllocateEvent(vm1, "foo", 3));
        a.addEvent(Action.Hook.POST, new AllocateEvent(vm2, "bar", 5));
        a.addEvent(Action.Hook.POST, new SubstitutedVMEvent(vm2, vm3));
        JSONObject o = ac.toJSON(a);
        System.out.flush();
        Action a2 = ac.fromJSON(o);
        Assert.assertEquals(a, a2);
    }
}
