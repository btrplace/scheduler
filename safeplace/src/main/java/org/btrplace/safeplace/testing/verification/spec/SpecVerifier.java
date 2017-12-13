/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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
            UserVar<?> var = c.args().get(i);
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
            mo.setRootContext(new Context(tc.instance().getModel().copy()));
            fillArguments(mo, tc);
            Boolean res = good.eval(mo);
            if (!Boolean.TRUE.equals(res)) {
                return VerifierResult.newKo("Failure at the initial stage");
            }
            ReconfigurationSimulator sim = new ReconfigurationSimulator(mo, tc.plan());
            int x = sim.start(good);
            if (x >= 0) {
                return VerifierResult.newKo("Failure at time '" + x + "'");
            }
            return VerifierResult.newOk();
        }

        //DISCRETE
        Model res = tc.plan().getResult();
        if (res == null) {
            throw new IllegalStateException("no destination model");
        }
        Context mo = new Context(res);
        mo.setRootContext(new Context(tc.instance().getModel().copy()));
        fillArguments(mo, tc);

        Boolean bOk = good.eval(mo);
        if (bOk == null) {
            return VerifierResult.newError(new Exception("Runtime error in the spec"));
        }
        if (bOk) {
            return VerifierResult.newOk();
        }
        return VerifierResult.newKo("Unconsistent destination model");

    }

    @Override
    public String id() {
        return "spec";
    }
}
