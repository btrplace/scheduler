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
        List args = Arrays.asList(s);
        Packings p = new Packings();
        Set res = p.eval(new SpecModel(), args);
        System.out.println(res);
        Assert.assertEquals(res.size(), 12);
    }
}
