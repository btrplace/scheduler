package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.Fuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.FuzzerListener;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerification implements FuzzerListener {

    private Fuzzer fuzzer;

    private ExecutorService executor;

    private Constraint cstr;

    private List<Verifier> verifiers;

    private List<Constant> args;

    private boolean continuous;

    private CompletionService<TestCase> completionService;

    private int nbPlans;

    public ParallelConstraintVerification(Fuzzer f, List<Verifier> verifiers, int nbWorkers, Constraint cstr, List<Constant> args, boolean conti) {
        this.fuzzer = f;
        executor = Executors.newFixedThreadPool(nbWorkers);
        completionService = new ExecutorCompletionService<>(executor);
        this.verifiers = verifiers;
        this.cstr = cstr;
        this.args = args;
        this.continuous = conti;
    }

    public List<TestCase> verify() {
        fuzzer.addListener(this);
        fuzzer.go();
        System.out.println(nbPlans + " produced");
        List<TestCase> issues = new ArrayList<>();
        for (int i = 0; i < nbPlans; i++) {
            try {
                Future<TestCase> f = completionService.take();
                TestCase tc = f.get();
                if (tc != null) {
                    System.out.print("-");
                    issues.add(tc);
                } else {
                    System.out.print("+");
                }
                if ((i + 1) % 80 == 0) {
                    System.out.println();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        System.out.println();
        return issues;
    }


    @Override
    public void recv(final ReconfigurationPlan p) {
        completionService.submit(new Consumer(verifiers, p, cstr, args, continuous));
        nbPlans++;
    }

    class Consumer implements Callable<TestCase> {

        private TestCase tc;

        public Consumer(List<Verifier> verifiers, ReconfigurationPlan p, Constraint c, List<Constant> args, boolean continuous) {
            tc = new TestCase(verifiers, c, p, args, continuous);
        }

        @Override
        public TestCase call() {
            if (!tc.succeed()) {
                return tc;
            }
            return null;
        }
    }
}
