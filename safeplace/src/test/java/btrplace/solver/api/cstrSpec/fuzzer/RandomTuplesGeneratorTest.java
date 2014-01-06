package btrplace.solver.api.cstrSpec.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class RandomTuplesGeneratorTest {

    @Test
    public void test() {
        List<List<Integer>> l = new ArrayList<>();
        List<Integer> cnt = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            cnt.add(i);
        }
        l.add(cnt);
        l.add(cnt);
        l.add(cnt);

        RandomTuplesGenerator<Integer> tg = new RandomTuplesGenerator<>(Integer.class, l);
        Set<Integer[]> s = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            System.out.println(Arrays.toString(tg.next()));
            //Assert.assertTrue(s.add(tg.next()));
            //System.out.println(Arrays.toString(tg.next()));
        }
        Assert.fail();
    }
}
