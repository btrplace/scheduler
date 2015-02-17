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
import org.btrplace.btrpsl.antlr.ANTLRBtrplaceSL2Lexer;
import org.btrplace.btrpsl.element.*;

/**
 * A part of an enumeration.
 * Can be a single element or a range of numbers.
 * Returns a set of string
 *
 * @author Fabien Hermenier
 */
public class Range extends BtrPlaceTree {

    /**
     * Make a new tree
     *
     * @param payload the root token
     * @param errors  the errors to report
     */
    public Range(Token payload, ErrorReporter errors) {
        super(payload, errors);
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {

        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.string);
        if (getChildCount() == 1) {
            if (getChild(0).getType() != ANTLRBtrplaceSL2Lexer.IDENTIFIER) {
                BtrpOperand o = getChild(0).go(this);
                if (o.degree() > 0) {
                    return ignoreError(getChild(0).getToken(), o + ": sets are not allowed in an enumeration");
                }
                s.getValues().add(new BtrpString(getChild(0).go(this).toString()));
            } else {
                s.getValues().add(new BtrpString(getChild(0).getText()));
            }
        } else if (getChildCount() == 2) {
            BtrpOperand first = getChild(0).go(this);
            BtrpOperand last = getChild(1).go(this);
            if (first == IgnorableOperand.getInstance() || last == IgnorableOperand.getInstance()) {
                return IgnorableOperand.getInstance();
            }
            if (first.type() != BtrpOperand.Type.number || last.type() != BtrpOperand.Type.number) {
                return ignoreError(getChild(first.type() == BtrpOperand.Type.number ? 1 : 0).getToken(), "Bounds must be numbers");
            }
            BtrpNumber begin = (BtrpNumber) first;
            BtrpNumber end = (BtrpNumber) last;
            if (!begin.isInteger() || !end.isInteger()) {
                return ignoreError(getChild(begin.isInteger() ? 1 : 0).getToken(), "Bounds must be integers");
            }

            if (begin.getBase() != end.getBase()) {
                return ignoreError(getChild(1).getToken(), "bounds must be expressed in the same base");
            }

            int from = Math.min(begin.getIntValue(), end.getIntValue());
            int to = Math.max(begin.getIntValue(), end.getIntValue());
            for (int i = from; i <= to; i++) {
                BtrpNumber bi = new BtrpNumber(i, begin.getBase()); //Keep the base
                s.getValues().add(new BtrpString(bi.toString()));
            }

            //Set the right line and col number wrt the second number (as the first one is an artificial token)
            token.setLine(getChild(1).getLine());
            token.setCharPositionInLine(getChild(1).getCharPositionInLine() - 2); //remove the ".."
        } else {
            return ignoreError("Bug in range");
        }

        return s;
    }
}
