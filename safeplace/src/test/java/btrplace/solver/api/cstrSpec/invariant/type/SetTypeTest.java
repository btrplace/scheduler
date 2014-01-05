package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class SetTypeTest {

    @Test
    public void testSimple() {
        SetType t = new SetType(IntType.getInstance());
        System.out.println(t);
        Assert.assertEquals(t.enclosingType(), IntType.getInstance());
        SetType t2 = new SetType(t);
        Assert.assertEquals(t2.enclosingType(), t);
        System.out.println(t2);
    }
}
