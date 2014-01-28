package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownVM;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
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
    Node n3;
    Node n4;

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
        n3 = mo.newNode();
        n4 = mo.newNode();

        vm0 = mo.newVM();
        vm1 = mo.newVM();
        vm2 = mo.newVM();
        vm3 = mo.newVM();
        vm4 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addOnlineNode(n4);

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
                if (x.id().equals("offline")) {
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
    public void test() throws Exception {
        ReconfigurationPlan p = makePlan();
        Constraint c = makeConstraint();
        TestCase tc = new TestCase(0, p, null, false);
        System.out.println(p.getOrigin().getMapping() + "\n" + p);
        System.out.println(c.pretty());
        SignatureReducer red = new SignatureReducer();
        List<Constant> args = new ArrayList<>();

        args.add(new Constant(p.getOrigin().getMapping().getAllNodes(), new SetType(NodeType.getInstance())));

        /*Set<Set<Node>> ps = new HashSet<>();
        ps.add(new HashSet<>(Arrays.asList(n0, n1)));
        ps.add(new HashSet<>(Arrays.asList(n2, n3, n4)));
        args.add(new Constant(ps, new SetType(new SetType(NodeType.getInstance()))));
        args.add(BoolType.getInstance().newValue(true));                             */
        red.reduce(p, c, args);
        System.out.println(args);
        Assert.fail();
    }
}
