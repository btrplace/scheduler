package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ElementsReducerTest {

    public Constraint makeConstraint(String id) {

        SpecReader ex = new SpecReader();
        try {
            for (Constraint x : ex.getSpecification(new File("src/main/cspec/v1.cspec")).getConstraints()) {
                if (x.id().equals(id)) {
                    return x;
                }
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        return null;
    }

    @Test
    public void test() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOfflineNode(n1);
        m.addOnlineNode(n2);
        m.addReadyVM(vm0);
        m.addRunningVM(vm1, n2);
        m.addRunningVM(vm2, n2);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new SuspendVM(vm1, n2, n2, 4, 7));
        p.add(new BootNode(n1, 5, 8));
        p.add(new SuspendVM(vm2, n2, n2, 6, 9));
        p.add(new ShutdownNode(n0, 8, 11));

        Constraint cstr = makeConstraint("lonely");

        List<Constant> in = new ArrayList<>();
        in.add(new Constant(mo.getMapping().getAllVMs(), new SetType(VMType.getInstance())));

        CTestCase tc = new CTestCase("foo", cstr, in, p, true);

        ElementsReducer er = new ElementsReducer();
        SpecVerifier v1 = new SpecVerifier();
        Verifier v2 = new ImplVerifier();
        Assert.fail();
        CTestCase r = er.reduce(tc, v1, v2, null);
        System.out.println(tc);
        System.out.println(r);
        Assert.assertEquals(r.getPlan().getOrigin().getMapping().getNbNodes(), 1);
    }
}
