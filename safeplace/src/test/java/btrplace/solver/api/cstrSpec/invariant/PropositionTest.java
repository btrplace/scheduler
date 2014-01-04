package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.DefaultModel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class PropositionTest {

    @Test
    public void testTrue() {
        Proposition t = Proposition.True;
        Assert.assertEquals(t.not(), Proposition.False);
        Assert.assertEquals(t.eval(new DefaultModel()), Boolean.TRUE);
        Assert.assertEquals(t.toString(), "true");
    }

    @Test
    public void testFalse() {
        Proposition t = Proposition.False;
        Assert.assertEquals(t.toString(), "false");
        Assert.assertEquals(t.not(), Proposition.True);
        Assert.assertEquals(t.eval(new DefaultModel()), Boolean.FALSE);
    }
}
