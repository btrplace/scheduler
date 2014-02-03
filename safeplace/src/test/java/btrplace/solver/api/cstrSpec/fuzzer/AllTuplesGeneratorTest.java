package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class AllTuplesGeneratorTest {

    @Test
    public void test() {
        List<List<Integer>> l = new ArrayList<>();
        List<Integer> cnt = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            cnt.add(i);
        }
        l.add(cnt);
        l.add(cnt);
        l.add(cnt);
        double nb = 0;
        AllTuplesGenerator<Integer> tg = new AllTuplesGenerator<>(Integer.class, l);
        for (Integer[] t : tg) {
            //System.out.println(t);
            nb++;
        }
        Assert.assertEquals(nb, Math.pow(cnt.size(), l.size()));
    }
}
