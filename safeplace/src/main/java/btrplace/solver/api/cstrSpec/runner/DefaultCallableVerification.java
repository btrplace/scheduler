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

package btrplace.solver.api.cstrSpec.runner;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.guard.Guard;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class DefaultCallableVerification implements CallableVerification {

    protected Constraint c;

    protected Verifier ve;

    private List<VerifDomain> vDoms;

    private ReconfigurationPlanFuzzer2 fuzz;

    private List<Guard> guards;

    private boolean stop;

    private ParallelConstraintVerificationFuzz master;

    protected SpecVerifier specVerifier = new SpecVerifier();

    public DefaultCallableVerification(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer2 fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        this.master = master;
        this.fuzz = fuzz;
        stop = false;
        this.c = c;
        this.ve = ve;
        this.vDoms = vDoms;
        guards = new ArrayList<>();
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    public Boolean call() {

        while (!stop) {
            ReconfigurationPlan p = fuzz.next();

            ConstraintInputFuzzer cig = new ConstraintInputFuzzer(c, null);

            if (!checkPre(p)) {
                continue;
            }
            TestCase tc = runTest(p, cig.newParams());
            try {
                if (tc.succeed()) {
                    if (!master.commitCompliant(tc)) {
                        return false;
                    }
                } else {
                    if (!master.commitDefiant(tc)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!master.commitDefiant(tc)) {
                    return false;
                }
            }
        }
        return true;
    }

    public abstract TestCase runTest(ReconfigurationPlan p, List<Constant> args);

    private boolean checkPre(ReconfigurationPlan p) {
        /*SpecVerifier spec = new SpecVerifier();
        for (Constraint c : master.preconditions()) {
            CheckerResult res = spec.verify(c, Collections.<Constant>emptyList(), p);
            if (!res.getStatus()) {
                return false;
            }
        }
        for (Constraint c : master.preconditions()) {
            CheckerResult res = ve.verify(c, Collections.<Constant>emptyList(), p);
            if (!res.getStatus()) {
                return false;
            }
        }               */
        return true;
    }
}
