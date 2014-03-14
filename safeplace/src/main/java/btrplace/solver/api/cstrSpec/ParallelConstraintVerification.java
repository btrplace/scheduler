package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzerListener;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerification implements ReconfigurationPlanFuzzerListener {

    private ReconfigurationPlanFuzzer fuzzer;

    private ExecutorService executor;

    private Constraint cstr;

    private Verifier v;

    private boolean continuous;

    private CompletionService<TestCase> completionService;

    private int nbPlans;

    public ParallelConstraintVerification(ReconfigurationPlanFuzzer f, Verifier v, int nbWorkers, Constraint cstr, boolean conti) {
        this.fuzzer = f;
        executor = Executors.newFixedThreadPool(nbWorkers);
        completionService = new ExecutorCompletionService<>(executor);
        this.v = v;
        this.cstr = cstr;
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
        if (cstr.isCore()) {
            completionService.submit(new Consumer(v, p, cstr, Collections.<Constant>emptyList(), !continuous));
            nbPlans++;
        } else {
            ConstraintInputGenerator tig = new ConstraintInputGenerator(cstr, new SpecModel(p.getOrigin()), true);
            //System.out.println(tig.count() + " inputs for plan \n" + p.getOrigin().getMapping());
            for (List<Constant> params : tig) {
                completionService.submit(new Consumer(v, p, cstr, params, !continuous));
                nbPlans++;
            }
        }
    }

    class Consumer implements Callable<TestCase> {

        private TestCase tc;

        public Consumer(Verifier v, ReconfigurationPlan p, Constraint c, List<Constant> args, boolean continuous) {
            tc = new TestCase(v, c, p, args, continuous);
        }

        @Override
        public TestCase call() {
            //System.err.println("Start verification");
            boolean b = tc.succeed();
            /*try {
                double l = 1000L *  Math.random();
                //System.err.println("wait for " + (long)l);
                Thread.sleep((long)l);
            } catch (Exception e) {
                e.printStackTrace();
            } */
            //System.err.println("Stop verification");
            return b ? null : tc;
        }
    }
}
