package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.spec.prop.And;
import btrplace.solver.api.cstrSpec.spec.prop.Or;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static btrplace.solver.api.cstrSpec.spec.prop.Proposition.False;
import static btrplace.solver.api.cstrSpec.spec.prop.Proposition.True;

/**
 * @author Fabien Hermenier
 */
public class OrTest {

    @Test
    public void testInstantiation() {
        Or a = new Or(False, True);
        Assert.assertEquals(a.first(), False);
        Assert.assertEquals(a.second(), True);
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
        Or p = new Or(a, b);
        Assert.assertEquals(p.eval(new SpecModel()), r);
    }

    @Test
    public void testNot() {
        Or or = new Or(True, False);
        And o = or.not();
        Assert.assertEquals(o.first(), False);
        Assert.assertEquals(o.second(), True);
    }
}
