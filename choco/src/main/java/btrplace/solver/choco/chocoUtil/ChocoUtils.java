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

import choco.cp.solver.constraints.integer.Element;
import choco.cp.solver.constraints.integer.ElementV;
import choco.cp.solver.constraints.integer.EuclideanDivisionXYZ;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.cp.solver.constraints.integer.bool.BooleanFactory;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;

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
    public static void postImplies(Solver s, SConstraint<IntDomainVar> c1, SConstraint<IntDomainVar> c2) {
        IntDomainVar bC1 = s.createBooleanVar("isSatisfied(" + c1.pretty() + ")");
        s.post(ReifiedFactory.builder(bC1, c1, s));

        IntDomainVar bC2 = s.createBooleanVar("isSatisfied(" + c2.pretty() + ")");
        s.post(ReifiedFactory.builder(bC2, c2, s));

        SConstraint cNotC1 = BooleanFactory.not(bC1);
        IntDomainVar bNotC1 = s.createBooleanVar("not(" + bC1.getName() + ")");
        s.post(ReifiedFactory.builder(bNotC1, cNotC1, s));


        s.post(BooleanFactory.or(s.getEnvironment(), bNotC1, bC2));
    }

    /**
     * Make and post an implies constraint where the first operand is a boolean: b1 -> c2.
     * The constraint is translated into (or(not(b1,c2))
     *
     * @param s  the solver
     * @param b1 the first constraint as boolean
     * @param c2 the second constraint
     */
    public static void postImplies(Solver s, IntDomainVar b1, SConstraint<IntDomainVar> c2) {

        IntDomainVar bC2 = s.createBooleanVar("isSatisfied(" + c2.pretty() + ")");
        s.post(ReifiedFactory.builder(bC2, c2, s));

        IntDomainVar notB1 = s.createBooleanVar("not(" + b1.getName() + ")");
        s.post(s.neq(b1, notB1));

        s.post(BooleanFactory.or(s.getEnvironment(), notB1, bC2));
    }

    /**
     * Make and post a postifOnlyIf constraint that state and(or(b1, non c2), or(non b1, c2))
     *
     * @param s  the solver
     * @param b1 the first constraint
     * @param c2 the second constraint
     */
    public static void postIfOnlyIf(Solver s, IntDomainVar b1, SConstraint c2) {
        IntDomainVar notBC1 = s.createBooleanVar("not(" + b1.getName() + ")");
        s.post(s.neq(b1, notBC1));

        IntDomainVar bC2 = s.createBooleanVar("isSatisfied(" + c2.pretty() + ")");
        s.post(ReifiedFactory.builder(bC2, c2, s));

        IntDomainVar notBC2 = s.createBooleanVar("not(" + bC2.getName() + ")");
        s.post(s.neq(notBC2, bC2));

        IntDomainVar or1 = s.createBooleanVar("or(" + b1.getName() + "," + notBC2.getName() + ")");
        s.post(ReifiedFactory.builder(or1, BooleanFactory.or(s.getEnvironment(), b1, notBC2), s));

        IntDomainVar or2 = s.createBooleanVar("or(" + notBC1.getName() + "," + bC2.getName() + ")");
        s.post(ReifiedFactory.builder(or2, BooleanFactory.or(s.getEnvironment(), notBC1, bC2), s));

        s.post(BooleanFactory.and(or1, or2));
    }

    /**
     * add a constraint such as array[index]=value
     */
    public static void nth(Solver s, IntDomainVar index, IntDomainVar[] array, IntDomainVar var) {
        s.post(new ElementV(ArrayUtils.append(array,
                new IntDomainVar[]{index, var}), 0, s.getEnvironment()));
    }

    /**
     * add a constraint such as array[index]=value
     */
    public static void nth(Solver s, IntDomainVar index, int[] array, IntDomainVar var) {
        s.post(new Element(index, array, var));
    }

    public static IntDomainVar nth(Solver s, IntDomainVar index, IntDomainVar[] array) {
        int[] minmax = getMinMax(array);
        IntDomainVar ret = s.createBoundIntVar(foldSetNames(array), minmax[0],
                minmax[1]);
        ChocoUtils.nth(s, index, array, ret);
        return ret;
    }

    /**
     * make a constraint, x * y = z
     */
    public static void mult(Solver s, IntDomainVar x, IntDomainVar y, IntDomainVar z) {
        s.post(new TimesXYZ(x, y, z));
    }

    public static IntDomainVar mult(Solver s, IntDomainVar left, IntDomainVar right) {
        int min = left.getInf() * right.getInf(), max = min;
        for (int prod : new int[]{left.getInf() * right.getSup(),
                left.getSup() * right.getSup(), left.getInf() * right.getSup()}) {
            if (prod < min) {
                min = prod;
            }
            if (prod > max) {
                max = prod;
            }
        }
        IntDomainVar ret = s.createBoundIntVar(
                "(" + left.getName() + ")*(" + right.getName() + ")", min, max);
        mult(s, left, right, ret);
        return ret;
    }

    public static IntDomainVar mult(Solver s, IntDomainVar left, int right) {
        int min = left.getInf() * right, max = min;
        int prod = left.getSup() * right;
        if (prod < min) {
            min = prod;
        }
        if (prod > max) {
            max = prod;
        }
        IntDomainVar ret = s.createBoundIntVar("(" + left.getName() + ")*" + right, min, max);
        mult(s, left, s.createIntegerConstant("" + right, right), ret);
        return ret;
    }

    public static IntDomainVar div(Solver s, IntDomainVar var, int i) {
        int a = var.getInf() / i;
        int b = var.getSup() / i;
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        IntDomainVar ret = s.createBoundIntVar("(" + var.getName() + ")/" + i, min,
                max);
        s.post(new EuclideanDivisionXYZ(var, s.createIntegerConstant("" + i, i), ret));
        return ret;
    }

    /**
     * print an array of IntDomainVar as {var0, var1, var2, var3}
     */
    public static String foldSetNames(IntDomainVar[] values) {
        StringBuilder sb = null;
        for (IntDomainVar idv : values) {
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
     * IntDomainVar
     *
     * @param array the table of VarIntDomain
     * @return [min(inf(array)), max(sup(array))]
     */
    public static int[] getMinMax(IntDomainVar[] array) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (IntDomainVar idv : array) {
            if (idv.getInf() < min) {
                min = idv.getInf();
            }
            if (idv.getSup() > max) {
                max = idv.getSup();
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
    public static int[] getNextContiguousValues(IntDomainVar v, int from) {
        IntDomain dom = v.getDomain();
        int ub = dom.getSup();
        int lb = dom.getNextValue(from - 1);
        //from - 1 to include the 'from' value if needed
        int prev = lb;
        for (int val = dom.getNextValue(lb); val <= ub; val = dom.getNextValue(val)) {
            if (val == prev || val == prev + 1) {
                prev++;
            } else {
                //Not contiguous
                return new int[]{lb, prev};
            }
        }
        return new int[]{lb, ub};
    }

    public static String prettyContiguous(IntDomainVar v) {
        int[] zone = getNextContiguousValues(v, v.getInf());
        StringBuilder buf = new StringBuilder(Arrays.toString(zone));
        while (zone[1] < v.getSup()) {
            zone = getNextContiguousValues(v, zone[1] + 1);
            buf.append(' ').append(Arrays.toString(zone));
        }
        return buf.toString();
    }
}
