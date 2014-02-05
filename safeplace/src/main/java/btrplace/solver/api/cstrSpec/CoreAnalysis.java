package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.Fuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.FuzzerListener;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestCaseConverter;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.CheckerVerifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CoreAnalysis {

    public static void main(String[] args) throws Exception {
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File(args[0]));

        File fImpl = new File(args[1]);
        if (!fImpl.getParentFile().isDirectory() && !fImpl.getParentFile().mkdirs()) {
            System.err.println("Unable to create '" + fImpl.getParent() + "'");
            System.exit(1);
        }

        File fChecker = new File(args[2]);
        if (!fChecker.getParentFile().isDirectory() && !fChecker.getParentFile().mkdirs()) {
            System.err.println("Unable to create '" + fChecker.getParent() + "'");
            System.exit(1);
        }

        List<Constraint> cores = new ArrayList<>();
        final TestCaseConverter tcc = new TestCaseConverter();
        for (Constraint c : spec.getConstraints()) {
            if (c.isCore()) {
                cores.add(c);
            }
        }
        System.out.println("Verifying implementation:");
        List<TestCase> implFailures = check(cores, Arrays.asList(new SpecVerifier(), new ImplVerifier()));
        try (FileWriter implF = new FileWriter(fImpl)) {
            tcc.toJSON(implFailures, implF);
        }
        System.out.println("Verifying checkers:");
        List<TestCase> checkFailures = check(cores, Arrays.asList(new SpecVerifier(), new CheckerVerifier()));
        try (FileWriter checkF = new FileWriter(fChecker)) {
            tcc.toJSON(checkFailures, checkF);
        }
    }

    private static List<TestCase> check(final List<Constraint> cores, final List<Verifier> verifiers) throws Exception {
        final List<TestCase> issues = new ArrayList<>();
        Fuzzer fuzzer = new Fuzzer(1, 1).minDuration(1).maxDuration(3).allDurations().allDelays();
        fuzzer.addListener(new FuzzerListener() {
            private int d = 0;

            @Override
            public void recv(ReconfigurationPlan p) {
                for (Constraint c : cores) {
                    TestCase tc3 = new TestCase(verifiers, c, p, Collections.<Constant>emptyList(), false);
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
        return issues;
    }
}
