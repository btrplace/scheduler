package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
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
        mo.getMapping().addRunningVM(vm0, n0);
        mo.getMapping().addReadyVM(vm1);

        Specification spec = getSpecification();
        Constraint c = spec.get("among");
        ImplVerifier v = new ImplVerifier(false);
        //System.out.println(mo.getMapping());
        CheckerResult res = v.verify(c, new DefaultReconfigurationPlan(mo),
                Arrays.asList(
                        new Constant(Arrays.asList(vm0), new SetType(VMType.getInstance())),
                        new Constant(Arrays.asList(Arrays.asList(n0, n1)), new SetType(new SetType(NodeType.getInstance())))
                ),
                true);
        Assert.assertFalse(res.getStatus());
    }
}
