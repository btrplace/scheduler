package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class FuzzerTest {

    @Test
    public void testGo() throws Exception {
        final Set<ReconfigurationPlan> plans = new HashSet<>();
        Fuzzer fuzzer = new Fuzzer(1, 1).minDuration(1)
                .maxDuration(3)
                .allDurations()
                .allDelays();/*.nbDurations(3).nbDelays(3);*/
        fuzzer.addListener(new FuzzerListener() {
            int d = 0;

            @Override
            public void recv(ReconfigurationPlan p) {
                ++d;
                //System.out.println("->" + d);
                System.out.println(p);
                System.out.flush();
                Assert.assertTrue(plans.add(p), "only " + d + " generated");

            }
        });
        fuzzer.go();
    }
}
