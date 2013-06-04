package btrplace.solver.choco.chocoUtil;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class RoundedUpDivisionTest {

    @Test
    public void test1() {
        CPSolver s = new CPSolver();
        IntDomainVar a = s.createBoundIntVar("a", 0, 5);
        IntDomainVar b = s.createBoundIntVar("b", 0, 5);
        double q = 1;
        s.post(new RoundedUpDivision(a, b, q));
        Assert.assertEquals(Boolean.TRUE, s.solveAll());
        Assert.assertEquals(s.getNbSolutions(), 6);
    }

    @Test
    public void test2() {
        CPSolver s = new CPSolver();
        IntDomainVar a = s.createBoundIntVar("a", 0, 32);
        IntDomainVar b = s.createBoundIntVar("b", 0, 48);
        double q = 1.5;
        s.post(new RoundedUpDivision(a, b, q));
        Assert.assertEquals(Boolean.TRUE, s.solveAll());
        Assert.assertEquals(s.getNbSolutions(), 33);
    }
    /*
    private static void pretty(int a, int b, double q) {
        StringBuilder a1 = new StringBuilder();
        for (int i = 0; i < b; i++) {
            a1.append("(").append(i).append(",").append((int)Math.ceil(i / q)).append(") ");
        }
        System.err.println(a1.toString());
    } */
}
