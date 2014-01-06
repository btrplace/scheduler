package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.DefaultModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class PTest {

    @Test
    public void test() {
        Set<Integer> s = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            s.add(i);
        }
        List args = Arrays.asList(s);
        P p = new P();
        Set res = p.eval(new DefaultModel(), args);
        System.out.println(res);
        Assert.assertEquals(res.size(), 12);
    }
}
