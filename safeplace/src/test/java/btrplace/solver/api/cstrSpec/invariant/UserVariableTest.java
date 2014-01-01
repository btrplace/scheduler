package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class UserVariableTest {

    @Test
    public void test() {
        Primitive p1 = new Primitive("VM", new SetType(VMType.getInstance()));
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        mo.getMapping().addReadyVM(v);
        mo.getMapping().addReadyVM(mo.newVM());

        Var vs = p1.newInclusive("vs", true);
        System.out.println(vs.pretty());

        Assert.assertFalse(vs.set(mo.newVM()));
        p1.set(mo.getMapping().getAllVMs());
        Assert.assertTrue(vs.set(v));
    }
}
