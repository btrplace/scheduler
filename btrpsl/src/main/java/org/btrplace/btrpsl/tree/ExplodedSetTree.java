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
