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

package btrplace.safeplace.verification.spec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.spec.prop.Proposition;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.spec.term.UserVar;
import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.verification.CheckerResult;
import btrplace.safeplace.verification.Verifier;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier {

    private List<VerifDomain> vDoms;

    public SpecVerifier(List<VerifDomain> vDoms) {
        this.vDoms = vDoms;
    }

    public SpecVerifier() {
        this(Collections.<VerifDomain>emptyList());
    }

    @Override
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

    @Override
    public CheckerResult verify(Constraint cstr, List<Constant> values, ReconfigurationPlan p) {

        Proposition good = cstr.getProposition();
        SpecModel mo = new SpecModel(p.getOrigin()); //Discrete means the plan contains no actions.
        setInputs(cstr, mo, values);
        SpecReconfigurationPlanChecker spc = new SpecReconfigurationPlanChecker(mo, p);
        try {
            Action a = spc.check(good);
            if (a != null) {
                //System.out.println("A: " + a);
                return new CheckerResult(false, a);
            }

        } catch (Exception e) {
            if ("Failure at the beginning of the plan".equals(e.getMessage())) {
                return CheckerResult.newKo(e.getMessage());
            }
            e.printStackTrace();
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
