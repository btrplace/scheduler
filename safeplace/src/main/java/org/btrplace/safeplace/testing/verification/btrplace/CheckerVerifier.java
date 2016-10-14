/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing.verification.btrplace;

import org.btrplace.model.constraint.SatConstraintChecker;
import org.btrplace.plan.ReconfigurationPlanChecker;
import org.btrplace.plan.SatConstraintViolationException;
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.VerifierResult;
import org.btrplace.scheduler.SchedulerException;

//import org.btrplace.plan.ReconfigurationPlanCheckerException;

/**
 * @author Fabien Hermenier
 */
public class CheckerVerifier implements Verifier {

    @Override
    public VerifierResult verify(TestCase tc) {
        if (tc.impl() == null) {
            return VerifierResult.newOk();
        }
        SatConstraintChecker checker = tc.impl().getChecker();

        if (tc.continuous()) {
            ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
            chk.addChecker(checker);
            try {
                chk.check(tc.plan());
                return VerifierResult.newOk();
            } catch (SatConstraintViolationException ex) {
                return VerifierResult.newKo(ex.getMessage());
            } catch (SchedulerException ex) {
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
