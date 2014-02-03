package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.spec.prop.Iff;
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
public class IffTest {

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {True, True, Boolean.TRUE},
                {True, False, Boolean.FALSE},
                {False, True, Boolean.FALSE},
                {False, False, Boolean.TRUE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        Iff p = new Iff(a, b);
        Assert.assertEquals(p.eval(new SpecModel()), r);
    }
}
