package btrplace.solver.api.cstrSpec;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Fabien Hermenier
 */
public class TestParse {

    StatesExtractor ex = new StatesExtractor();

    @Test
    public void testParseSpread() throws Exception {
        ex.extract(new File("src/test/resources/spread.cspec"));
        Assert.fail();
    }

    @Test
    public void testParseFence() {
        Constraint c = null;
        try {
            c = ex.extract(new File("src/test/resources/fence.cspec"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        UnitTestsGenerator gen = new UnitTestsGenerator();
        UnitTestsExecutor exe  = new UnitTestsExecutor();
        try {
            exe.execute(gen.generate(c));
            System.out.println(exe);
            if (!exe.getFailures().isEmpty()) {
                for (TestResult r : exe.getFailures()) {
                    System.out.println(r);
                }
                Assert.fail();
            }
        } catch (Exception ex) {
            Assert.fail(ex.getMessage(), ex);
        }
    }

    @Test
    public void testParseBan() {
        Constraint c = null;
        try {
            c = ex.extract(new File("src/test/resources/ban.cspec"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        UnitTestsGenerator gen = new UnitTestsGenerator();
        UnitTestsExecutor exe = new UnitTestsExecutor();
        try {
            exe.execute(gen.generate(c));
            System.out.println(exe);
            if (!exe.getFailures().isEmpty()) {
                for (TestResult r : exe.getFailures()) {
                    System.out.println(r);
                }
                Assert.fail();
            }
        } catch (Exception ex) {
            Assert.fail(ex.getMessage(), ex);
        }
    }

    @Test
    public void testParseMaxOnline() throws Exception {
        ex.extract(new File("src/test/resources/maxOnline.cspec"));
        Assert.fail();
    }

    @Test
    public void testParseNodeState() throws Exception {
        ex.extract(new File("src/test/resources/nodeState.cspec"));
        Assert.fail();
    }

    @Test
    public void testParseHost() throws Exception {
        ex.extract(new File("src/test/resources/host.cspec"));
        Assert.fail();
    }

    @Test
    public void testParseNoVMOnOfflineNode() throws Exception {
        Constraint c = null;
        try {
            c = ex.extract(new File("src/test/resources/noVMOnOfflineNode.cspec"));
            System.err.println(c);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        UnitTestsGenerator gen = new UnitTestsGenerator();
        UnitTestsExecutor exe = new UnitTestsExecutor();
        try {
            exe.execute(gen.generate(c));
            System.out.println(exe);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage(), ex);
        }
    }

}
