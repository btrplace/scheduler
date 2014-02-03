package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.fuzzer.DelaysGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.DurationsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlansGenerator;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifierTest {

    public Specification getSpec() throws Exception {
        SpecReader r = new SpecReader();
        return r.getSpecification(new File("src/test/resources/v1.cspec"));
    }

    @Test
    public void dummy4Cores() throws Exception {

        Specification s = getSpec();
        List<Constraint> cores = new ArrayList<>();
        for (Constraint c : s.getConstraints()) {
            if (c.isCore()) {
                cores.add(c);
            }
        }
        check(cores);
    }

    private void check(List<Constraint> cores) {
        ModelsGenerator mg = new ModelsGenerator(1, 1);
        int nb = 0;
        for (Model mo : mg) {
            ReconfigurationPlansGenerator rpgen = new ReconfigurationPlansGenerator(mo);
            for (ReconfigurationPlan p : rpgen) {
                //3 durations, 3 delays
                DurationsGenerator dg = new DurationsGenerator(p, 1, 3);
                for (int i = 0; i < 3; i++) {
                    ReconfigurationPlan pd = dg.next();
                    DelaysGenerator delayG = new DelaysGenerator(pd, true);
                    for (int j = 0; j < 3; j++) {
                        ReconfigurationPlan cp = delayG.next();
                        System.out.println(cp.getOrigin().getMapping());
                        System.out.println(cp);
                        for (Constraint c : cores) {
                            CheckerResult res = new SpecVerifier().verify(c, p, Collections.<Constant>emptyList(), false);
                            if (res.getStatus() == null) {
                                System.err.println(p.getOrigin().getMapping());
                                System.err.println(p);
                                System.err.println(c.toString() + " " + res);
                                Assert.fail();
                            }
                            System.out.println(c.toString() + " " + res);
                            nb++;
                        }
                        System.out.println("\n");
                    }
                }

            }
        }
        System.out.println(nb + " verification(s)");
    }

    @Test
    public void testDumb() throws Exception {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        VM v = mo.newVM();
        mo.getMapping().addOnlineNode(n);
        mo.getMapping().addRunningVM(v, n);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new ShutdownNode(n, 1, 3));
        p.add(new BootVM(v, n, 0, 1));

        Specification s = getSpec();
        Constraint c = null;
        for (Constraint c2 : s.getConstraints()) {
            if (c2.id().equals("noHostForReadyVMs")) {
                c = c2;
                break;
            }
        }
        System.out.println(mo.getMapping());
        System.out.println(p);
        SpecVerifier verif = new SpecVerifier();
        System.out.println(c.getProposition());
        System.out.println(verif.verify(c, p, Collections.<Constant>emptyList(), false));
        Assert.fail();
    }
}
