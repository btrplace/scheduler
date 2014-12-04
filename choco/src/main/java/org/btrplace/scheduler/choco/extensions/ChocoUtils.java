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

package org.btrplace.scheduler.choco.extensions;


import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.VariableFactory;

import static org.chocosolver.solver.constraints.LogicalConstraintFactory.and;
import static org.chocosolver.solver.constraints.LogicalConstraintFactory.or;

/**
 * Utility class to ease the creation of some constraints on Choco.
 *
 * @author Fabien Hermenier
 */
public final class ChocoUtils {

    private ChocoUtils() {
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

        BoolVar bC2 = c2.reif();

        BoolVar notB1 = VariableFactory.not(b1);
        s.post(new Arithmetic(b1, Operator.NQ, notB1));

        s.post(or(notB1, bC2));
    }

    /**
     * Make and post a constraint that states and(or(b1, non c2), or(non b1, c2))
     *
     * @param s  the solver
     * @param b1 the first constraint
     * @param c2 the second constraint
     */
    public static void postIfOnlyIf(Solver s, BoolVar b1, Constraint c2) {
        BoolVar notBC1 = VariableFactory.not(b1);

        BoolVar bC2 = c2.reif();
        BoolVar notBC2 = bC2.not();
        s.post(or(and(b1, bC2), and(notBC1, notBC2)));
    }
}
