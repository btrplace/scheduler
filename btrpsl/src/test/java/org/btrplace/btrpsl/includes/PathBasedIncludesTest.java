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

package org.btrplace.btrpsl.includes;

import org.testng.annotations.Test;

/**
 * Unit tests for {@code PathBasedIncludes}.
 *
 * @author Fabien Hermenier
 */
@Test
public class PathBasedIncludesTest {
/*
    private static ScriptBuilder makeVJobBuilder() {
        VJobElementBuilder e = new DefaultVJobElementBuilder(null);
        Configuration cfg = new SimpleConfiguration();
        for (int i = 0; i < 100; i++) {
            cfg.addOnline(new SimpleNode("sol-" + i + ".sophia.grid5000.fr", 1, 1, 1));
            cfg.addOnline(new SimpleNode("helios-" + i + ".sophia.grid5000.fr", 1, 1, 1));
            cfg.addOnline(new SimpleNode("paravent-" + i + ".rennes.grid5000.fr", 1, 1, 1));
            cfg.addOnline(new SimpleNode("parapide-" + i + ".rennes.grid5000.fr", 1, 1, 1));

        }
        cfg.addOnline(new SimpleNode("frontend.sophia.grid5000.fr", 1, 1, 1));
        cfg.addOnline(new SimpleNode("frontend.rennes.grid5000.fr", 1, 1, 1));
        e.useConfiguration(cfg);
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        c.add(new RunningCapacityBuilder());
        return new ScriptBuilder(e, c);
    }

    public void testBasic() {
        ScriptBuilder b = makeVJobBuilder();
        PathBasedIncludes incs = new PathBasedIncludes(b, new File("src/test/resources/btrpsl/includes/i1"));
        b.setIncludes(incs);
        try {
            List<Script> res = incs.getVJob("sophia.sol");
            Assert.assertNotNull(res);
            Assert.assertEquals(res.size(), 1);

            res = incs.getVJob("sophia.helios");
            Assert.assertNotNull(res);
            Assert.assertEquals(res.size(), 1);


            res = incs.getVJob("sophia");
            Assert.assertNotNull(res);
            Assert.assertEquals(res.size(), 1);

            Assert.assertTrue(incs.addPath(new File("src/test/resources/btrpsl/includes/i2")));
            Assert.assertNotNull(incs.getVJob("rennes"));
            Assert.assertNotNull(incs.getVJob("rennes.parapide"));
            Assert.assertNotNull(incs.getVJob("rennes.paravent"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testCreationFromPaths() {
        try {
            ScriptBuilder b = makeVJobBuilder();
            PathBasedIncludes incs = PathBasedIncludes.fromPaths(b, "src/test/resources/btrpsl/includes/i1:src/test/resources/btrpsl/includes/i2");
            b.setIncludes(incs);
            List<File> path = incs.getPaths();
            Assert.assertEquals(path.size(), 2);
            Assert.assertEquals(path.get(0).getPath(), "src/test/resources/btrpsl/includes/i1");
            Assert.assertEquals(path.get(1).getPath(), "src/test/resources/btrpsl/includes/i2");
            Assert.assertNotNull(incs.getVJob("rennes"));
            Assert.assertNotNull(incs.getVJob("rennes.parapide"));
            Assert.assertNotNull(incs.getVJob("rennes.paravent"));

            Assert.assertNotNull(incs.getVJob("sophia"));
            Assert.assertNotNull(incs.getVJob("sophia.helios"));
            Assert.assertNotNull(incs.getVJob("sophia.sol"));

            Assert.assertNotNull(incs.toString());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testWildcard() {
        try {
            ScriptBuilder b = makeVJobBuilder();
            PathBasedIncludes incs = PathBasedIncludes.fromPaths(b, "src/test/resources/btrpsl/");
            b.setIncludes(incs);
            List<Script> res = incs.getVJob("includes.*");
            Assert.assertEquals(res.size(), 2);
            System.out.println(res);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    private static final VJobElementBuilder defaultEb = new DefaultVJobElementBuilder(new TemplateFactoryStub());

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testNonExistantImport() throws ScriptBuilderException {
        try {
            ScriptBuilder b = makeVJobBuilder();
            PathBasedIncludes incs = PathBasedIncludes.fromPaths(b, "src/test/resources/btrpsl");
            b.setIncludes(incs);
            VJob v = b.check("namespace baz; import toto; $nodes = @sol-[1..15].sophia.grid5000.fr;");
            System.out.println(v);
        } catch (ScriptBuilderException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testImportWithErrors() throws ScriptBuilderException {
        VJobElementBuilder e = defaultEb;
        Configuration cfg = new SimpleConfiguration();
        e.useConfiguration(cfg);
        for (int i = 1; i <= 10; i++) {
            cfg.addWaiting(new SimpleVirtualMachine("foo.VM" + i, 5, 5, 5));
        }
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        c.add(new RootBuilder());
        ScriptBuilder b = new ScriptBuilder(e, c);
        ErrorReporter r = null;
        //2 errors here,
        try {
            PathBasedIncludes includes = PathBasedIncludes.fromPaths(b, "src/test/resources/btrpsl/bads");
            b.setIncludes(includes);
            b.check("namespace bar;\n\n\n\nimport bad;\nimport ins.*;\n VM7: tiny;\nroot($bad); lonely($bad;");
        } catch (ScriptBuilderException ex) {
            r = ex.getErrorReporter();
            System.out.println(ex.getMessage());
            Assert.assertEquals(r.getErrors().size(), 6);
            throw ex;
        }
    }

    @Test(expectedExceptions = {ScriptBuilderException.class})
    public void testImportWithErrorsIntoClean() throws ScriptBuilderException {
        VJobElementBuilder e = defaultEb;
        Configuration cfg = new SimpleConfiguration();
        e.useConfiguration(cfg);
        for (int i = 1; i <= 10; i++) {
            cfg.addWaiting(new SimpleVirtualMachine("foo.VM" + i, 5, 5, 5));
        }
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        c.add(new RootBuilder());
        ScriptBuilder b = new ScriptBuilder(e, c);
        ErrorReporter r = null;
        //2 errors here,
        try {
            PathBasedIncludes includes = PathBasedIncludes.fromPaths(b, "src/test/resources/btrpsl/bads");
            b.setIncludes(includes);
            b.check("namespace bar;\n\n\n\nimport bad;\nimport ins.*;\n VM7: tiny;\nroot(VM7);");
        } catch (ScriptBuilderException ex) {
            r = ex.getErrorReporter();
            System.out.println(ex.getMessage());
            Assert.assertEquals(r.getErrors().size(), 2);
            throw ex;
        }
    }     */
}
