package btrplace.solver.api.cstrSpec.generator;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        RandomTuplesGenerator<Integer> tg = new RandomTuplesGenerator<Integer>(Integer.class, l);
        for (int i = 0; i < 100; i++) {
            System.out.println(Arrays.toString(tg.next()));
        }
    }
}
