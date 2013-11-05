package btrplace.solver.api.cstrSpec;

import btrplace.json.JSONConverterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

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
    public void testParseBan() {
        Constraint c = null;
        try {
            c = ex.extract(new File("src/test/resources/ban.cspec"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        UnitTestsGenerator gen = new UnitTestsGenerator();
        /* FileWriter out = new FileWriter("ban.tests");
        gen.generate(c, out);
        out.close();*/

        try {
            for (TestUnit t : gen.generate(c)) {
                TestResult res = t.verify();
                System.out.println(res);
            }
        } catch (JSONConverterException ex) {
            Assert.fail(ex.getMessage(), ex);
        } catch (IOException ex) {
            Assert.fail(ex.getMessage(), ex);
        }
        /*ModelGenerator gen = new ModelGenerator();
        List<Model> models = gen.all(VMType.getInstance().domain().size(), VMType.getInstance().domain().size());
        Proposition good = inv.getProposition();
        Proposition noGood = good.not();
        System.out.println("Good: " + good);
        System.out.println("No-good: " + noGood);
        for (Map<String, Object> vals : inv.expandParameters()) {
            System.out.println(vals);
            //Instantiate the good formula
            for (Model mo : models) {
                inv.instantiate(vals);
                Boolean gr = good.evaluate(mo);
                Boolean ngr = noGood.evaluate(mo);
                System.out.println("(Good) " + good + " == " + gr);
                System.out.println("(No good) " + noGood + " == " + ngr);
                if (gr == null || ngr == null) {
                    Assert.fail(mo.toString());
                }
                if (!(gr||ngr)) {
                    Assert.fail("Nor good or bad !\n" + mo.toString());
                }
                if (gr && ngr) {
                    Assert.fail("Good and bad !\n" + mo.toString());
                }
            }

        }*/
        //Assert.fail();
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
        ex.extract(new File("src/test/resources/noVMOnOfflineNode.cspec"));
        Assert.fail();
    }

}
