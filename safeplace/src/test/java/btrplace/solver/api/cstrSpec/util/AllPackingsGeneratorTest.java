package btrplace.solver.api.cstrSpec.util;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllPackingsGeneratorTest {

    @Test
    public void test() {
        AllPackingsGenerator<Character> pg = new AllPackingsGenerator<>(Character.class, Arrays.asList(new Character[]{'a', 'b', 'c'}));
        Set<Set<Set<Character>>> packings = new HashSet<>();
        while (pg.hasNext()) {
            packings.add(pg.next());
        }
        for (Set<Set<Character>> s : packings) {
            System.out.println(s);
        }
        Assert.assertEquals(packings.size(), 15);
    }
}
