package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.backend.NoBackend;
import btrplace.solver.api.cstrSpec.backend.VerificationBackend;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.guard.Guard;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerificationFuzz implements Guardable {

    private Constraint cstr;

    private Verifier v;

    private boolean continuous;

    private ReconfigurationPlanFuzzer2 fuzz;

    private VerificationBackend backend;

    private List<VerifDomain> vDoms;
    private boolean verbose;

    private int nbWorkers;

    private List<Guard> guards;

    private List<CallableVerification> slaves;

    private List<Constraint> pre;

    public ParallelConstraintVerificationFuzz(ReconfigurationPlanFuzzer2 fuzz, List<VerifDomain> vDoms, Verifier v, Constraint cstr) {
        guards = new ArrayList<>();
        backend = new NoBackend();
        nbWorkers = Runtime.getRuntime().availableProcessors();
        verbose = false;
        continuous = true;

        this.fuzz = fuzz;

        this.vDoms = vDoms;
        this.v = v;
        this.cstr = cstr;
        slaves = new ArrayList<>();
        pre = new ArrayList<>();
    }


    public void verify() {
        ThreadFactory tf = new ThreadFactory() {

            private int slaveId;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("slave-" + (++slaveId));
                return t;
            }
        };
        Executor exec = Executors.newFixedThreadPool(nbWorkers, tf);

        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(exec);

        for (int i = 0; i < nbWorkers; i++) {

            try {
                CallableVerification c;
                if (continuous) {
                    c = new CallableContinuousVerificationFuzz2(this, fuzz, v, vDoms, cstr);
                } else {
                    c = new CallableDiscreteVerificationFuzz2(this, fuzz, v, vDoms, cstr);
                }
                completionService.submit(c);
                slaves.add(c);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        for (int i = 0; i < nbWorkers; i++) {
            try {
                Future<Boolean> f = completionService.take();
                Boolean ok = f.get();
                if (!ok) {
                    for (CallableVerification slave : slaves) {
                        slave.stop();
                    }
                }
            } catch (UnsupportedOperationException ex) {
                throw ex;
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        backend.flush();
    }

    public boolean commitDefiant(TestCase c) {
        for (Guard g : guards) {
            if (!g.acceptDefiant(c)) {
                stopAll();
                return false;
            }
        }
        backend.addDefiant(c);
        return true;
    }

    public boolean commitCompliant(TestCase c) {
        for (Guard g : guards) {
            if (!g.acceptCompliant(c)) {
                stopAll();
                return false;
            }
        }
        backend.addCompliant(c);
        return true;
    }

    private void stopAll() {
        for (CallableVerification slave : slaves) {
            slave.stop();
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getNbWorkers() {
        return nbWorkers;
    }

    public void setNbWorkers(int nbWorkers) {
        this.nbWorkers = nbWorkers;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public boolean precondition(Constraint c) {
        if (!c.isCore()) {
            return false;
        }
        return pre.add(c);
    }

    public List<Constraint> preconditions() {
        return pre;
    }

    @Override
    public Guardable limit(Guard l) {
        guards.add(l);
        return this;
    }

    public void setBackend(VerificationBackend backend) {
        this.backend = backend;
    }
}
