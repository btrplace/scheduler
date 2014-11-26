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

import org.btrplace.btrpsl.element.BtrpNumber;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.BtrpString;
import org.btrplace.btrpsl.includes.PathBasedIncludes;
import org.btrplace.btrpsl.template.DefaultTemplateFactory;
import org.btrplace.btrpsl.template.DefaultTemplateFactoryTest;
import org.btrplace.json.plan.ReconfigurationPlanConverter;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.NamingService;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Unit tests for {@link ScriptBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test(sequential = true)
public class ScriptBuilderTest {

    private static final String RC_ROOT = "src/test/resources/org/btrplace/btrpsl/";

    public void testNumberComputation() {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            Script v = b.build(new File(RC_ROOT + "number.btrp"));
            BtrpNumber x = (BtrpNumber) v.getImportable("$x");
            BtrpNumber y = (BtrpNumber) v.getImportable("$y");
            BtrpNumber z = (BtrpNumber) v.getImportable("$z");
            BtrpNumber a = (BtrpNumber) v.getImportable("$a");
            BtrpNumber b2 = (BtrpNumber) v.getImportable("$b");
            BtrpNumber c2 = (BtrpNumber) v.getImportable("$c");
            BtrpNumber toto = (BtrpNumber) v.getImportable("$toto");
            BtrpNumber titi = (BtrpNumber) v.getImportable("$titi");
            BtrpNumber foo = (BtrpNumber) v.getImportable("$foo");
            BtrpNumber bar = (BtrpNumber) v.getImportable("$bar");
            BtrpNumber bi = (BtrpNumber) v.getImportable("$bi");

            BtrpNumber f1 = (BtrpNumber) v.getImportable("$f1");
            BtrpNumber f2 = (BtrpNumber) v.getImportable("$f2");
            BtrpNumber f3 = (BtrpNumber) v.getImportable("$f3");


            //System.err.println(v);
            Assert.assertTrue(x.isInteger());
            Assert.assertEquals(x.getIntValue(), 2);

            Assert.assertTrue(y.isInteger());
            Assert.assertEquals(y.getIntValue(), 8);

            Assert.assertTrue(z.isInteger());
            Assert.assertEquals(z.getIntValue(), 26);

            Assert.assertTrue(a.isInteger());
            Assert.assertEquals(a.getIntValue(), 2);

            Assert.assertTrue(b2.isInteger());
            Assert.assertEquals(b2.getIntValue(), 0);

            Assert.assertTrue(c2.isInteger());
            Assert.assertEquals(c2.getIntValue(), 1);

            Assert.assertFalse(f1.isInteger());
            Assert.assertEquals(f1.getDoubleValue(), 0.7);

            Assert.assertFalse(f2.isInteger());
            Assert.assertEquals(f2.getDoubleValue(), 3.0);

            Assert.assertFalse(f3.isInteger());
            Assert.assertEquals(f3.getDoubleValue(), 892, 5);

            BtrpNumber baz = (BtrpNumber) v.getImportable("$baz");
            Assert.assertEquals(baz, BtrpNumber.TRUE);

            BtrpNumber biz = (BtrpNumber) v.getImportable("$biz");
            Assert.assertEquals(biz, BtrpNumber.TRUE);

            Assert.assertEquals(toto, BtrpNumber.FALSE);
            Assert.assertEquals(titi, BtrpNumber.TRUE);
            Assert.assertEquals(foo, BtrpNumber.TRUE);
            Assert.assertEquals(bar, BtrpNumber.TRUE);
            Assert.assertEquals(bi, BtrpNumber.FALSE);


        } catch (Exception x) {
            Assert.fail(x.getMessage(), x);
        }
    }

    public void testSetManipulation() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());

        Script v = b.build(new File(RC_ROOT + "setManip.btrp"));
        BtrpSet t1 = (BtrpSet) v.getImportable("$T1");
        BtrpSet t2 = (BtrpSet) v.getImportable("$T2");
        BtrpSet t3 = (BtrpSet) v.getImportable("$T3");
        BtrpNumber x = (BtrpNumber) v.getImportable("$x");
        BtrpNumber res = (BtrpNumber) v.getImportable("$res");
        BtrpNumber res2 = (BtrpNumber) v.getImportable("$res2");
        BtrpNumber res3 = (BtrpNumber) v.getImportable("$res3");
        BtrpNumber y = (BtrpNumber) v.getImportable("$y");

        Assert.assertEquals(t1.size() + t2.size() + t3.size(), 100);

        Assert.assertEquals(x.getIntValue(), 12);
        Assert.assertEquals(y.getIntValue(), 3);

        BtrpSet C = (BtrpSet) v.getImportable("$C");
        Assert.assertEquals(C.size(), 90);

        BtrpSet a = (BtrpSet) v.getImportable("$a");

        Assert.assertEquals(res, BtrpNumber.TRUE);
        Assert.assertEquals(res2, BtrpNumber.TRUE);
        Assert.assertEquals(res3, BtrpNumber.TRUE);

        Assert.assertEquals(a.degree(), 2);
        Assert.assertEquals(a.size(), 4);
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testSetManipulationWithErrors() throws ScriptBuilderException {
        try {
            ScriptBuilder b = new ScriptBuilder(new DefaultModel());
            b.build(
                    "namespace test.template;\n" +
                            "VM[1..20] : tinyVMs<migratable,volatile>;\n" +
                            "$x = VM[1..10] + VM15;\n" +
                            "$y = VM[1..10] + @N[1..20,57];\n" +
                            "$z = VM[1..10] + 7;\n" +
                            "$a = VM[1..10] - {VM[1..10]};\n" +
                            "$b = VM[1..10] / @N1;\n" +
                            "$c = VM[1..10] / @N[1,3];\n" +
                            "$d = VM[1..10] * VM[21,22];\n" +
                            "$e = VM[22,23] / 2;\n"
            );
        } catch (ScriptBuilderException x) {
            System.out.println(x);
            Assert.assertEquals(x.getErrorReporter().getErrors().size(), 8);
            throw x;
        }
    }

    public void testIfStatement() {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            Script v = b.build(new File(RC_ROOT + "ifStatement.btrp"));
            BtrpNumber first = (BtrpNumber) v.getImportable("$first");
            BtrpNumber second = (BtrpNumber) v.getImportable("$second");
            BtrpNumber third = (BtrpNumber) v.getImportable("$third");
            Assert.assertNotNull(first);
            Assert.assertNotNull(second);
            Assert.assertEquals(first, BtrpNumber.TRUE);
            Assert.assertEquals(second, BtrpNumber.TRUE);
            Assert.assertEquals(third.getIntValue(), 5);
        } catch (Exception x) {
            Assert.fail(x.getMessage(), x);
        }
    }


    /**
     * Test templates on VMs and nodes.
     */
    public void testTemplate1() throws ScriptBuilderException {
        Model mo = new DefaultModel();
        ScriptBuilder b = new ScriptBuilder(mo);
        Script v = b.build("namespace test.template;\nVM[1..5] : tinyVMs;\nfrontend : mediumVMs; @N[1..12] : defaultNodes;\n");
        Assert.assertEquals(v.getVMs().size(), 6);
        NamingService<Node> srvNodes = (NamingService<Node>) mo.getView(NamingService.ID + "node");
        NamingService<VM> srvVMs = (NamingService<VM>) mo.getView(NamingService.ID + "vm");
        for (VM el : v.getVMs()) {
            String name = srvVMs.resolve(el);
            if (name.endsWith("frontend")) {
                Assert.assertEquals(mo.getAttributes().get(el, "template"), "mediumVMs");
            } else {
                Assert.assertEquals(mo.getAttributes().get(el, "template"), "tinyVMs");
            }
        }

        Assert.assertEquals(v.getNodes().size(), 12);
        for (Node el : v.getNodes()) {
            Assert.assertEquals(mo.getAttributes().get(el, "template"), "defaultNodes");
        }
    }

    @Test
    public void testTemplateWithOptions() throws ScriptBuilderException {
        Model mo = new DefaultModel();
        ScriptBuilder b = new ScriptBuilder(mo);
        Script v = b.build("namespace test.template;\nVM[1..3] : tinyVMs<migratable,start=\"7.5\",stop=12>;");
        Assert.assertEquals(v.getVMs().size(), 3);
        for (VM el : v.getVMs()) {
            Assert.assertEquals(mo.getAttributes().getKeys(el).size(), 4); //3 + 1 (the template)
            Assert.assertEquals(mo.getAttributes().getBoolean(el, "migratable").booleanValue(), true);
            Assert.assertEquals(mo.getAttributes().getDouble(el, "start"), 7.5);
            Assert.assertEquals(mo.getAttributes().getInteger(el, "stop").longValue(), 12);
        }
    }


    public void testTemplate2() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        b.build("namespace test.template;\nVM[1..20] : tinyVMs<migratable,start=\"+7\",stop=12>;\nVMfrontend : mediumVMs;\n");

    }


    public void testLogical() {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            Script v = b.build(new File(RC_ROOT + "logical.btrp"));
            BtrpNumber and1 = (BtrpNumber) v.getImportable("$and1");
            BtrpNumber and2 = (BtrpNumber) v.getImportable("$and2");
            BtrpNumber and3 = (BtrpNumber) v.getImportable("$and3");
            BtrpNumber and4 = (BtrpNumber) v.getImportable("$and4");

            BtrpNumber or1 = (BtrpNumber) v.getImportable("$or1");
            BtrpNumber or2 = (BtrpNumber) v.getImportable("$or2");
            BtrpNumber or3 = (BtrpNumber) v.getImportable("$or3");
            BtrpNumber or4 = (BtrpNumber) v.getImportable("$or4");

            Assert.assertEquals(and1, BtrpNumber.FALSE);
            Assert.assertEquals(and2, BtrpNumber.FALSE);
            Assert.assertEquals(and3, BtrpNumber.FALSE);
            Assert.assertEquals(and4, BtrpNumber.TRUE);

            Assert.assertEquals(or1, BtrpNumber.TRUE);
            Assert.assertEquals(or2, BtrpNumber.TRUE);
            Assert.assertEquals(or3, BtrpNumber.TRUE);
            Assert.assertEquals(or4, BtrpNumber.FALSE);

            BtrpNumber h1 = (BtrpNumber) v.getImportable("$h1");
            BtrpNumber h2 = (BtrpNumber) v.getImportable("$h2");
            Assert.assertEquals(h1, BtrpNumber.TRUE);
            Assert.assertEquals(h2, BtrpNumber.TRUE);


        } catch (Exception x) {
            Assert.fail(x.getMessage(), x);
        }
    }

    public void textExportRestrictions() {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        PathBasedIncludes includes = new PathBasedIncludes(b, new File(RC_ROOT));
        b.setIncludes(includes);

        try {
            b.build("namespace zog; import testExport; for $n in $testExport.racks { }");
            b.build("namespace toto; import testExport; for $n in $testExport.nodes { }");
            b.build("namespace testExport.bla; import testExport; for $n in $testExport.nodes { } for $r in $testExport.racks {}");
            b.build("namespace sysadmin; import testExport; for $n in $testExport.nodes { } for $r in $testExport.racks {} for $n in $testExport {}");

        } catch (Exception x) {
            Assert.fail(x.getMessage(), x);
        }

        //Now, with bad restrictions
        try {
            b.build("namespace zog; import testExport; for $n in $nodes { }");
            Assert.fail();
        } catch (Exception x) {
        }

        try {
            b.build("namespace sysadmin.foo; import testExport; for $n in $nodes { }");
            Assert.fail();
        } catch (Exception x) {
        }

        try {
            b.build("namespace sysadmin.foo; import testExport; for $v in $testExport { }");
            Assert.fail();
        } catch (Exception x) {
        }
    }

    public void testMeUsage() {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            Script v = b.build("namespace foo; VM[1..5] : tiny;\nVM[6..10] : small;\n lonely($me); ");
            SatConstraint cs = v.getConstraints().iterator().next();
            Assert.assertEquals(cs.getInvolvedVMs().size(), 10);
        } catch (Exception x) {
            Assert.fail(x.getMessage(), x);
        }
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testMeReassignment() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        b.build("namespace foo; VM[1..5] : tiny;\nVM[6..10] : small;\n $me = 7; ");
    }

    public void testStringSupport() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Script v = b.build("namespace foo; VM[1..10] : tiny;\n$arr = {\"foo\",\"bar\", \"baz\"};$arr2 = $arr + {\"git\"}; $out = \"come \" + \"out \" + 5 + \" \" + VM1; export $arr2,$out to *;");
        BtrpString out = (BtrpString) v.getImportable("$out");
        BtrpSet arr2 = (BtrpSet) v.getImportable("$arr2");
        Assert.assertEquals(out.toString(), "come out 5 foo.VM1");
        Assert.assertEquals(arr2.size(), 4);
    }

    public void testLargeRange() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Script v = b.build("namespace foo; @N[1..500] : defaultNode;\n$all = @N[251..500]; export $all to *;");
        BtrpSet out = (BtrpSet) v.getImportable("$all");
        Assert.assertEquals(out.size(), 250);
    }

    public void testDependencies() throws Exception {
        //
        // a
        // |- b
        // \– c
        //    \-d
        //
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        PathBasedIncludes includes = new PathBasedIncludes(b, new File(RC_ROOT + "deps"));
        b.setIncludes(includes);

        Script v = b.build(new File(RC_ROOT + "deps/a.btrp"));
        Assert.assertEquals(v.getDependencies().size(), 2);
        for (Script s : v.getDependencies()) {
            if (s.getlocalName().equals("c")) {
                for (Script s2 : s.getDependencies()) {
                    Assert.assertTrue(s2.getlocalName().equals("foo") || s2.getlocalName().equals("bar"));
                }
            } else if (s.getlocalName().equals("b")) {
                for (Script s2 : s.getDependencies()) {
                    Assert.assertTrue(s2.getlocalName().equals("toto") || s2.getlocalName().equals("titi"));
                }
            } else {
                Assert.fail();
            }
        }
        System.out.println(v.prettyDependencies());
    }


    public void testVariablesInElementRange() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Script v = b.build(new File(RC_ROOT + "range.btrp"));
        BtrpSet s = (BtrpSet) v.getImportable("$foo");

        System.out.println(s);
        Assert.assertEquals(s.size(), 9);
    }

    @DataProvider(name = "badRanges")
    public Object[][] getBadRanges() {
        return new String[][]{
                //new String[]{"$a = VM[1..a];"},
                new String[]{"$a = VM[1..12];"},
                /*new String[]{"$a = VM[1..0xF];"},
                new String[]{"$a = VM[0xF..20];"},
                new String[]{"$a = VM[a..7];"},
                new String[]{"$a = VM[1.5..3];"},
                new String[]{"$a = @N[1..3.2];"},
                new String[]{"$a = @N[1..3.2];"},
                new String[]{"$a = @N[1..3.2];"},
                new String[]{"$a = @N[3,7,11,15];"},
                new String[]{"$a = @N[1..3,5..a];"},  */

        };
    }

    @Test(dataProvider = "badRanges", expectedExceptions = {ScriptBuilderException.class})
    public void testBadRanges(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..10] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.err.println(str + " " + ex.getMessage());
            System.err.flush();
            throw ex;
        }
        Assert.fail();
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testConstraintWithBadParameters() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        b.build("namespace foo; VM[1..10] : tiny;\nlonely(N15);");
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testWithLexerErrors() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        b.build("namespace foo; VM[1..10] : tiny;\nroot(VM10;");
    }

    public void testMissingEndl() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        ErrorReporter r = null;
        try {
            b.build("namespace foo; VM[1..10] : tiny;\nroot(VM10);root(VM9");
        } catch (ScriptBuilderException ex) {
            System.out.println(ex);
            r = ex.getErrorReporter();
            Assert.assertEquals(r.getErrors().size(), 1);
            Assert.assertEquals(r.getErrors().get(0).lineNo(), 2);
            Assert.assertTrue(r.getErrors().get(0).colNo() > 10);
        }
        Assert.assertNotNull(r);
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testReAssignment() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(100, new DefaultModel());
        ErrorReporter r;
        try {
            Script scr = b.build("namespace foo; @N[1,1] : tiny;");
            System.out.println(scr.getVMs());
        } catch (ScriptBuilderException ex) {
            System.out.println(ex);
            r = ex.getErrorReporter();
            Assert.assertEquals(r.getErrors().size(), 1);
            throw ex;
        }
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testVMReAssignment() throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(100, new DefaultModel());
        ErrorReporter r;
        try {
            Script scr = b.build("namespace foo; VM[1,1] : tiny;");
            System.out.println(scr.getVMs());
        } catch (ScriptBuilderException ex) {
            System.out.println(ex);
            r = ex.getErrorReporter();
            Assert.assertEquals(r.getErrors().size(), 1);
            throw ex;
        }
    }


    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testTemplateReassignment() throws ScriptBuilderException {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        VM v2 = mo.newVM();
        mo.getMapping().addReadyVM(v);
        mo.getMapping().addReadyVM(v2);
        mo.getAttributes().put(v, "template", "t1");
        mo.getAttributes().put(v2, "template", "tiny");
        NamingService<Node> nsNodes = NamingService.newNodeNS();
        NamingService<VM> nsVMs = NamingService.newVMNS();
        mo.attach(nsNodes);
        mo.attach(nsVMs);
        nsVMs.register(v, "foo.VM1");
        nsVMs.register(v2, "foo.VM2");
        ScriptBuilder b = new ScriptBuilder(100, mo);
        b.setTemplateFactory(new DefaultTemplateFactory(nsNodes, nsVMs, mo));
        b.getTemplateFactory().register(new DefaultTemplateFactoryTest.MockVMTemplate("tiny"));
        b.getTemplateFactory().register(new DefaultTemplateFactoryTest.MockVMTemplate("t1"));
        ErrorReporter r;
        try {
            Script scr = b.build("namespace foo; VM[1,2] : tiny;");
            System.out.println(scr.getVMs());
        } catch (ScriptBuilderException ex) {
            System.out.println(ex);
            r = ex.getErrorReporter();
            Assert.assertEquals(r.getErrors().size(), 1);
            throw ex;
        }
    }

    @Test
    public void testResolution() throws Exception {
        Model mo = new DefaultModel();
        ScriptBuilder b = new ScriptBuilder(mo);
        NamingService<Node> nsNodes = b.getNamingServiceNodes();
        NamingService<VM> nsVMs = b.getNamingServiceVMs();

        for (int i = 1; i < 10; i++) {
            if (i <= 5) {
                Node n = mo.newNode();
                mo.getMapping().addOnlineNode(n);
                nsNodes.register(n, "@N" + i);
            }
            VM v = mo.newVM();
            nsVMs.register(v, "ns.VM" + i);
            mo.getMapping().addReadyVM(v);
        }

        //TemplateFactory tpf = new DefaultTemplateFactory(b.getNamingService(), true);
        //b.setTemplateFactory(tpf);

        Script scr = b.build("namespace ns;\n"
                + "VM[1..10] : tiny;\n"
                + "@N[1..5]: default;\n"
                + "$vms = VM[1..10];\n"
                + "running($vms);\n"
                + ">>split($vms / 2);\n");

        Assert.assertEquals(mo.getMapping().getNbNodes(), 5);
        Assert.assertEquals(mo.getMapping().getNbVMs(), 10);
        ChocoScheduler ra = new DefaultChocoScheduler();
        ReconfigurationPlan p = ra.solve(mo, scr.getConstraints());
        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 10);
    }
}
