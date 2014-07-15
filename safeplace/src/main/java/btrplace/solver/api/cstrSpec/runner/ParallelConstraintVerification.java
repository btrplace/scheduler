package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

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

    private List<VerifDomain> vDoms;
    private boolean verbose;


    public ParallelConstraintVerification(ModelsGenerator mg, List<VerifDomain> vDoms, Verifier v, int nbWorkers, Constraint cstr, boolean cont, boolean verbose) {
        ThreadFactory tf = new ThreadFactory() {

            private int slaveId;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("slave-" + (++slaveId));
                return t;
            }
        };
        exec = Executors.newFixedThreadPool(nbWorkers, tf);
        this.vDoms = vDoms;
        completionService = new ExecutorCompletionService<>(exec);
        this.v = v;
        this.cstr = cstr;
        this.continuous = cont;
        modelsGen = mg;
        this.verbose = verbose;
    }

    public void verify() {
        int nbModels = 0;
        long st = System.currentTimeMillis();
        for (Model mo : modelsGen) {

            try {
                Callable c;
                if (continuous) {
                    c = new CallableContinuousVerification(v, vDoms, mo, cstr, verbose, 10);
                } else {
                    c = new CallableDiscreteVerification(v, vDoms, mo, cstr, verbose);
                }
                completionService.submit(c);
                nbModels++;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        long end = System.currentTimeMillis();
        try {
            exec.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
        if (verbose) {
            System.out.println(nbModels + " produced in " + (end - st) + " ms");
        }
        for (int i = 0; i < nbModels; i++) {
            try {
                Future<List<TestCase>[]> f = completionService.take();
                List<TestCase>[] res = f.get();
                if ((i + 1) % 80 == 0) {
                    System.out.println();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
