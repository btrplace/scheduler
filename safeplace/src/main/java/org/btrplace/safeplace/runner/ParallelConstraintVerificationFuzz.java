/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.safeplace.runner;

import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.backend.NoBackend;
import org.btrplace.safeplace.backend.VerificationBackend;
import org.btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer2;
import org.btrplace.safeplace.guard.Guard;
import org.btrplace.safeplace.verification.TestCase;
import org.btrplace.safeplace.verification.Verifier;
import org.btrplace.safeplace.verification.spec.VerifDomain;

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
