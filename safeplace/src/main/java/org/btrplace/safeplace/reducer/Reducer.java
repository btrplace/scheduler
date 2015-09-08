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

package org.btrplace.safeplace.reducer;

import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.fuzzer.TestCase;
import org.btrplace.safeplace.runner.TestCaseResult;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.verification.Verifier;
import org.btrplace.safeplace.verification.spec.SpecVerifier;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class Reducer {
    public abstract TestCaseResult reduce(TestCaseResult tc, SpecVerifier oracle, Verifier against);

    public TestCase derive(TestCase tc, List<Constant> args, ReconfigurationPlan p) {
        return tc;
        //return new TestCase(tc.getTestClass(), tc.getTestName(), tc.getNumber(), tc.getConstraint(), args, p, tc.continuous());
    }

    public boolean consistent(SpecVerifier v1, Verifier v2, Constraint cstr, List<Constant> args, ReconfigurationPlan p, boolean c, TestCaseResult.Result errType) {
        return false;
        /*boolean r1, r2;
        CheckerResult cr1, cr2;
        if (c) {
            cr1 = v1.verify(cstr, args, p);
            cr2 = v2.verify(cstr, args, p);
        } else {
            Model src = p.getOrigin();
            Model dst = p.getResult();
            cr1 = v1.verify(cstr, args, src, dst);
            cr2 = v2.verify(cstr, args, src, dst);
        }
        TestCaseResult.Result res = TestCaseResult.makeResult(cr1, cr2);*/
        //return errType != res;
    }

    public boolean consistent(SpecVerifier v1, Verifier v2, TestCase tc, TestCaseResult.Result errType) {
        return consistent(v1, v2, tc.getConstraint(), tc.getParameters(), tc.getPlan(), tc.continuous(), errType);
    }

    public abstract long lastDuration();

    public abstract long lastReduction();
}
