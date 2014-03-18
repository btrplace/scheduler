package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

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

    private List<VerifDomain> vDoms;
    private boolean verbose;


    public ParallelConstraintVerification(ModelsGenerator mg, List<VerifDomain> vDoms, Verifier v, int nbWorkers, Constraint cstr, boolean cont, boolean verbose) {
        exec = Executors.newFixedThreadPool(nbWorkers);
        this.vDoms = vDoms;
        completionService = new ExecutorCompletionService<>(exec);
        this.v = v;
        this.cstr = cstr;
        this.continuous = cont;
        modelsGen = mg;
        ok = new ArrayList<>();
        ko = new ArrayList<>();
        this.verbose = verbose;
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
            Callable c;
            if (continuous) {
                c = new CallableContinuousVerification(v, vDoms, mo, cstr, verbose);
            } else {
                c = new CallableDiscreteVerification(v, vDoms, mo, cstr, verbose);
            }
            completionService.submit(c);
            nbModels++;
        }
        long end = System.currentTimeMillis();
        exec.shutdown();
        if (verbose) {
            System.out.println(nbModels + " produced in " + (end - st) + " ms");
        }
        for (int i = 0; i < nbModels; i++) {
            try {
                Future<List<TestCase>[]> f = completionService.take();
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

}
