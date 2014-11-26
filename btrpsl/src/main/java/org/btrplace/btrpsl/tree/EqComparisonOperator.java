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

/**
 * A parser to check the equality between two operands.
 * Return 1 if equals, 0 otherwise. Types must be the same
 *
 * @author Fabien Hermenier
 */
public class EqComparisonOperator extends BtrPlaceTree {

    private boolean opposite = false;

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public EqComparisonOperator(Token t, boolean opp, ErrorReporter errs) {
        super(t, errs);
        this.opposite = opp;
    }

    @Override
    public BtrpOperand go(BtrPlaceTree parent) {
        BtrpOperand l = getChild(0).go(this);
        BtrpOperand r = getChild(1).go(this);
        if (!opposite) {
            return l.eq(r);
        } else {
            return l.eq(r).not();
        }
    }

}
