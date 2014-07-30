package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.KillVM;
import btrplace.plan.event.SuspendVM;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ImplVerifierTest {

    private static SpecReader ex = new SpecReader();

    private static Specification getSpecification() throws Exception {
        return ex.getSpecification(new File("src/main/cspec/v1.cspec"));
    }

    @Test
    public void testAmong() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addReadyVM(vm0);
        mo.getMapping().addReadyVM(vm1);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new BootVM(vm0, n1, 0, 1));
        p.add(new BootVM(vm1, n1, 0, 1));
        /*Model dst = mo.clone();
        dst.getMapping().addRunningVM(vm0, n1);
        dst.getMapping().addRunningVM(vm1, n1);*/
        Specification spec = getSpecification();
        Constraint c = spec.get("among");
        ImplVerifier iv = new ImplVerifier();
        //iv.continuous(false);
        CTestCase tc = new CTestCase("", c, Arrays.asList(
                new Constant(Collections.singletonList(vm1), new SetType(VMType.getInstance())),
                new Constant(Collections.singleton(new HashSet<>(Arrays.asList(n0))), new SetType(new SetType(NodeType.getInstance())))
        ), p, false);
        CheckerResult res = iv.verify(tc.getConstraint(), tc.getParameters(), p.getOrigin(), p.getResult());
        Assert.assertEquals(res.getStatus(), Boolean.TRUE);

    }

    @Test
    public void testLonely() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM v0 = mo.newVM();
        VM v1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addRunningVM(v0, n1);
        mo.getMapping().addReadyVM(v1);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new SuspendVM(v0, n1, n1, 0, 3));
        p.add(new BootVM(v1, n1, 0, 3));
        ImplVerifier v = new ImplVerifier();
        //v.continuous(true);
        Constraint c = getSpecification().get("lonely");
        List<Constant> args = Arrays.asList(new Constant(new HashSet(Arrays.asList(v0, v1)), new SetType(VMType.getInstance())));
        System.out.println(v.verify(c, args, p.getOrigin(), p.getResult()));
        //TestCase tc = new TestCase(v, c, p, args, true);
        //System.out.println(tc.pretty(true));
        Assert.fail();
    }

    @Test
    public void testKill() throws Exception {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Node n = mo.newNode();
        mo.getMapping().addOnlineNode(n);
        mo.getMapping().addRunningVM(v, n);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new KillVM(v, n, 1, 4));
        ImplVerifier i = new ImplVerifier();
        Constraint c = getSpecification().get("noVMsOnOfflineNodes");
        CheckerResult res = i.verify(c, Collections.<Constant>emptyList(), p);
        Assert.assertTrue(res.getStatus());
    }
}
