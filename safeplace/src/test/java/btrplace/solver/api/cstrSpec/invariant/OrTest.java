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
public class OrTest {

    @Test
    public void testInstantiation() {
        Or a = new Or().add(False).add(Proposition.True);
        Assert.assertEquals(a.size(), 2);
        Assert.assertEquals(a.get(0), False);
        Assert.assertEquals(a.get(1), True);
    }

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {True, True, Boolean.TRUE},
                {True, False, Boolean.TRUE},
                {False, True, Boolean.TRUE},
                {False, False, Boolean.FALSE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        Or p = new Or().add(a).add(b);
        Assert.assertEquals(p.evaluate(new DefaultModel()), r);
    }

    @Test
    public void testNot() {
        Or or = new Or().add(True).add(False);
        And o = or.not();
        Assert.assertEquals(o.get(0), False);
        Assert.assertEquals(o.get(1), True);
    }
}
