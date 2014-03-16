package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.DelaysGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlansGenerator;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerification {

    private Constraint cstr;

    private Verifier v;

    private boolean continuous;

    private CompletionService<List<TestCase>[]> completionService;

    private ModelsGenerator modelsGen;

    private ExecutorService exec;
    private List<TestCase> ok;
    private List<TestCase> ko;

    public ParallelConstraintVerification(ModelsGenerator mg, Verifier v, int nbWorkers, Constraint cstr, boolean cont) {
        exec = Executors.newFixedThreadPool(nbWorkers);
        completionService = new ExecutorCompletionService<>(exec);
        this.v = v;
        this.cstr = cstr;
        this.continuous = cont;
        modelsGen = mg;
        ok = new ArrayList<>();
        ko = new ArrayList<>();
    }

    public List<TestCase> getCompliant() {
        return ok;
    }

    public List<TestCase> getDefiant() {
        return ko;
    }

    public void verify() {
        int nbModels = 0;
        long st = System.currentTimeMillis();
        for (Model mo : modelsGen) {
            Consumer c = new Consumer(v, mo, cstr, continuous);
            completionService.submit(c);
            nbModels++;
        }
        long end = System.currentTimeMillis();
        exec.shutdown();
        System.out.println(nbModels + " produced in " + (end - st) + " ms");
        for (int i = 0; i < nbModels; i++) {
            try {
                Future<List<TestCase>[]> f = completionService.take();
                //System.out.print(".");
                List<TestCase>[] res = f.get();
                ok.addAll(res[0]);
                ko.addAll(res[1]);
                if ((i + 1) % 80 == 0) {
                    System.out.println();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class Consumer implements Callable<List<TestCase>[]> {

        private Model mo;

        private boolean continuous;

        private Constraint c;

        private Verifier ve;

        public Consumer(Verifier ve, Model mo, Constraint c, boolean cont) {
            this.mo = mo;
            this.c = c;
            this.ve = ve;
            this.continuous = cont;
        }

        @Override
        public List<TestCase>[] call() {
            System.err.println("start");
            ReconfigurationPlansGenerator grn = new ReconfigurationPlansGenerator(mo, 2);
            SpecModel sp = new SpecModel(mo);
            List<List<Constant>> allArgs = new ArrayList<>();
            ConstraintInputGenerator cig = new ConstraintInputGenerator(this.c, sp, true);
            for (List<Constant> args : cig) {
                allArgs.add(args);
            }
            List<TestCase>[] res = new List[2];
            res[0] = new ArrayList<>();
            res[1] = new ArrayList<>();
            long st = System.currentTimeMillis();
            for (ReconfigurationPlan skelPlan : grn) {
                //Every input
                List<ReconfigurationPlan> delayed = new ArrayList<>();
                if (continuous) {
                    for (ReconfigurationPlan drp : new DelaysGenerator(skelPlan)) {
                        delayed.add(drp);
                    }
                } else {
                    delayed.add(skelPlan);
                }
                for (ReconfigurationPlan p : delayed) {
                    for (List<Constant> args : allArgs) {
                        TestCase tc = new TestCase(ve, this.c, p, args, !continuous);
                        try {
                            if (tc.succeed()) {
                                res[0].add(tc);
                            } else {
                                res[1].add(tc);
                            }
                        } catch (Exception e) {
                            res[1].add(tc);
                        }
                        /*for (int i = 0; i < 1000000; i++) {
                            if (Math.pow(i,3) % 5 == 0) {
                                res[0].add(null);
                            }
                        } */

                    }
                }
            }
            long ed = System.currentTimeMillis();
            System.err.println("Stop: " + (ed - st));
            return res;
        }
    }
}
