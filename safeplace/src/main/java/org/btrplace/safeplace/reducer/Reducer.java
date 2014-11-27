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

import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.CTestCase;
import org.btrplace.safeplace.CTestCaseResult;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.verification.CheckerResult;
import org.btrplace.safeplace.verification.Verifier;
import org.btrplace.safeplace.verification.spec.SpecVerifier;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class Reducer {
    public abstract CTestCase reduce(CTestCase tc, SpecVerifier v1, Verifier v2, CTestCaseResult.Result errType) throws Exception;

    public CTestCase derive(CTestCase tc, List<Constant> args, ReconfigurationPlan p) {
        return new CTestCase(tc.getTestClass(), tc.getTestName(), tc.getNumber(), tc.getConstraint(), args, p, tc.continuous());
    }

    public boolean consistent(SpecVerifier v1, Verifier v2, Constraint cstr, List<Constant> args, ReconfigurationPlan p, boolean c, CTestCaseResult.Result errType) {
        boolean r1, r2;
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
        CTestCaseResult.Result res = CTestCaseResult.makeResult(cr1, cr2);
        return errType != res;
        /*if (errType == res) {
            return fal
        }
        System.out.println(cr1 + " " + cr2);
        r1 = cr1.getStatus();
        r2 = cr2.getStatus();
        //System.out.println("With " + tc.getConstraint().toString(tc.getParameters()) + " " + r1 + " " + r2);
        if (r1 == r2) {
            return true;
        }
        //System.out.println(errType + " to " + r1 + " " + r2);
        //We maintain the error type
        if ((errType == CTestCaseResult.Result.falseNegative && !r2) || (errType == CTestCaseResult.Result.falsePositive && r2)) {
            return false;
        }
        return true; */
    }

    public boolean consistent(SpecVerifier v1, Verifier v2, CTestCase tc, CTestCaseResult.Result errType) {
        return consistent(v1, v2, tc.getConstraint(), tc.getParameters(), tc.getPlan(), tc.continuous(), errType);
    }

}
