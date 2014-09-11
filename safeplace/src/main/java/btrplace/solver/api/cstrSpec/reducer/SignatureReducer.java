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

package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.ColType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reduce a constraint signature to the minimum possible.
 * <p>
 * In practice the sets are reduced one by one by removing values one by one.
 * A value will stay in the set if its removal lead to a different error.
 *
 * @author Fabien Hermenier
 */
public class SignatureReducer extends Reducer {

    private List<Constant> deepCopy(List<Constant> in) {
        List<Constant> cpy = new ArrayList<>(in.size());
        for (Constant c : in) {
            Type t = c.type();
            Object v = c.eval(null);
            Object o = v; //Assume immutable if not a collection
            if (v instanceof Collection) {
                o = toList((Collection) v);
                //Deep transformation into lists
            }
            cpy.add(new Constant(o, t));
        }
        return cpy;
    }

    private List toList(Collection v) {
        List l = new ArrayList(v.size());
        for (Object o : v) {
            if (o instanceof Collection) {
                l.add(toList((Collection) o));
            } else {
                //Assume immutable
                l.add(o);
            }
        }
        return l;
    }

    public CTestCase reduce(CTestCase tc, SpecVerifier v1, Verifier v2, CTestCaseResult.Result errType) throws Exception {
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getParameters();
        List<Constant> cpy = deepCopy(in);
        //System.out.println("Reducing " + cpy);
        for (int i = 0; i < cpy.size(); i++) {
            reduceArg(v1, v2, p, cstr, cpy, i, errType);
            //  System.out.println("\t" + cpy);
        }
        //System.out.println("Result: " + cpy);
        return derive(tc, cpy, p);
    }

    private void reduceArg(SpecVerifier v1, Verifier v2, ReconfigurationPlan p, Constraint cstr, List<Constant> in, int i, CTestCaseResult.Result errType) throws Exception {
        Constant c = in.get(i);
        if (c.type() instanceof ColType) {
            List l = (List) c.eval(null);
            in.set(i, new Constant(l, c.type()));
            for (int j = 0; j < l.size(); j++) {
                if (!reduceSetTo(v1, v2, p, cstr, in, l, j, errType)) {
                    j--;
                }
            }
        }
    }

    private boolean reduceSetTo(SpecVerifier v1, Verifier v2, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List col, int i, CTestCaseResult.Result errType) throws Exception {
        if (col.get(i) instanceof Collection) {
            System.err.println("before fail " + col + " " + i);
            if (failWithout(v1, v2, p, cstr, in, col, i, errType)) {
                return true;
            }
            System.err.println("after fail " + col + " " + i);
            List l = (List) col.get(i);
            col.set(i, l);
            for (int j = 0; j < l.size(); j++) {
                if (!reduceSetTo(v1, v2, p, cstr, in, l, j, errType)) {
                    j--;
                }
            }
            return false;
        } else {
            boolean ret = failWithout(v1, v2, p, cstr, in, col, i, errType);
            //System.out.println(" no way to remove that value");
            return ret;
        }
    }

    private boolean failWithout(SpecVerifier v1, Verifier v2, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List col, int i, CTestCaseResult.Result errType) throws Exception {
        Object o = col.remove(i);

        return false;
        /*if (consistent(v1, v2, new CTestCase("", cstr, in, p) errType)) { //Not the same error. Component needed
            col.add(i, o);
            //System.out.println("Unable to remove  " + o + " from " + col);
            return true;
        }
        return false;*/
    }
}