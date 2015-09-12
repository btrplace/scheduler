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

package org.btrplace.safeplace.verification.spec;

import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.fuzzer.TestCase;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.CheckerResult;
import org.btrplace.safeplace.verification.Verifier;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier {

    private List<Domain> vDoms;


    public SpecVerifier(List<Domain> vDoms) {
        this.vDoms = vDoms;
    }

    public SpecVerifier() {
        this(Collections.<Domain>emptyList());
    }

    @Override
    public Verifier clone() {
        SpecVerifier s = new SpecVerifier();
        for (Domain v : vDoms) {
            s.vDoms.add(v.clone());
        }
        return s;
    }

    @Override
    public CheckerResult verify(TestCase tc) {
        if (tc.continuous()) {
            return verify(tc.getConstraint(), tc.getParameters(), tc.getPlan().getOrigin(), tc.getPlan().getResult());
        }
        return verify(tc.getConstraint(), tc.getParameters(), tc.getPlan());
    }

    public CheckerResult verify(Constraint cstr, List<Constant> values, Model dst, Model src) {
        SpecModel sRes = new SpecModel(dst);
        setInputs(cstr, sRes, values);
        Proposition ok = cstr.getProposition();
        Boolean bOk = ok.eval(sRes);
        if (bOk == null) {
            return CheckerResult.newError(new Exception("Runtime error in the spec"));
        }
        if (bOk) {
            return CheckerResult.newOk();
        } else {
            return CheckerResult.newKo("Unconsistent destination model");
        }
    }

    public CheckerResult verify(Constraint cstr, List<Constant> values, ReconfigurationPlan p) {

        Proposition good = cstr.getProposition();
        SpecModel mo = new SpecModel(p.getOrigin()); //Discrete means the plan contains no actions.
        setInputs(cstr, mo, values);
        SpecReconfigurationPlanChecker spc = new SpecReconfigurationPlanChecker(mo, p);
        try {
            Action a = spc.check(good);
            if (a != null) {
                return new CheckerResult(false, a);
            }

        } catch (Exception e) {
            if ("Failure at the beginning of the plan".equals(e.getMessage())) {
                return CheckerResult.newKo(e.getMessage());
            }
            return CheckerResult.newError(e);
        }
        return CheckerResult.newOk();
    }

    private void setInputs(Constraint c, SpecModel mo, List<Constant> values) {
        //Check signature
        if (values.size() != c.getParameters().size()) {
            throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
        }
        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            Type t = values.get(i).type();
            if (!var.type().equals(t)) {
                throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
            }
        }

        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            mo.setValue(var.label(), values.get(i).eval(mo));
        }
    }

    public static String signatureToString(Constraint c) {
        StringBuilder b = new StringBuilder(c.id());
        b.append('(');
        Iterator<UserVar> ite = c.getParameters().iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    public static String toString(String id, List<Constant> args) {
        StringBuilder b = new StringBuilder(id);
        b.append('(');
        Iterator<Constant> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    @Override
    public String id() {
        return "spec";
    }
}
