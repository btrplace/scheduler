package btrplace.solver.choco.extensions;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.search.loop.monitors.SMF;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;

/*
 * Created on 18/09/14.
 *
 * @author Sophie Demassey
 */public class FastImpliesEqTest {

    @Test
    public void test1() {
        Solver s = new Solver();
        //SMF.log(s, true, true);
        BoolVar b = VF.bool("b", s);
        IntVar x = VF.bounded("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(5, s.findAllSolutions());
    }

    @Test
    public void test2() {
        Solver s = new Solver();
        BoolVar b = VF.bool("b", s);
        IntVar x = VF.enumerated("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(5, s.findAllSolutions());
    }

    @Test
    public void test3() {
        Solver s = new Solver();
        BoolVar b = VF.bool("b", s);
        IntVar x = VF.bounded("x", 0, 2, s);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(3, s.findAllSolutions());
    }

    @Test
    public void test4() {
        Solver s = new Solver();
        BoolVar b = VF.one(s);
        IntVar x = VF.bounded("x", 0, 2, s);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(0, s.findAllSolutions());
    }

    @Test
    public void test5() {
        Solver s = new Solver();
        BoolVar b = VF.one(s);
        IntVar x = VF.bounded("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(1, s.findAllSolutions());
    }

    @Test
    public void test6() {
        Solver s = new Solver();
        BoolVar b = VF.zero(s);
        IntVar x = VF.bounded("x", 0, 3, s);
        int c = 2;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(4, s.findAllSolutions());
    }

    @Test
    public void test7() {
        Solver s = new Solver();
        BoolVar b = VF.zero(s);
        IntVar x = VF.bounded("x", 0, 2, s);
        int c = 3;
        s.post(new FastImpliesEq(b, x, c));
        Assert.assertEquals(3, s.findAllSolutions());
    }
}
