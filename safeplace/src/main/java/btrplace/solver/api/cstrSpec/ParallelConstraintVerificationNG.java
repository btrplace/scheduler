package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlansGenerator;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerificationNG {

    private Constraint cstr;

    private Verifier v;

    private boolean continuous;

    private CompletionService completionService;

    private ModelsGenerator modelsGen;

    private Queue<TestCase> defiant;

    private final Object lock;

    private int nb;

    public ParallelConstraintVerificationNG(ModelsGenerator mg, Verifier v, int nbWorkers, Constraint cstr, boolean cont) {
        completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(nbWorkers));
        defiant = new LinkedBlockingDeque<>();
        this.v = v;
        this.cstr = cstr;
        this.continuous = cont;
        modelsGen = mg;
        lock = new Object();
    }

    public int verify() {
        int nbModels = 0;
        for (Model mo : modelsGen) {
            completionService.submit(new Consumer(mo, cstr, continuous));
            nbModels++;
        }
        System.out.println(nbModels + " produced");
        for (int i = 0; i < nbModels; i++) {
            try {
                completionService.take();
                System.out.print(".");
                if ((i + 1) % 80 == 0) {
                    System.out.println();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        System.out.println();
        return nb;
    }

    public Queue<TestCase> getDefiant() {
        return defiant;
    }

    public void compliant(TestCase tc) {
        synchronized (lock) {
            nb++;
        }
    }

    public void defiant(TestCase tc) {
        //System.err.println("-");
        defiant.add(tc);
        synchronized (lock) {
            nb++;
        }

    }

    class Consumer implements Callable {

        private Model mo;

        private boolean continuous;

        private Constraint c;

        public Consumer(Model mo, Constraint c, boolean cont) {
            this.mo = mo;
            this.c = c;
            this.continuous = cont;
        }

        @Override
        public Object call() {
            ReconfigurationPlansGenerator grn = new ReconfigurationPlansGenerator(mo);
            SpecModel sp = new SpecModel(mo);
            for (ReconfigurationPlan rp : grn) {
                //Every input
                ConstraintInputGenerator cig = new ConstraintInputGenerator(c, sp, true);
                for (List<Constant> args : cig) {
                    TestCase tc = new TestCase(v, cstr, rp, args, !continuous);
                    try {
                        if (tc.succeed()) {
                            compliant(tc);
                        } else {
                            defiant(tc);
                        }
                    } catch (Exception e) {
                        //defiant(tc);
                    }
                }
            }
            return null;
        }
    }
}
