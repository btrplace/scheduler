package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownVM;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SignatureReducerTest {

    private ReconfigurationPlan makePlan() {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);

        m.addRunningVM(vm0, n1);
        m.addRunningVM(vm1, n2);
        m.addRunningVM(vm2, n2);
        m.addSleepingVM(vm3, n2);
        m.addReadyVM(vm4);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new ShutdownVM(vm0, n1, 0, 3));
        p.add(new SuspendVM(vm1, n2, n2, 0, 3));
        return p;
    }

    public Constraint makeConstraint() {
        Constraint cstr = null;

        SpecReader ex = new SpecReader();
        try {
            for (btrplace.solver.api.cstrSpec.Constraint x : ex.extractConstraints(new File("src/test/resources/v1.cspec"))) {
                if (x.id().equals("spread")) {
                    cstr = x;
                    return cstr;
                }
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        return null;
    }

    @Test
    public void test() {
        ReconfigurationPlan p = makePlan();
        Constraint c = makeConstraint();
        TestCase tc = new TestCase(0, p, null, false);
        System.out.println(p.getOrigin().getMapping() + "\n" + p);
        System.out.println(c.pretty());
        SignatureReducer red = new SignatureReducer();
        List<Object> args = new ArrayList<>();
        /*args.add(5);
        args.add(Arrays.asList(1, 2, 3, 4));
        args.add("foo");
        args.add(Arrays.asList("a","b","c","d","e"));*/
        args.add(p.getOrigin().getMapping().getAllVMs());

        red.reduce(tc, c, args);
        Assert.fail();
    }
}
