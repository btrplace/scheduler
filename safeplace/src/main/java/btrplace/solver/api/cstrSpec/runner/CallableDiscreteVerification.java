package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ResultingModelsGenerator;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Fabien Hermenier
 */
class CallableDiscreteVerification implements Callable<List<TestCase>[]> {

    private Model mo;

    private Constraint c;

    private Verifier ve;

    private boolean verbose;

    private List<VerifDomain> vDoms;

    public CallableDiscreteVerification(Verifier ve, List<VerifDomain> vDoms, Model mo, Constraint c, boolean verbose) {
        this.mo = mo;
        this.c = c;
        this.ve = ve;
        this.vDoms = vDoms;
        this.verbose = verbose;

    }

    @Override
    public List<TestCase>[] call() {
        SpecVerifier specVerifier = new SpecVerifier();
        int nbPlans = 0;
        ResultingModelsGenerator grn = new ResultingModelsGenerator(mo);
        SpecModel sp = new SpecModel(mo);
        List<List<Constant>> allArgs = new ArrayList<>();
        ConstraintInputGenerator cig = new ConstraintInputGenerator(this.c, sp, true);
        for (List<Constant> args : cig) {
            allArgs.add(args);
        }
        System.err.println(allArgs);
        List<TestCase>[] res = new List[2];
        res[0] = new ArrayList<>();
        res[1] = new ArrayList<>();
        long st = System.currentTimeMillis();
        Set<Model> s = new HashSet<>();

        for (Model dst : grn) {
            if (!s.add(dst)) {
                System.err.println("Duplicate !");
                for (Model x : s) {
                    System.err.println("already: \n" + x.getMapping());
                    System.err.println("new: \n" + dst);
                }
            }
            nbPlans++;
            for (List<Constant> args : allArgs) {
                CheckerResult specRes = specVerifier.verify(c, mo, dst, args);
                CheckerResult againstRes = ve.verify(c, mo, dst, args);
                TestCase tc = new TestCase(specRes, againstRes, ve, this.c, mo, dst, args, true);
                try {
                    if (tc.succeed()) {
                        res[0].add(tc);
                    } else {
                        System.err.println(Thread.currentThread().getName() /*+ " - add\n" + tc.pretty(true)*/);
                           /* if (!s.add(tc.pretty(true))) {
                                System.err.println("Bug");
                            }*/
                        //System.err.println(tc.pretty(true));
                        res[1].add(tc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    res[1].add(tc);
                }
            }
        }
        long ed = System.currentTimeMillis();
        if (verbose) {
            System.out.println(nbPlans + " plan(s) x " + allArgs.size() + " input(s) = " + (nbPlans * allArgs.size()) + " tests in " + (ed - st) + " ms");
        }
        return res;
    }
}
