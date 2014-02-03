package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.IntPlus;
import btrplace.solver.api.cstrSpec.spec.term.Plus;
import btrplace.solver.api.cstrSpec.spec.term.SetPlus;
import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
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
        Plus p = new IntPlus(IntType.getInstance().newValue(5), IntType.getInstance().newValue(7));
        Assert.assertEquals(p.eval(new SpecModel()), 12);
        Assert.assertEquals(p.type(), IntType.getInstance());
    }

    @Test
    public void testCollections() {
        Constant v1 = new Constant(new HashSet(Arrays.asList(1, 2)), new SetType(IntType.getInstance()));
        Constant v2 = new Constant(new HashSet(Arrays.asList(4, 5)), new SetType(IntType.getInstance()));
        Plus p = new SetPlus(v1, v2);
        Set s = (Set) p.eval(new SpecModel());
        Assert.assertEquals(s.size(), 4);
        Assert.assertEquals(p.type(), new SetType(IntType.getInstance()));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testBadCollections() throws RuntimeException {
        Constant v1 = new Constant(new HashSet(Arrays.asList(1, 2)), new SetType(IntType.getInstance()));
        Constant v2 = new Constant(new HashSet(Arrays.asList(VMStateType.getInstance().newValue("running"))), new SetType(VMStateType.getInstance()));
        new SetPlus(v1, v2);
    }

    @Test
    public void testPlusPlus() {
        Plus p1 = new IntPlus(IntType.getInstance().newValue(5), IntType.getInstance().newValue(7));
        Plus p2 = new IntPlus(IntType.getInstance().newValue(1), IntType.getInstance().newValue(2));
        Plus p3 = new IntPlus(p1, p2);
        Assert.assertEquals(p3.eval(new SpecModel()), 15);
    }

}
