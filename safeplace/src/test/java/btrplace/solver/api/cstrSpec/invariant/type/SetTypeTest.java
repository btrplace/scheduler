package btrplace.solver.api.cstrSpec.invariant.type;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class SetTypeTest {

    @Test
    public void testSimple() {
        SetType t = new SetType(NatType.getInstance());
        System.out.println(t);
        Assert.assertEquals(t.enclosingType(), NatType.getInstance());
        SetType t2 = new SetType(t);
        Assert.assertEquals(t2.enclosingType(), t);
        System.out.println(t2);
    }
}
