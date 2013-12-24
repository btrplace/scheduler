package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.DefaultModel;
import btrplace.solver.api.cstrSpec.invariant.type.NatType;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.VMStateType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class PlusTest {

    @Test
    public void testInts() {
        Plus p = new IntPlus(NatType.getInstance().newValue(5), NatType.getInstance().newValue(7));
        Assert.assertEquals(p.eval(new DefaultModel()), 12);
        Assert.assertEquals(p.type(), NatType.getInstance());
    }

    @Test
    public void testCollections() {
        Value v1 = new Value(new HashSet(Arrays.asList(1, 2)), new SetType(NatType.getInstance()));
        Value v2 = new Value(new HashSet(Arrays.asList(4, 5)), new SetType(NatType.getInstance()));
        Plus p = new SetPlus(v1, v2);
        Set s = (Set) p.eval(new DefaultModel());
        Assert.assertEquals(s.size(), 4);
        Assert.assertEquals(p.type(), new SetType(NatType.getInstance()));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testBadCollections() throws RuntimeException {
        Value v1 = new Value(new HashSet(Arrays.asList(1, 2)), new SetType(NatType.getInstance()));
        Value v2 = new Value(new HashSet(Arrays.asList(VMStateType.getInstance().newValue("running"))), new SetType(VMStateType.getInstance()));
        new SetPlus(v1, v2);
    }

    @Test
    public void testPlusPlus() {
        Plus p1 = new IntPlus(NatType.getInstance().newValue(5), NatType.getInstance().newValue(7));
        Plus p2 = new IntPlus(NatType.getInstance().newValue(1), NatType.getInstance().newValue(2));
        Plus p3 = new IntPlus(p1, p2);
        Assert.assertEquals(p3.eval(new DefaultModel()), 15);
    }

}
