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
public class AndTest {

    @Test
    public void testInstantiation() {
        And a = new And().add(False).add(Proposition.True);
        Assert.assertEquals(a.size(), 2);
        Assert.assertEquals(a.get(0), False);
        Assert.assertEquals(a.get(1), True);
    }

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {True, True, Boolean.TRUE},
                {True, False, Boolean.FALSE},
                {False, True, Boolean.FALSE},
                {False, False, Boolean.FALSE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        And p = new And().add(a).add(b);
        Assert.assertEquals(p.evaluate(new DefaultModel()), r);
    }

    @Test
    public void testNot() {
        And and = new And().add(True).add(False);
        Or o = and.not();
        Assert.assertEquals(o.get(0), False);
        Assert.assertEquals(o.get(1), True);
    }
}
