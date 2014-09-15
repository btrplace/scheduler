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

package btrplace.safeplace.verification.btrplace;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.verification.CheckerResult;
import btrplace.safeplace.verification.Verifier;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CheckerVerifier implements Verifier {

    @Override
    public CheckerResult verify(Constraint cstr, List<Constant> params, Model res, Model src) {
        if (cstr.isCore()) {
            if (res == null) {
                return new CheckerResult(false, "Core constraint violation");
            }
            return CheckerResult.newOk();
        }
        try {
            SatConstraint sat = Constraint2BtrPlace.build(cstr, params);
            if (sat.setContinuous(false)) {

                if (res == null) {
                    //Core constraint violation
                    return new CheckerResult(false, "Core constraint violation");
                }
                if (!sat.getChecker().endsWith(res)) {
                    return new CheckerResult(false, "Violation of " + sat.toString());
                }
                return CheckerResult.newOk();
            } else {
                throw new UnsupportedOperationException(sat + " cannot be discrete");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public CheckerResult verify(Constraint cstr, List<Constant> params, ReconfigurationPlan p) {
        if (cstr.isCore()) {
            Model res = p.getResult();
            if (res == null) {
                return new CheckerResult(false, "Core constraint violation");
            }
            return CheckerResult.newOk();
        }
        try {
            SatConstraint sat = Constraint2BtrPlace.build(cstr, params);
            if (sat.setContinuous(true)) {
                ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
                chk.addChecker(sat.getChecker());
                try {
                    chk.check(p);
                    return CheckerResult.newOk();
                } catch (ReconfigurationPlanCheckerException ex) {
                    //     ex.printStackTrace();
                    if (ex.getAction() == null) {
                        return CheckerResult.newKo(ex.getMessage());
                    }
                    return CheckerResult.newFailure(ex.getAction());
                }
            } else {
                throw new UnsupportedOperationException(sat + " cannot be continuous");
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new RuntimeException(ex);
        }
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
