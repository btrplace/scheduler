/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;


import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;

/**
 * Utility class to ease the creation of some constraints on Choco.
 *
 * @author Fabien Hermenier
 */
public final class ChocoUtils {

    private ChocoUtils() {
    }

    /**
     * Make and post an implies constraint where the first operand is a boolean: b1 -&gt; c2.
     * The constraint is translated into (or(not(b1,c2))
     *
     * @param rp the problem to solve
     * @param b1 the first constraint as boolean
     * @param c2 the second constraint
     */
    public static void postImplies(ReconfigurationProblem rp, BoolVar b1, Constraint c2) {
        Model s = rp.getModel();
        BoolVar bC2;
        if (rp.labelVariables()) {
            bC2 = s.boolVar(rp.makeVarLabel(c2.toString(), " satisfied"));
        } else {
            bC2 = s.boolVar();
        }
        c2.reifyWith(bC2);

        BoolVar notB1 = b1.not();
        s.post(rp.getModel().arithm(b1, "!=", notB1));

        s.post(rp.getModel().or(notB1, bC2));
    }

    /**
     * Make and post a constraint that states and(or(b1, non c2), or(non b1, c2))
     *
     * @param rp the problem to solve
     * @param b1 the first constraint
     * @param c2 the second constraint
     */
    public static void postIfOnlyIf(ReconfigurationProblem rp, BoolVar b1, Constraint c2) {
        Model csp = rp.getModel();
        BoolVar notBC1 = b1.not();
        BoolVar bC2;
        if (rp.labelVariables()) {
            bC2 = csp.boolVar(rp.makeVarLabel(c2, " satisfied"));
        } else {
            bC2 = csp.boolVar();
        }
        c2.reifyWith(bC2);
        BoolVar notBC2 = bC2.not();
        csp.post(rp.getModel().or(rp.getModel().or(b1, bC2), rp.getModel().or(notBC1, notBC2)));
    }
}
