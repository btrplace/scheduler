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

package org.btrplace.json.plan;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link ActionConverter}.
 *
 * @author Fabien Hermenier
 */
public class ActionConverterTest {

    private static Model mo = new DefaultModel();
    private static VM vm1 = mo.newVM();
    private static VM vm2 = mo.newVM();
    private static VM vm3 = mo.newVM();
    private static Node n1 = mo.newNode();
    private static Node n2 = mo.newNode();

    @Test
    public void testMigrate() throws JSONConverterException, IOException {
        MigrateVM a = new MigrateVM(vm1, n1, n2, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
        a.setBandwidth(200);
        o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testBootVM() throws JSONConverterException, IOException {
        BootVM a = new BootVM(vm1, n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testKillVM() throws JSONConverterException, IOException {
        KillVM a = new KillVM(vm1, n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testAllocate() throws JSONConverterException, IOException {
        Allocate a = new Allocate(vm1, n1, "foo", 4, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testSuspendVM() throws JSONConverterException, IOException {
        SuspendVM a = new SuspendVM(vm1, n1, n2, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testResumeVM() throws JSONConverterException, IOException {
        ResumeVM a = new ResumeVM(vm1, n1, n2, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testForgeVM() throws JSONConverterException, IOException {
        ForgeVM a = new ForgeVM(vm1, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testShutdownVM() throws JSONConverterException, IOException {
        ShutdownVM a = new ShutdownVM(vm1, n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testShutdownNode() throws JSONConverterException, IOException {
        ShutdownNode a = new ShutdownNode(n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testBootNode() throws JSONConverterException, IOException {
        BootNode a = new BootNode(n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        System.out.println(o);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test(dependsOnMethods = "testBootNode")
    public void testEvents() throws JSONConverterException, IOException {
        BootNode a = new BootNode(n1, 3, 5);
        a.addEvent(Action.Hook.PRE, new AllocateEvent(vm1, "foo", 3));
        a.addEvent(Action.Hook.POST, new AllocateEvent(vm2, "bar", 5));
        a.addEvent(Action.Hook.POST, new SubstitutedVMEvent(vm2, vm3));
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        String o = ac.toJSONString(a);
        //System.out.println(o);
        //System.out.println(a);
        //System.out.println(ac.fromJSON(o));
        System.out.flush();
        Action a2 = ac.fromJSON(o);
        Assert.assertEquals(a, a2);
    }

    @Test
    public void testListSerialization() throws JSONConverterException, IOException {
        ActionConverter ac = new ActionConverter();
        ac.setModel(mo);
        List<Action> l = new ArrayList<>();
        l.add(new BootVM(vm1, n1, 0, 5));
        l.add(new ShutdownNode(n2, 0, 5));
        String jo = ac.toJSONString(l);
        List<Action> l2 = ac.listFromJSON(jo);
        Assert.assertTrue(l2.containsAll(l) && l2.size() == l.size());
    }
}
