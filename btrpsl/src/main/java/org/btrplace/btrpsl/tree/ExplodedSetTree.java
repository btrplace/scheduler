/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.tree;

import org.antlr.runtime.Token;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.element.BtrpOperand;
import org.btrplace.btrpsl.element.BtrpSet;
import org.btrplace.btrpsl.element.DefaultBtrpOperand;
import org.btrplace.btrpsl.element.IgnorableOperand;

import java.util.HashSet;
import java.util.Set;

/**
 * A parser to make exploded sets.
 *
 * @author Fabien Hermenier
 */
public class ExplodedSetTree extends BtrPlaceTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public ExplodedSetTree(Token t, ErrorReporter errs) {
        super(t, errs);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        if (getChildCount() == 0) {
            return ignoreError("Empty sets not allowed");
        }
        BtrpOperand t0 = getChild(0).go(this);
        if (t0 == IgnorableOperand.getInstance()) {
            return t0;
        }
        BtrpSet s = new BtrpSet(t0.degree() + 1, t0.type());

        Set<BtrpOperand> viewed = new HashSet<>();
        for (int i = 0; i < getChildCount(); i++) {
            BtrpOperand tx = getChild(i).go(this);
            //s.getIntValue().add() is not safe at all. So preconditions have to be check
            if (tx == IgnorableOperand.getInstance()) {
                return tx;
            }
            if (tx.degree() != s.degree() - 1) {
                return ignoreError(tx + " has type '" + tx.prettyType() + "'. It should be a '" +
                        DefaultBtrpOperand.prettyType(s.degree() - 1, s.type()) + "' to be insertable into a '" +
                        s.prettyType() + "'");
            }
            if (tx.type() != s.type()) {
                return ignoreError("Unable to add '" + tx.type() + "' elements in a set of '" + s.type() + "' elements");
            }
            if (viewed.add(tx)) {
                s.getValues().add(tx);
            } else {
                return ignoreError(tx + " ignored");
            }
        }
        return s;
    }
}
