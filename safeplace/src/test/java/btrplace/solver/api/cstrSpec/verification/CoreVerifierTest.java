package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.fuzzer.Fuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.FuzzerListener;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.btrplace.CheckerVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CoreVerifierTest {

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
        //check(Collections.singletonList(s.get("noVMsOnOfflineNodes")));
    }

    private void check(final List<Constraint> cores) {
        final List<Verifier> vf = new ArrayList<>();
        vf.add(new SpecVerifier());
        vf.add(new CheckerVerifier());
        final List<TestCase3> issues = new ArrayList<>();
        final List<TestCase3> good = new ArrayList<>();
        Fuzzer fuzzer = new Fuzzer(1, 1).minDuration(1).maxDuration(3).allDurations().allDelays();/*.nbDurations(3).nbDelays(3);*/
        fuzzer.addListener(new FuzzerListener() {
            int d = 0;

            @Override
            public void recv(ReconfigurationPlan p) {
                //System.out.println((++d) + ".");
                //System.out.println(p.getOrigin().getMapping());
                //System.out.println(p);
                for (Constraint c : cores) {
                    TestCase3 tc3 = new TestCase3(vf, c, p, Collections.<Constant>emptyList(), false);
                    if (!tc3.succeed()) {
                        System.out.println(tc3.getPlan().getOrigin().getMapping());
                        System.out.println(tc3.getPlan());
                        System.out.println(tc3.pretty());
                        issues.add(tc3);
                    } else {
                        good.add(tc3);
                    }
                }
            }
        });
        fuzzer.go();
        System.err.println(issues.size() + "/" + (issues.size() + good.size()) + "  un-consistencies");
        System.out.flush();
        System.err.flush();
        Assert.fail();

    }

    @Test
    public void dummy4Cores2() throws Exception {

        Specification s = getSpec();
        List<Constraint> cores = new ArrayList<>();
        for (Constraint c : s.getConstraints()) {
            if (c.isCore()) {
                cores.add(c);
            }
        }
        checkcheck(cores);
    }

    private void checkcheck(final List<Constraint> cores) {
        Fuzzer fuzzer = new Fuzzer(1, 1).minDuration(1).maxDuration(3).allDurations().allDelays();/*.nbDurations(3).nbDelays(3);*/
        fuzzer.addListener(new FuzzerListener() {
            int d = 0;

            @Override
            public void recv(ReconfigurationPlan p) {
                System.out.println((++d) + ".");
                System.out.println(p.getOrigin().getMapping());
                System.out.println(p);
                for (Constraint c : cores) {
                    CheckerResult res = new CheckerVerifier().verify(c, p, Collections.<Constant>emptyList(), false);
                    if (res.getStatus() == null) {
                        System.err.println(p.getOrigin().getMapping());
                        System.err.println(p);
                        System.err.println(c.toString() + " " + res);
                        Assert.fail();
                    }
                    System.out.println(c.toString() + " " + res);
                }
                System.out.println("\n");
            }
        });
        fuzzer.go();
    }

    @Test
    public void testDumb() throws Exception {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        VM v = mo.newVM();
        mo.getMapping().addOfflineNode(n);
        mo.getMapping().addReadyVM(v);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new BootNode(n, 1, 4));
        p.add(new BootVM(v, n, 2, 3));

        Specification s = getSpec();
        Constraint c = s.get("noVMsOnOfflineNodes");
        System.out.println(mo.getMapping());
        System.out.println(p);
        SpecVerifier verif = new SpecVerifier();
        System.out.println(c.getProposition());
        System.out.println(verif.verify(c, p, Collections.<Constant>emptyList(), false));
        Assert.fail();
    }
}
