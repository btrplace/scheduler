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

package org.btrplace.btrpsl;

import org.btrplace.btrpsl.element.BtrpElement;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.includes.BasicIncludes;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Gather;
import org.btrplace.model.constraint.RunningCapacity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for Script
 *
 * @author Fabien Hermenier
 */
@Test
public class ScriptTest {

    public void testId() {
        Script v = new Script();
        v.setFullyQualifiedName("foo");
        Assert.assertEquals(v.getlocalName(), "foo");
        Assert.assertEquals(v.getNamespace(), "");
        Assert.assertEquals(v.id(), "foo");
        v.setFullyQualifiedName("foo.bar");
        Assert.assertEquals(v.getlocalName(), "bar");
        Assert.assertEquals(v.getNamespace(), "foo");
        Assert.assertEquals(v.id(), "foo.bar");
    }

    public void testVMsAddition() {
        Script v = new Script();
        Model mo = new DefaultModel();
        BtrpElement vm1 = new BtrpElement(BtrpOperand.Type.VM, "VM1", mo.newVM());
        BtrpElement vm2 = new BtrpElement(BtrpOperand.Type.VM, "VM2", mo.newVM());
        BtrpElement vm3 = new BtrpElement(BtrpOperand.Type.VM, "VM3", mo.newVM());
        v.add(vm1);
        v.add(Arrays.asList(vm2, vm3));
        Assert.assertEquals(v.getVMs().size(), 3);
        Assert.assertTrue(v.getVMs().contains(vm1.getElement())
                && v.getVMs().contains(vm2.getElement())
                && v.getVMs().contains(vm3.getElement()));
    }

    public void testNodeAddition() {
        Script v = new Script();
        Model mo = new DefaultModel();
        BtrpElement n1 = new BtrpElement(BtrpOperand.Type.node, "@N1", mo.newNode());
        BtrpElement n2 = new BtrpElement(BtrpOperand.Type.node, "@N2", mo.newNode());

        v.add(n1);
        v.add(n2);
        Assert.assertEquals(v.getNodes().size(), 2);
        Assert.assertTrue(v.getNodes().contains(n1.getElement()) && v.getNodes().contains(n2.getElement()));

    }

    public void testConstraints() {
        Script v = new Script();
        Model mo = new DefaultModel();
        VM vm2 = mo.newVM();
        v.addConstraint(new Gather(Collections.singleton(vm2)));
        v.addConstraint(new RunningCapacity(Collections.singleton(mo.newNode()), 5));
        Assert.assertEquals(v.getConstraints().size(), 2);
    }

    public void testExported() {
        Script v = new Script();
        v.setFullyQualifiedName("testScript");
        Model mo = new DefaultModel();
        BtrpOperand o1 = new BtrpElement(BtrpOperand.Type.VM, "vm1", mo.newVM());
        v.addExportable("$s", o1, Collections.singleton("*"));

        //No restrictions, so every can access the exported variable
        Assert.assertEquals(v.getImportable("$s"), o1);
        Assert.assertTrue(v.canImport("$s", "foo"));
        Assert.assertTrue(v.canImport("$s", "bar.toto"));

        //Explicit no restriction
        BtrpOperand o2 = new BtrpElement(BtrpOperand.Type.VM, "vm2", mo.newVM());
        v.addExportable("$x", o2, Collections.singleton("*"));
        Assert.assertEquals(v.getImportable("$x"), o2);
        Assert.assertTrue(v.canImport("$x", "foo"));
        Assert.assertTrue(v.canImport("$x", "bar.toto"));

        //Unknown export
        Assert.assertFalse(v.canImport("$z", "foo"));
        Assert.assertFalse(v.canImport("$z", "bar.toto"));


        Set<String> valid = new HashSet<>();
        valid.add("foo");
        valid.add("foo.*");
        valid.add("bar");

        BtrpOperand o3 = new BtrpElement(BtrpOperand.Type.VM, "vm2", mo.newVM());
        v.addExportable("$y", o3, valid);
        Assert.assertNull(v.getImportable("$y"));

        Assert.assertEquals(v.getImportable("$y", "foo"), o3);

        Assert.assertNull(v.getImportable("$y", "zog"));

        Assert.assertEquals(v.getImportable("$y", "foo.bar.fii"), o3);

        Assert.assertEquals(v.getImportable("$y", "bar"), o3);

        Assert.assertEquals(v.getExported().size(), 3);
    }

    @Test
    public void testIncludeResolution() throws Exception {
        String include1 = "namespace foo.inc1;\n$myV = 3;\nexport $myV to *;";
        String include2 = "namespace foo.inc2;\n$myV = 3;\nexport $myV to *;";
        String script = "namespace foo.script;\nimport foo.inc1;\nimport foo.inc2;\n $myV = $foo.inc1.myV + $foo.inc2.myV; export $myV to *;";
        Model mo = new DefaultModel();
        ScriptBuilder builder = new ScriptBuilder(10, mo);
        BasicIncludes bi = new BasicIncludes();
        Script scr1 = builder.build(include1);
        Script scr2 = builder.build(include2);
        bi.add(scr1);
        bi.add(scr2);
        builder.setIncludes(bi);
        builder.build(script);
    }
}
