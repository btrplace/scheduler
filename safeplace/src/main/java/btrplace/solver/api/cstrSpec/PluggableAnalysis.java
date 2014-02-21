package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.Fuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.FuzzerListener;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class PluggableAnalysis {

    public static void main(String[] args) throws Exception {

        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File("src/test/resources/v1.cspec"));

        Constraint cstr = spec.get("spread");
        List<TestCase> issues = check(cstr, Arrays.asList(new SpecVerifier(), new ImplVerifier()));
        for (TestCase tc : issues) {
            System.out.println(tc.pretty());
            System.out.println(tc.getPlan().getOrigin().getMapping());
            System.out.println(tc.getPlan());
        }

    }

    private static List<TestCase> check(final Constraint cstr, final List<Verifier> verifiers) throws Exception {
        final List<TestCase> issues = new ArrayList<>();
        Fuzzer fuzzer = new Fuzzer(2, 2).minDuration(1).maxDuration(3).allDurations().allDelays();
        fuzzer.addListener(new FuzzerListener() {
            private int d = 0;

            @Override
            public void recv(ReconfigurationPlan p) {
                SpecModel mo = new SpecModel(p.getOrigin());
                ConstraintInputGenerator ig = new ConstraintInputGenerator(cstr, mo, true);
                for (List<Constant> args : ig) {
                    //System.out.println(args);
                    TestCase tc3 = new TestCase(verifiers, cstr, p, args, false);
                    if (!tc3.succeed()) {
                        System.out.print("-");
                        issues.add(tc3);

                        //System.out.println(tc3.pretty());
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
