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
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        System.out.println("Checker verification: ");
        check(cores, Arrays.asList(new SpecVerifier(), new CheckerVerifier()));

        System.out.println("Implementation verification: ");
        check(cores, Arrays.asList(new SpecVerifier(), new ImplVerifier()));
    }

    private void check(final List<Constraint> cores, final List<Verifier> verifs) throws IOException {
        final List<TestCase> issues = new ArrayList<>();
        final TestCaseConverter tcc = new TestCaseConverter();
        Fuzzer fuzzer = new Fuzzer(1, 1).minDuration(1).maxDuration(3).allDurations().allDelays();/*.nbDurations(3).nbDelays(3);*/
        fuzzer.addListener(new FuzzerListener() {
            private int d = 0;

            @Override
            public void recv(ReconfigurationPlan p) {
                for (Constraint c : cores) {
                    TestCase tc3 = new TestCase(verifs, c, p, Collections.<Constant>emptyList(), false);
                    if (!tc3.succeed()) {
                        System.out.print("-");
                        issues.add(tc3);
                    } else {
                        System.out.print("+");
                    }
                    if (d++ == 80) {
                        d = 0;
                        System.out.println();
                    }
                }
            }
        });
        fuzzer.go();
        System.out.println();
        for (TestCase c : issues) {
            System.out.println(c.pretty());
        }
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
