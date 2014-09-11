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

package btrplace.safeplace.runner;

import btrplace.plan.ReconfigurationPlan;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.verification.TestCase;
import btrplace.safeplace.verification.Verifier;
import btrplace.safeplace.verification.spec.VerifDomain;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CallableContinuousVerificationFuzz2 extends DefaultCallableVerification {
    public CallableContinuousVerificationFuzz2(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer2 fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        super(master, fuzz, ve, vDoms, c);
    }

    public TestCase runTest(ReconfigurationPlan p, List<Constant> args) {
        /*CheckerResult specRes = specVerifier.verify(c, args, p);
        CheckerResult againstRes = ve.verify(c, args, p);
          */
        return new TestCase(null, null, ve, this.c, p, args, false);
    }

}
