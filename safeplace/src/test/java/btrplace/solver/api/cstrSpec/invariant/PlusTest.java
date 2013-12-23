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
public class PlusTest {

    @Test
    public void testInts() {
        Plus p = new Plus(NatType.getInstance().newValue(5), NatType.getInstance().newValue(7));
        Assert.assertEquals(p.getValue(new DefaultModel()), 12);
        Assert.assertEquals(p.type(), NatType.getInstance());
    }

    @Test
    public void testCollections() {
        Value v1 = new Value(Arrays.asList(1, 2), new SetType(NatType.getInstance()));
        Value v2 = new Value(Arrays.asList(4, 5), new SetType(NatType.getInstance()));
        Plus p = new Plus(v1, v2);
        Set s = (Set) p.getValue(new DefaultModel());
        Assert.assertEquals(s.size(), 4);
        Assert.assertEquals(p.type(), new SetType(NatType.getInstance()));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testBadCollections() throws RuntimeException {
        Value v1 = new Value(Arrays.asList(1, 2), new SetType(NatType.getInstance()));
        Value v2 = new Value(Arrays.asList(VMStateType.getInstance().newValue("running")), new SetType(VMStateType.getInstance()));
        new Plus(v1, v2);
    }

    @Test
    public void testPlusPlus() {
        Plus p1 = new Plus(NatType.getInstance().newValue(5), NatType.getInstance().newValue(7));
        Plus p2 = new Plus(NatType.getInstance().newValue(1), NatType.getInstance().newValue(2));
        Plus p3 = new Plus(p1, p2);
        Assert.assertEquals(p3.getValue(new DefaultModel()), 15);
    }

}
