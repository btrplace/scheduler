package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;

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
            CallableVerification c = new CallableVerification(v, mo, cstr, continuous);
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

}
