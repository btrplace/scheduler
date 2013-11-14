package btrplace.solver.api.cstrSpec.generator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class RandomTuplesGeneratorTest {

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

        RandomTuplesGenerator<Integer> tg = new RandomTuplesGenerator<>(Integer.class, l);
        Set<Integer[]> s = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Assert.assertTrue(s.add(tg.next()));
            //System.out.println(Arrays.toString(tg.next()));
        }
    }
}
