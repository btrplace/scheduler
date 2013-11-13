package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.generator.TupleGenerator;
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
        for (int i = 0; i < 300; i++) {
            cnt.add(i);
        }
        l.add(cnt);
        l.add(cnt);
        l.add(cnt);
        int nb = 0;
        TupleGenerator<Integer> tg = new TupleGenerator<>(Integer.class, l);
        for(Integer [] t : tg) {
            //System.out.println(t);
            nb++;
        }
        Assert.assertEquals(nb, Math.pow(cnt.size(), l.size()));
    }
}
