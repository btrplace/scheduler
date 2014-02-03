package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.spec.prop.And;
import btrplace.solver.api.cstrSpec.spec.prop.Implies;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static btrplace.solver.api.cstrSpec.spec.prop.Proposition.False;
import static btrplace.solver.api.cstrSpec.spec.prop.Proposition.True;

/**
 * @author Fabien Hermenier
 */
public class ImpliesTest {

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
        Assert.assertEquals(p.eval(new SpecModel()), r);
    }

    @Test
    public void testNot() {
        Implies p = new Implies(True, False); //not(or(not(a),b)) -> and(a, not(b))
        And a = p.not();
        Assert.assertEquals(a.first(), True);
        Assert.assertEquals(a.second(), True);
    }
}
