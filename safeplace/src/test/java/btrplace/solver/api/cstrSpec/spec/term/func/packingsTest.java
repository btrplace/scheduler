package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class PackingsTest {

    @Test
    public void test() {
        Set<Integer> s = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            s.add(i);
        }
        //System.out.println(AllTuplesGenerator.allSubsets(Integer.class, s));
        List args = Arrays.asList(s);
        Packings p = new Packings();
        Set<Set<Set<Object>>> res = p.eval(new SpecModel(), args);
        //System.out.println(res);
        for (Set<Set<Object>> x : res) {
            System.out.println(x);
        }
        Assert.assertEquals(res.size(), 14);
        /*
         [[0]]
         [[1]]
         [[2]]
         [[0,1]]
         [[0,2]]
         [[1,2]]
         [[1,2,3]]
         [[0,1],[2]]
         [[0,2],[1]]
         [[1,2],[0]]
         [[0],[1]]
         [[0],[2]]
         [[1],[2]]
         [[1],[2],[3]]

        0
        0 1
        0 1 2
        0,1
        0,1 2
        0,2
        0,2 1
        0,1,2
        1
        1 0
        1 0 2
        1 2
        1 2 0
        1,2
        1,2 0
        1,2,0
        2
        2 0
         */
    }
}
