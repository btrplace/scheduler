package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileReader;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanFuzzerTest {

    @Test
    public void test() throws Exception {
        TransitionTable nodeTrans = new TransitionTable(new FileReader("node_transitions"));
        TransitionTable vmTrans = new TransitionTable(new FileReader("vm_transitions"));
        ReconfigurationPlanFuzzer f = new ReconfigurationPlanFuzzer(nodeTrans, vmTrans, 1, 1);
        for (int i = 0; i < 100; i++) {
            ReconfigurationPlan p = f.newPlan();
            Assert.assertEquals(p.getOrigin().getMapping().getNbVMs(), 1);
            Assert.assertEquals(p.getOrigin().getMapping().getNbNodes(), 1);

            System.out.println("Original plan:\n" + p);
            System.out.println("With delay:");
            DelaysGenerator dg = new DelaysGenerator(p, true);
            for (int j = 0; j < 10; j++) {
                System.out.println(dg.next());
            }
        }
    }
}
