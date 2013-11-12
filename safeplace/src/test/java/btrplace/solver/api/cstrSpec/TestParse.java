package btrplace.solver.api.cstrSpec;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Fabien Hermenier
 */
public class TestParse {

    StatesExtractor2 ex = new StatesExtractor2();

    private void go(String path) throws Exception {
        Constraint c =ex.extract(new File(path));
        System.out.println(c);
        UnitTestsGenerator gen = new UnitTestsGenerator();
        UnitTestsExecutor exe  = new UnitTestsExecutor();
            exe.execute(gen.generate(c));
            if (!exe.getFailures().isEmpty()) {
                for (TestResult r : exe.getFailures()) {
                    System.out.println(r);
                }
                Assert.fail(exe.toString());
            }
            System.out.println(exe);
    }

    @Test
    public void testParseSpread() throws Exception {
        go("src/test/resources/spread.cspec");
    }

    @Test
    public void testSingleRunningCapacity() throws Exception {
        go("src/test/resources/singleRunningCapacity.cspec");
    }

    @Test
    public void testParseGather() throws Exception {
        go("src/test/resources/gather.cspec");
    }

    @Test
    public void testParseLonely() throws Exception {
        go("src/test/resources/lonely.cspec");
    }


    @Test
    public void testParseFence() throws Exception {
            go("src/test/resources/fence.cspec");
    }

    @Test
    public void testParseBan() throws Exception {
        go("src/test/resources/ban.cspec");

    }

    @Test
    public void testParseMaxOnline() throws Exception {
        ex.extract(new File("src/test/resources/maxOnline.cspec"));
        Assert.fail();
    }

/*    @Test
    public void testParseNodeState() throws Exception {
        ex.extract(new File("src/test/resources/nodeState.cspec"));
        Assert.fail();
    }*/

    /*@Test
    public void testParseNoVMOnOfflineNode() throws Exception {
        go("src/test/resources/noVMOnOfflineNode.cspec");
    } */

    @Test
    public void testFoo() {
        for (int i = 1; i <= 5; i++) {
            int nbNodes = i;
            int nbVMs = i;
            long nbModels = 0;
            for (int q = 0; q <= nbNodes; q++) {
                long vmp = (long)Math.pow(2*q + 1, nbVMs);
                long np = C(nbNodes, q);
                long r = vmp * np;
                nbModels += r;
            }
            System.err.println("Nb of models having " + nbNodes + " nodes and " + nbVMs + " VMs: " + nbModels);
        }
        Assert.fail();
    }

    public static long C(int n, int k) {
        return facto(n) / (facto(k)*facto(n - k));
    }

    public static long facto(int n) {
        long r = 1;
        while (n > 1) {
            r *= n--;
        }
        return r;
    }
}
