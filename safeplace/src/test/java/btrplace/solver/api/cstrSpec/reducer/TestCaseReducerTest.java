package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.model.constraint.Offline;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCaseReducerTest {

    @Test
    public void test() {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOnlineNode(n1);
        m.addOfflineNode(n2);
        m.addRunningVM(vm0, n0);
        m.addReadyVM(vm1);
        m.addSleepingVM(vm2, n1);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new ShutdownNode(n1, 0, 3));
        p.add(new BootVM(vm1, n0, 2, 7));
        p.add(new BootNode(n2, 0, 3));
        p.add(new MigrateVM(vm0, n0, n2, 4, 10));

        System.out.println("-- Plan --\n" + p.getOrigin().getMapping() + "\n" + p);
        SpecReader ex = new SpecReader();
        Constraint cstr = null;

        try {
            for (Constraint x : ex.extractConstraints(new File("src/test/resources/v1.cspec"))) {
                if (x.id().equals("offline")) {
                    cstr = x;
                    break;
                }
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        ImplVerifier verif = new ImplVerifier();
        //System.out.println(p.getOrigin().getMapping());
        List<Object> in = new ArrayList<>();
        in.add(Collections.singletonList(n1));
        TestCase tc = new TestCase(0, p, new Offline(Collections.singleton(n1)), cstr.eval(p.getOrigin(), in));

        System.out.println(cstr.getProposition());
        TestResult tr = verif.verify(tc);
        System.out.println(tr);
        //TestCaseReducer r = new TestCaseReducer();
        //r.reducePlan(0, tc, cstr, in);

        TestCaseReducer tcr = new TestCaseReducer();
        List<TestCase> reduced = tcr.reducePlan(tc, cstr, in);

        System.out.println("------------\nReduced Test Cases\n-----------");
        for (TestCase t : reduced) {
            System.out.println(t);
            System.out.println(verif.verify(t));
            System.out.println("---");
        }
        Assert.fail();
    }
}
