/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.verification.btrplace;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;
import org.btrplace.plan.ReconfigurationPlanChecker;
import org.btrplace.plan.SatConstraintViolationException;
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.VerifierResult;
import org.btrplace.scheduler.SchedulerException;


/**
 * @author Fabien Hermenier
 */
public class CheckerVerifier implements Verifier {

    @Override
    @SuppressWarnings("squid:S1166")
    public VerifierResult verify(TestCase tc) {
        if (tc.impl() == null) {
            return VerifierResult.newOk();
        }
        SatConstraintChecker<? extends SatConstraint> checker = tc.impl().getChecker();

        if (tc.continuous()) {
            ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
            chk.addChecker(checker);
            try {
                chk.check(tc.plan());
                return VerifierResult.newOk();
            } catch (SatConstraintViolationException | SchedulerException ex) {
                return VerifierResult.newKo(ex.getMessage());
            }
        }
            boolean res = checker.endsWith(tc.plan().getResult());
            if (res) {
                return VerifierResult.newOk();
            }
            return VerifierResult.newKo("Incorrect destination model");
    }


    @Override
    public String toString() {
        return "checker";
    }

    @Override
    public String id() {
        return "checker";
    }
}
