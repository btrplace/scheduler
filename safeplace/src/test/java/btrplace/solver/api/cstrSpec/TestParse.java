package btrplace.solver.api.cstrSpec;

import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.generator.DelaysGenerator;
import btrplace.solver.api.cstrSpec.generator.DurationsGenerator;
import btrplace.solver.api.cstrSpec.generator.ModelsGenerator;
import btrplace.solver.api.cstrSpec.generator.ReconfigurationPlansGenerator;
import btrplace.solver.api.cstrSpec.invariant.StatesExtractor2;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class TestParse {

    StatesExtractor2 ex = new StatesExtractor2();

    private void go(String path) throws Exception {

        Constraint c = ex.extract(new File(path));

        ModelsGenerator mg = new ModelsGenerator(2, 2);
        ConstraintsConverter cstrC = ConstraintsConverter.newBundle();
        Verifier verifChk = new ImplVerifier();
        int num = 0, failures = 0;
        for (Model mo : mg) {
            System.out.print("-- Model " + mg.done() + "/" + mg.count());
            ReconfigurationPlansGenerator pg = new ReconfigurationPlansGenerator(mo);
            System.out.println(" " + pg.count() + " plan(s) --");
            int k = 0;
            for (ReconfigurationPlan p : pg) {
                ReconfigurationPlan p2 = new DelaysGenerator(new DurationsGenerator(p, 1, 3, true).next(), true).next();
                System.out.print(".");
                ConstraintInputGenerator cg = new ConstraintInputGenerator(c, p.getOrigin(), true);
                //System.out.println(c + "\n" + cg.count() + " signature(s)\n");
                for (Map<String, Object> in : cg) {
                    cstrC.setModel(p2.getOrigin());
                    SatConstraint satCstr = (SatConstraint) cstrC.fromJSON(JSONs.unMarshal(c.getMarshal(), in));
                    TestResult res = verifChk.verify(new TestCase(num, p2, satCstr, c.instantiate(in, p)));
                    if (!res.succeeded()) {
                        failures++;
                        System.out.println("\n" + res);
                        //Assert.fail();
                    }
                    num++;
                }
                cg.reset();
                if (++k % 80 == 0) {
                    System.out.println();
                }
            }
            System.out.println();
        }
        System.out.println("\n" + num + " verification(s) performed: " + failures + " failures");
        Assert.fail();
    }

    /*@Test
    public void testParseSpread() throws Exception {
        ex.extract(new File("src/test/resources/spread.cspec"));
    }

    @Test
    public void testSingleRunningCapacity() throws Exception {
        System.out.println(ex.extract(new File("src/test/resources/singleRunningCapacity.cspec")));
    }

    @Test
    public void testCumulatedRunningCapacity() throws Exception {
        System.out.println(ex.extract(new File("src/test/resources/cumulatedRunningCapacity.cspec")));
    }


    @Test
    public void testParseGather() throws Exception {
        ex.extract(new File("src/test/resources/gather.cspec"));
    }

    @Test
    public void testParseLonely() throws Exception {
        System.out.println(ex.extract(new File("src/test/resources/lonely.cspec")));
    }


    @Test
    public void testParseFence() throws Exception {
        ex.extract(new File("src/test/resources/fence.cspec"));
    }

    @Test
    public void testParseBan() throws Exception {
        ex.extract(new File("src/test/resources/ban.cspec"));

    }

    @Test
    public void testParseMaxOnline() throws Exception {
        System.out.println(ex.extract(new File("src/test/resources/maxOnline.cspec")));

    }

    @Test
    public void testParseRoot() throws Exception {
        Constraint cstr = ex.extract(new File("src/test/resources/root.cspec"));
        System.out.println(cstr.toString());

    }

    @Test
    public void testParseAmong() throws Exception {
        Constraint cstr = ex.extract(new File("src/test/resources/among.cspec"));
        System.out.println(cstr.toString());

    }

             */
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
                long vmp = (long) Math.pow(2 * q + 1, nbVMs);
                long np = C(nbNodes, q);
                long r = vmp * np;
                nbModels += r;
            }
            System.err.println("Nb of models having " + nbNodes + " nodes and " + nbVMs + " VMs: " + nbModels);
        }
    }

    public static long C(int n, int k) {
        return facto(n) / (facto(k) * facto(n - k));
    }

    public static long facto(int n) {
        long r = 1;
        while (n > 1) {
            r *= n--;
        }
        return r;
    }

    /*@DataProvider(name = "specs")
    public Object[][] getSpecs() {
        return new String[][]{
                {"src/test/resources/ban.cspec"},
                {"src/test/resources/cumulatedRunningCapacity.cspec"},
                {"src/test/resources/fence.cspec"},
                {"src/test/resources/gather.cspec"},
                {"src/test/resources/lonely.cspec"},
                {"src/test/resources/maxOnline.cspec"},
                {"src/test/resources/root.cspec"},
                {"src/test/resources/singleRunningCapacity.cspec"},
                {"src/test/resources/noVMonOfflineNode.cspec"},
        };
    }

    @Test(dataProvider = "specs")
    public void testExtraction(String path) throws Exception {
        Constraint cstr = ex.extract(new File(path));
        System.out.println(cstr);
        System.out.flush();
    }
                 */
    @Test
    public void testV1() throws Exception {
        List<Constraint> cstrs = ex.extractConstraints(new File("src/test/resources/v1.cspec"));
        for (Constraint cstr : cstrs) {
            System.out.println(cstr);
        }
        System.out.flush();
        Assert.assertEquals(cstrs.size(), 16);
    }
}
