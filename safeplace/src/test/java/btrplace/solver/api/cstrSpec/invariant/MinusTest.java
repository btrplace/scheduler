package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.IntMinus;
import btrplace.solver.api.cstrSpec.spec.term.Minus;
import btrplace.solver.api.cstrSpec.spec.term.SetMinus;
import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
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
        Minus p = new IntMinus(IntType.getInstance().newValue(5), IntType.getInstance().newValue(7));
        Assert.assertEquals(p.eval(new SpecModel()), -2);
        Assert.assertEquals(p.type(), IntType.getInstance());
    }

    @Test
    public void testCollections() {
        Constant v1 = new Constant(Arrays.asList(1, 2), new SetType(IntType.getInstance()));
        Constant v2 = new Constant(Arrays.asList(2, 5), new SetType(IntType.getInstance()));
        Minus p = new SetMinus(v1, v2);
        Set s = (Set) p.eval(new SpecModel());
        Assert.assertEquals(s.size(), 1);
        Assert.assertEquals(p.type(), new SetType(IntType.getInstance()));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testBadCollections() throws RuntimeException {
        Constant v1 = new Constant(Arrays.asList(1, 2), new SetType(IntType.getInstance()));
        Constant v2 = new Constant(Arrays.asList(VMStateType.getInstance().newValue("running")), new SetType(VMStateType.getInstance()));
        new SetMinus(v1, v2);
    }

    @Test
    public void testMinusMinus() {
        Minus p1 = new IntMinus(IntType.getInstance().newValue(5), IntType.getInstance().newValue(7));
        Minus p2 = new IntMinus(IntType.getInstance().newValue(1), IntType.getInstance().newValue(2));
        Minus p3 = new IntMinus(p1, p2);
        Assert.assertEquals(p3.eval(new SpecModel()), -1);
    }

}
