package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class ResultingModelsGeneratorTest {

    @Test
    public void test() {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addRunningVM(vm1, n1);
        mo.getMapping().addSleepingVM(vm0, n1);
        ResultingModelsGenerator rmg = new ResultingModelsGenerator(mo);
        for (Model x : rmg) {
            System.err.println(x.getMapping());
        }
    }
}
