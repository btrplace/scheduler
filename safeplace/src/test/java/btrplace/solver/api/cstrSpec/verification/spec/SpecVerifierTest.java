package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifierTest {

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
        mo.getMapping().addOfflineNode(n1);
        mo.getMapping().addRunningVM(vm0, n0);
        mo.getMapping().addSleepingVM(vm1, n0);

        Specification spec = getSpecification();
        Constraint c = spec.get("among");
        SpecVerifier v = new SpecVerifier();
        CheckerResult res = v.verify(c, Arrays.asList(
                new Constant(Arrays.asList(vm0, vm1), new SetType(VMType.getInstance())),
                new Constant(Arrays.asList(Arrays.asList(n1)), new SetType(new SetType(NodeType.getInstance())))), mo, mo);
        Assert.assertFalse(res.getStatus());
    }

    @Test
    public void testSplit() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOfflineNode(n1);
        mo.getMapping().addRunningVM(vm0, n0);
        mo.getMapping().addSleepingVM(vm1, n0);

        Specification spec = getSpecification();
        Constraint c = spec.get("split");
        SpecVerifier v = new SpecVerifier();
        CheckerResult res = v.verify(c, Arrays.asList(
                new Constant(Arrays.asList(Arrays.asList(vm1)), new SetType(new SetType(VMType.getInstance())))), mo, mo);
        Assert.assertFalse(res.getStatus());
    }

    @Test
    public void testSplitAmong() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOfflineNode(n1);
        mo.getMapping().addRunningVM(vm0, n0);
        mo.getMapping().addSleepingVM(vm1, n0);

        Specification spec = getSpecification();
        Constraint c = spec.get("splitAmong");
        SpecVerifier v = new SpecVerifier();
        //v.continuous(false);
        List<Constant> args = Arrays.asList(
                new Constant(Arrays.asList(Arrays.asList(vm0), Arrays.asList(vm1)), new SetType(new SetType(VMType.getInstance()))),
                new Constant(Arrays.asList(Arrays.asList(n1, n0)), new SetType(new SetType(NodeType.getInstance()))));
        CheckerResult res = v.verify(c, args, mo, mo);
        Assert.assertFalse(res.getStatus());
    }

    @Test
    public void testQuarantine() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOfflineNode(n1);
        mo.getMapping().addRunningVM(vm0, n0);
        mo.getMapping().addRunningVM(vm1, n0);

        Specification spec = getSpecification();
        Constraint c = spec.get("quarantine");
        SpecVerifier v = new SpecVerifier();
        /*TestCase tc = new TestCase(Arrays.asList(new SpecVerifier()),
                                  c, new DefaultReconfigurationPlan(mo),
                                   Arrays.asList(new Constant(Collections.singleton(vm1),
                                  new SetType(VMType.getInstance()))), false);*/
        CheckerResult res = v.verify(c, Arrays.asList(new Constant(n0,
                NodeType.getInstance())), new DefaultReconfigurationPlan(mo));
        Assert.assertFalse(res.getStatus());
    }

    @Test
    public void testRunningCapacity() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        mo.getMapping().addOnlineNode(n0);
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addRunningVM(vm0, n0);
        mo.getMapping().addRunningVM(vm1, n1);

        ImplVerifier v = new ImplVerifier();
        Specification spec = getSpecification();
        Constraint c = spec.get("runningCapacity");
        List<Constant> args = Arrays.asList(
                new Constant(new HashSet<>(Arrays.asList(n0, n1)), new SetType(NodeType.getInstance())),
                new Constant(1, IntType.getInstance())
        );
        Assert.assertFalse(v.verify(c, args, mo, mo).getStatus());
        //System.out.println(tc.pretty(true));
    }
}
