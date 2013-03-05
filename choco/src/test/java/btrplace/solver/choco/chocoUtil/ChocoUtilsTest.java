package btrplace.solver.choco.chocoUtil;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link ChocoUtils}.
 *
 * @author Fabien Hermenier
 */
public class ChocoUtilsTest {

    @Test
    public void testGetNextContiguous() throws ContradictionException {
        CPSolver s = new CPSolver();
        IntDomainVar v = s.createEnumIntVar("foo", 0, 100);

        //1-3 5 9 11-15 17 25-50
        v.remVal(0);
        int [] bounds = ChocoUtils.getNextContiguousValues(v, 0);
        System.out.println(ChocoUtils.prettyContiguous(v));
        Assert.assertEquals(bounds[0], 1);
        Assert.assertEquals(bounds[1], 100);

        v.remVal(4);
        bounds = ChocoUtils.getNextContiguousValues(v, 1);
        System.out.println(ChocoUtils.prettyContiguous(v));
        Assert.assertEquals(bounds[0], 1);
        Assert.assertEquals(bounds[1], 3);

        bounds = ChocoUtils.getNextContiguousValues(v, 4);
        System.out.println(ChocoUtils.prettyContiguous(v));
        Assert.assertEquals(bounds[0], 5);
        Assert.assertEquals(bounds[1], 100);

        v.removeInterval(18, 24, null, false);
        System.out.println(ChocoUtils.prettyContiguous(v));

        v.removeInterval(51, 100, null, false);
        System.out.println(ChocoUtils.prettyContiguous(v));

        v.removeInterval(6, 8, null, false);
        System.out.println(ChocoUtils.prettyContiguous(v));

        v.removeInterval(18, 24, null, false);
        System.out.println(ChocoUtils.prettyContiguous(v));


        v.removeInterval(10, 10, null, false);
        System.out.println(ChocoUtils.prettyContiguous(v));

        v.removeInterval(16, 16, null, false);
        System.out.println(ChocoUtils.prettyContiguous(v));

        Assert.fail();
    }
}
