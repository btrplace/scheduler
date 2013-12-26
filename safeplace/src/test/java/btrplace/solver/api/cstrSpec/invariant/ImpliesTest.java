package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.DefaultModel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static btrplace.solver.api.cstrSpec.invariant.Proposition.False;
import static btrplace.solver.api.cstrSpec.invariant.Proposition.True;

/**
 * @author Fabien Hermenier
 */
public class ImpliesTest {

    @Test
    public void testInstantiation() {
        Implies i = new Implies(True, False);
        Assert.assertEquals(i.size(), 2);
    }

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {True, True, Boolean.TRUE},
                {True, False, Boolean.FALSE},
                {False, True, Boolean.TRUE},
                {False, False, Boolean.TRUE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        Implies p = new Implies(a, b);
        Assert.assertEquals(p.evaluate(new DefaultModel()), r);
    }

    @Test
    public void testNot() {
        Implies p = new Implies(True, False); //not(or(not(a),b)) -> and(a, not(b))
        And a = p.not();
        Assert.assertEquals(a.first(), True);
        Assert.assertEquals(a.second(), True);
    }
}
