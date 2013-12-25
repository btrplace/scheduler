package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.DefaultModel;
import btrplace.solver.api.cstrSpec.invariant.type.NatType;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.VMStateType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class MinusTest {

    @Test
    public void testInts() {
        Minus p = new Minus(NatType.getInstance().newValue(5), NatType.getInstance().newValue(7));
        Assert.assertEquals(p.eval(new DefaultModel()), -2);
        Assert.assertEquals(p.type(), NatType.getInstance());
    }

    @Test
    public void testCollections() {
        Constant v1 = new Constant(Arrays.asList(1, 2), new SetType(NatType.getInstance()));
        Constant v2 = new Constant(Arrays.asList(2, 5), new SetType(NatType.getInstance()));
        Minus p = new Minus(v1, v2);
        Set s = (Set) p.eval(new DefaultModel());
        Assert.assertEquals(s.size(), 1);
        Assert.assertEquals(p.type(), new SetType(NatType.getInstance()));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testBadCollections() throws RuntimeException {
        Constant v1 = new Constant(Arrays.asList(1, 2), new SetType(NatType.getInstance()));
        Constant v2 = new Constant(Arrays.asList(VMStateType.getInstance().newValue("running")), new SetType(VMStateType.getInstance()));
        new Minus(v1, v2);
    }

    @Test
    public void testMinusMinus() {
        Minus p1 = new Minus(NatType.getInstance().newValue(5), NatType.getInstance().newValue(7));
        Minus p2 = new Minus(NatType.getInstance().newValue(1), NatType.getInstance().newValue(2));
        Minus p3 = new Minus(p1, p2);
        Assert.assertEquals(p3.eval(new DefaultModel()), -1);
    }

}
