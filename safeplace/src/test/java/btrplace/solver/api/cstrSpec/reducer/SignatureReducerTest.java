package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownVM;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.*;
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

    Node n0;
    Node n1;
    Node n2;

    VM vm0;
    VM vm1;
    VM vm2;
    VM vm3;
    VM vm4;

    private ReconfigurationPlan makePlan() {

        Model mo = new DefaultModel();
        n0 = mo.newNode();
        n1 = mo.newNode();
        n2 = mo.newNode();

        vm0 = mo.newVM();
        vm1 = mo.newVM();
        vm2 = mo.newVM();
        vm3 = mo.newVM();
        vm4 = mo.newVM();

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
                if (x.id().equals("preserve")) {
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
        List<Constant> args = new ArrayList<>();
        /*args.add(5);
        args.add(Arrays.asList(1, 2, 3, 4));
        args.add("foo");
        args.add(Arrays.asList("a","b","c","d","e"));*/
        args.add(new Constant(p.getOrigin().getMapping().getAllVMs(), new SetType(VMType.getInstance())));
        args.add(StringType.getInstance().newValue("cpu"));
        args.add(IntType.getInstance().newValue(5));
        args.add(BoolType.getInstance().newValue(true));
        //args.add(Arrays.asList(Arrays.asList(n1, n2), Arrays.asList(n0)));
        //args.add(Boolean.TRUE);
        red.reduce(tc, c, args);
        Assert.fail();
    }
}
