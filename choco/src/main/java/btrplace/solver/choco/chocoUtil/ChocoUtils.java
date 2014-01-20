/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.chocoUtil;


import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

import java.util.Arrays;

import static solver.constraints.LogicalConstraintFactory.and;
import static solver.constraints.LogicalConstraintFactory.or;

/**
 * Utility class to ease the creation of some constraints on Choco.
 *
 * @author Fabien Hermenier
 */
public final class ChocoUtils {

    private ChocoUtils() {
    }

    /**
     * Make and post an implies constraint: c1 -> c2.
     * The constraint is translated into (or(not(c1),c2))
     *
     * @param s  the solver
     * @param c1 the first constraint
     * @param c2 the second constraint
     */
    public static void postImplies(Solver s, Constraint c1, Constraint c2) {
        BoolVar bC1 = c1.reif();
        //IntVar bC1 = s.createBooleanVar("isSatisfied(" + c1.toString() + ")");
        //s.post(ReifiedFactory.builder(bC1, c1, s));

        //IntVar bC2 = s.createBooleanVar("isSatisfied(" + c2.toString() + ")");
        //s.post(ReifiedFactory.builder(bC2, c2, s));
        BoolVar bC2 = c2.reif();

        //Constraint cNotC1 = BooleanFactory.not(bC1);
        //IntVar bNotC1 = s.createBooleanVar("not(" + bC1.getName() + ")");
        //s.post(ReifiedFactory.builder(bNotC1, cNotC1, s));
        BoolVar bNotC1 = bC1.not();

        s.post(or(bNotC1, bC2));
    }

    /**
     * Make and post an implies constraint where the first operand is a boolean: b1 -> c2.
     * The constraint is translated into (or(not(b1,c2))
     *
     * @param s  the solver
     * @param b1 the first constraint as boolean
     * @param c2 the second constraint
     */
    public static void postImplies(Solver s, BoolVar b1, Constraint c2) {

        //IntVar bC2 = s.createBooleanVar("isSatisfied(" + c2.toString() + ")");
        //s.post(ReifiedFactory.builder(bC2, c2, s));

        BoolVar bC2 = c2.reif();

        BoolVar notB1 = VariableFactory.not(b1);//("not(" + b1.getName() + ")", s);
        s.post(IntConstraintFactory.arithm(b1, "!=", notB1));

        s.post(or(notB1, bC2));
    }

    /**
     * Make and post a postifOnlyIf constraint that state and(or(b1, non c2), or(non b1, c2))
     *
     * @param s  the solver
     * @param b1 the first constraint
     * @param c2 the second constraint
     */
    public static void postIfOnlyIf(Solver s, BoolVar b1, Constraint c2) {
        BoolVar notBC1 = VariableFactory.not(b1);

        BoolVar bC2 = c2.reif();
        /*s.createBooleanVar("isSatisfied(" + c2.toString() + ")");
        s.post(ReifiedFactory.builder(bC2, c2, s));*/

        BoolVar notBC2 = bC2.not();
        //IntVar notBC2 = s.createBooleanVar("not(" + bC2.getName() + ")");
        //s.post(s.neq(notBC2, bC2));

        //IntVar or1 = s.createBooleanVar("or(" + b1.getName() + "," + notBC2.getName() + ")");
        //s.post(ReifiedFactory.builder(or1, BooleanFactory.or(s.getEnvironment(), b1, notBC2), s));

        //IntVar or2 = s.createBooleanVar("or(" + notBC1.getName() + "," + bC2.getName() + ")");
        //s.post(ReifiedFactory.builder(or2, BooleanFactory.or(s.getEnvironment(), notBC1, bC2), s));

        s.post(or(and(b1, bC2), and(notBC1, notBC2)));//BooleanFactory.and(or1, or2));
    }

    /**
     * add a constraint such as array[index]=value
     */
    public static void nth(Solver s, IntVar index, IntVar[] array, IntVar var) {

        s.post(new ElementV(ArrayUtils.append(array,
                new IntVar[]{index, var}), 0, s.getEnvironment()));
    }

    /**
     * add a constraint such as array[index]=value
     */
    public static void nth(Solver s, IntVar index, int[] array, IntVar var) {
        //s.post(new Element(index, array, var));
        s.post(IntConstraintFactory.element(index, array, var));
    }

    public static IntVar nth(Solver s, IntVar index, IntVar[] array) {
        int[] minmax = getMinMax(array);
        IntVar ret = VariableFactory.bounded(foldSetNames(array), minmax[0], minmax[1], s);
        ChocoUtils.nth(s, index, array, ret);
        return ret;
    }

    /**
     * make a constraint, x * y = z
     */
    public static void mult(Solver s, IntVar x, IntVar y, IntVar z) {
        //s.post(new TimesXYZ(x, y, z));
        s.post(IntConstraintFactory.times(x, y, z));
    }

    public static IntVar mult(Solver s, IntVar left, IntVar right) {
        int min = left.getLB() * right.getLB(), max = min;
        for (int prod : new int[]{left.getLB() * right.getUB(),
                left.getUB() * right.getUB(), left.getLB() * right.getUB()}) {
            if (prod < min) {
                min = prod;
            }
            if (prod > max) {
                max = prod;
            }
        }
        IntVar ret = VariableFactory.bounded(
                "(" + left.getName() + ")*(" + right.getName() + ")", min, max, s);
        mult(s, left, right, ret);
        return ret;
    }

    public static IntVar mult(Solver s, IntVar left, int right) {
        int min = left.getLB() * right, max = min;
        int prod = left.getUB() * right;
        if (prod < min) {
            min = prod;
        }
        if (prod > max) {
            max = prod;
        }
        IntVar ret = VariableFactory.bounded("(" + left.getName() + ")*" + right, min, max, s);
        mult(s, left, VariableFactory.fixed(right, s), ret);
        return ret;
    }

    public static IntVar div(Solver s, IntVar var, int i) {
        int a = var.getLB() / i;
        int b = var.getUB() / i;
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        IntVar ret = VariableFactory.bounded("(" + var.getName() + ")/" + i, min, max, s);
        s.post(IntConstraintFactory.eucl_div(var, VariableFactory.fixed(i, s), ret));//new EuclideanDivisionXYZ(var, VariableFactory.fixed(i, s), ret));
        return ret;
    }

    /**
     * print an array of IntVar as {var0, var1, var2, var3}
     */
    public static String foldSetNames(IntVar[] values) {
        StringBuilder sb = null;
        for (IntVar idv : values) {
            if (sb == null) {
                sb = new StringBuilder("{");
            } else {
                sb.append(", ");
            }
            sb.append(idv.getName());
        }
        return sb == null ? "{}" : sb.append("}").toString();
    }

    /**
     * get the min and max values of the inf and sup ranges of an array of
     * IntVar
     *
     * @param array the table of VarIntDomain
     * @return [min(inf(array)), max(sup(array))]
     */
    public static int[] getMinMax(IntVar[] array) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (IntVar idv : array) {
            if (idv.getLB() < min) {
                min = idv.getLB();
            }
            if (idv.getUB() > max) {
                max = idv.getUB();
            }
        }
        return new int[]{min, max};
    }

    /**
     * Get the next interval of possible contiguous values for an enumerated variable.
     *
     * @param v    the variable to inspect
     * @param from the initial value to consider.
     * @return an interval of values
     */
    public static int[] getNextContiguousValues(IntVar v, int from) {
        ///IntDomain dom = v.getDomain();
        int ub = v.getUB();
        int lb = v.nextValue(from - 1);
        //from - 1 to include the 'from' value if needed
        int prev = lb;
        for (int val = v.nextValue(lb); val <= ub; val = v.nextValue(val)) {
            if (val == prev || val == prev + 1) {
                prev++;
            } else {
                //Not contiguous
                return new int[]{lb, prev};
            }
        }
        return new int[]{lb, ub};
    }

    public static String prettyContiguous(IntVar v) {
        int[] zone = getNextContiguousValues(v, v.getLB());
        StringBuilder buf = new StringBuilder(Arrays.toString(zone));
        while (zone[1] < v.getUB()) {
            zone = getNextContiguousValues(v, zone[1] + 1);
            buf.append(' ').append(Arrays.toString(zone));
        }
        return buf.toString();
    }
}
