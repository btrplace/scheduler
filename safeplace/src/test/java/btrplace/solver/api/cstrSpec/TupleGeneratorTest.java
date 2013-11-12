package btrplace.solver.api.cstrSpec;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TupleGeneratorTest {

    @Test
    public void test() {
        List<List<Integer>> l = new ArrayList<>();
        List<Integer> cnt = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            cnt.add(i);
        }
        l.add(cnt);
        l.add(cnt);
        l.add(cnt);
        TupleGenerator<Integer> tg = new TupleGenerator<>(l);
        int nb = 0;
        while(tg.hasNext()) {
            List<Integer>t = tg.next();
            //System.out.println(t);
            nb++;
        }
        Assert.assertEquals(nb, Math.pow(cnt.size(), l.size()));
    }
    @Test
    public void test2() {
        List<List<Integer>> l = new ArrayList<>();
        List<Integer> cnt = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            cnt.add(i);
        }
        l.add(cnt);
        l.add(cnt);
        l.add(cnt);
        Assert.assertEquals(Collections.allTuples(l).size(), Math.pow(cnt.size(), l.size()));
    }
}
