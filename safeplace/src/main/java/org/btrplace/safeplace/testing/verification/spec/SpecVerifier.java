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

package org.btrplace.safeplace.testing.verification.spec;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.VerifierResult;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier {

    public void fillArguments(Context mo, TestCase tc) {
        Constraint c = tc.constraint();
        List<Constant> values = tc.args();

        //Check signature
        if (values.size() != c.args().size()) {
            throw new IllegalArgumentException(c.toString(values) + " cannot match " + c.signatureToString());
        }
        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.args().get(i);
            Type t = values.get(i).type();
            if (!var.type().equals(t)) {
                throw new IllegalArgumentException(c.toString(values) + " cannot match " + c.signatureToString());
            }
            mo.setValue(var.label(), values.get(i).eval(mo));
        }
    }

    @Override
    public VerifierResult verify(TestCase tc) {
        Proposition good = tc.constraint().proposition();

        if (tc.continuous()) {
            Context mo = new Context(tc.instance().getModel());
            fillArguments(mo, tc);
            SpecReconfigurationPlanChecker spc = new SpecReconfigurationPlanChecker(mo, tc.plan());
            //System.out.println(tc.plan());
            try {
                Action a = spc.check(good);
                if (a != null) {
                    return new VerifierResult(false, a);
                }

            } catch (Exception e) {
                if ("Failure at the beginning of the plan".equals(e.getMessage())) {
                    return VerifierResult.newKo(e.getMessage());
                }
                return VerifierResult.newError(e);
            } /*finally {
                System.out.println("Verif done");
            }*/
            return VerifierResult.newOk();
        }

        //discrete
        Model res = tc.plan().getResult();
        if (res == null) {
            System.err.println("NULL RES");
            tc.plan().getResult();
            System.err.println(tc.plan().getOrigin().getMapping());
            System.err.println(tc.plan());

        }
        Context mo = new Context(res);
        fillArguments(mo, tc);

        Boolean bOk = good.eval(mo);
        if (bOk == null) {
            return VerifierResult.newError(new Exception("Runtime error in the spec"));
        }
        if (bOk) {
            return VerifierResult.newOk();
        } else {
            return VerifierResult.newKo("Unconsistent destination model");
        }

    }

    @Override
    public String id() {
        return "spec";
    }
}
